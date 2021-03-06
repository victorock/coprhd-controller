package com.emc.storageos.volumecontroller.impl.plugins.metering.vnxfile.processor;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.nas.vnxfile.xmlapi.MoverNetStats;
import com.emc.nas.vnxfile.xmlapi.ResponsePacket;
import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.constraint.AlternateIdConstraint;
import com.emc.storageos.db.client.constraint.URIQueryResultList;
import com.emc.storageos.db.client.model.Stat;
import com.emc.storageos.db.client.model.StoragePort;
import com.emc.storageos.db.client.model.StorageSystem;
import com.emc.storageos.plugins.AccessProfile;
import com.emc.storageos.plugins.BaseCollectionException;
import com.emc.storageos.plugins.common.Constants;
import com.emc.storageos.plugins.common.domainmodel.Operation;
import com.emc.storageos.plugins.metering.vnxfile.VNXFileConstants;
import com.emc.storageos.volumecontroller.impl.NativeGUIDGenerator;
import com.emc.storageos.volumecontroller.impl.plugins.metering.smis.processor.PortMetricsProcessor;
import com.emc.storageos.volumecontroller.impl.plugins.metering.vnxfile.VNXFileProcessor;

/**
 * VNXStoragePortStatsProcessor is responsible to process the result received from
 * XML API Server during VNX Storage Port Stats stream processing. This extracts the
 * session information from response packet and uses the session id in the
 * subsequent requests.
 */
public class VNXStoragePortStatsProcessor extends VNXFileProcessor {

    private final Logger _logger = LoggerFactory.getLogger(VNXFileProcessor.class);

    private PortMetricsProcessor portMetricsProcessor;

    @Override
    public void processResult(Operation operation, Object resultObj,
            Map<String, Object> keyMap) throws BaseCollectionException {
        final PostMethod result = (PostMethod) resultObj;
        _logger.info("processing moversStats response" + resultObj);
        try {

            List<Stat> newstatsList = null;
            Map<String, List<String>> interPortMap = null;

            AccessProfile profile = (AccessProfile) keyMap.get(Constants.ACCESSPROFILE);
            List<Stat> statsList = (List<Stat>) keyMap.get(VNXFileConstants.STATS);
            final DbClient dbClient = (DbClient) keyMap.get(VNXFileConstants.DBCLIENT);
            /*
             * step --> 1 get the interface map for mover and interface map contain values as storage ports
             * <MoverId, Map<interfaceIP, list<physicalPortName>>
             */
            Map<String, Map<String, List<String>>> moverInterMap =
                    (Map<String, Map<String, List<String>>>) keyMap
                            .get(VNXFileConstants.INTREFACE_PORT_MAP);

            ResponsePacket responsePacket = (ResponsePacket) _unmarshaller
                    .unmarshal(result.getResponseBodyAsStream());
            List<Object> moversStats = getQueryStatsResponse(responsePacket);
            Iterator<Object> iterator = moversStats.iterator();
            // get the storagesystem from db
            StorageSystem storageSystem = dbClient.queryObject(StorageSystem.class,
                    profile.getSystemId());
            // process Mover stats contains samples for each data mover and calculate port metrics
            while (iterator.hasNext()) {
                MoverNetStats moverNetStats = (MoverNetStats) iterator.next();

                // process mover stats per data mover
                String moverId = moverNetStats.getMover();
                // get interfaces and their list ports for mover id
                interPortMap = moverInterMap.get(moverId);

                // get the sample data of mover or VDM
                List<MoverNetStats.Sample> sampleList = moverNetStats.getSample();
                Map<String, BigInteger> stringMapPortIOs = new HashMap<String, BigInteger>();
                /*
                 * step -->2 get the io-ops of physical ports from samples
                 * <physicalPortName, Big(input + output band)>
                 */
                getPortIOTraffic(sampleList, stringMapPortIOs);
                // stats sample time
                long sampleTime = sampleList.get(0).getTime();
                /* step -->3 process the port metrics and update storageport object and store in db */
                newstatsList = processPortStatsInfo(interPortMap, stringMapPortIOs,
                        storageSystem, dbClient, sampleTime);
                // finally add to stat object
                statsList.addAll(newstatsList);

            }
            // calculate the avg port utilization for VDM and store in db
            portMetricsProcessor.dataMoverAvgPortMetrics(profile.getSystemId());

        } catch (final Exception ex) {
            _logger.error(
                    "Exception occurred while processing the volume stats response due to {}",
                    ex.getMessage());
        } finally {
            result.releaseConnection();
        }

    }

    /**
     * Process the Port metrics which are received from XMLAPI server.
     * 
     * @param interPortMap
     * @param stringMapPortIOs
     * @param storageSystem
     * @param dbClient
     * @param sampleTime
     * @return list of Port stats
     */
    private List<Stat> processPortStatsInfo(Map<String, List<String>> interPortMap,
            Map<String, BigInteger> stringMapPortIOs,
            StorageSystem storageSystem, DbClient dbClient,
            Long sampleTime) {

        // get the interfaces and corresponding port
        List<Stat> stat = new ArrayList<Stat>();
        Stat fePortStat = null;
        for (Entry<String, List<String>> entry : interPortMap.entrySet()) {
            String interfaceIP = entry.getKey();
            List<String> portList = entry.getValue();

            // get the port information
            String portNativeGuid = NativeGUIDGenerator.generateNativeGuid(
                    storageSystem, interfaceIP, NativeGUIDGenerator.PORT);
            StoragePort storagePort = findExistingPort(portNativeGuid, dbClient);

            _logger.info(
                    "interface {} and port details {}", interfaceIP,
                    storagePort.getPortName());

            // calculate traffic per interface- total traffic all ports/no of ports
            BigInteger iovalue = new BigInteger("0");
            for (String physicalName : portList) {
                iovalue = iovalue.add(stringMapPortIOs.get(physicalName));
            }
            // get Average port IO by adding and dividing the number.
            Long iopes = iovalue.longValue() / portList.size();

            Long kbytes = iopes / 1024;

            _logger.info("processIPPortMetrics input data iops{} and time details {} iopes",
                    iopes.toString(), sampleTime.toString());
            // set Ethernet port speed to 1Gbps
            storagePort.setPortSpeed(1L);
            // send port metrics processor to store the content
            portMetricsProcessor.processIPPortMetrics(kbytes, iopes, storagePort, sampleTime);

            // finally add above value to stat object
            fePortStat = preparePortStatInfo(portNativeGuid,
                    storagePort.getId(), iopes, sampleTime);
            stat.add(fePortStat);
        }

        return stat;
    }

    /**
     * Get IO traffic(in + out) from sample list
     * 
     * @param sampleList
     * @param stringMapPortIOs
     * @return map of Port IO traffic
     */
    private Map<String, BigInteger> getPortIOTraffic(List<MoverNetStats.Sample> sampleList,
            Map<String, BigInteger> stringMapPortIOs)
    {
        // process Mover stats sample
        for (MoverNetStats.Sample sample : sampleList) {

            // get device traffic stats
            List<MoverNetStats.Sample.DeviceTraffic> deviceTrafficList = sample.getDeviceTraffic();
            Iterator<MoverNetStats.Sample.DeviceTraffic> deviceTrafficIterator = deviceTrafficList.iterator();
            while (deviceTrafficIterator.hasNext()) {
                MoverNetStats.Sample.DeviceTraffic deviceTraffic = deviceTrafficIterator.next();
                // add in + out io traffic
                BigInteger totalIOs = deviceTraffic.getIn().add(deviceTraffic.getOut());

                stringMapPortIOs.put(deviceTraffic.getDevice(), totalIOs);
            }
        }
        return stringMapPortIOs;
    }

    /**
     * Find the port for given portGuid
     * 
     * @param portGuid
     * @param dbClient
     * @return storage port
     */
    private StoragePort findExistingPort(String portGuid, DbClient dbClient) {
        URIQueryResultList results = new URIQueryResultList();
        StoragePort port = null;

        dbClient.queryByConstraint(
                AlternateIdConstraint.Factory.getStoragePortByNativeGuidConstraint(portGuid),
                results);
        Iterator<URI> iter = results.iterator();
        while (iter.hasNext()) {
            StoragePort tmpPort = dbClient.queryObject(StoragePort.class, iter.next());

            if (tmpPort != null && !tmpPort.getInactive()) {
                port = tmpPort;
                _logger.info("found port {}",
                        tmpPort.getNativeGuid() + ":" + tmpPort.getPortName());
                break;
            }
        }
        return port;
    }

    /**
     * Prepare the Port stat information
     * 
     * @param nativeId
     * @param resourceId
     * @param iops
     * @param timeSample
     * @return ipPortStat
     */
    private Stat preparePortStatInfo(String nativeId, URI resourceId,
            long iops, long timeSample) {
        Stat ipPortStat = new Stat();
        ipPortStat.setServiceType(Constants._File);
        ipPortStat.setTimeCollected(timeSample);
        ipPortStat.setResourceId(resourceId);
        ipPortStat.setNativeGuid(nativeId);
        ipPortStat.setTotalIOs(iops);

        return ipPortStat;

    }

    /**
     * Set Port Metrics processor
     * 
     * @param portMetricsProcessor
     */
    public void setPortMetricsProcessor(PortMetricsProcessor portMetricsProcessor) {
        this.portMetricsProcessor = portMetricsProcessor;
    }
}
