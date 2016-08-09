/*
 * Copyright (c) 2015 EMC Software LLC
 * All Rights Reserved
 */
package com.emc.sa.service.vipr.block;

import static com.emc.sa.service.ServiceParams.LINKED_SNAPSHOT_COPYMODE;
import static com.emc.sa.service.ServiceParams.LINKED_SNAPSHOT_COUNT;
import static com.emc.sa.service.ServiceParams.LINKED_SNAPSHOT_NAME;
import static com.emc.sa.service.ServiceParams.NAME;
import static com.emc.sa.service.ServiceParams.READ_ONLY;
import static com.emc.sa.service.ServiceParams.STORAGE_TYPE;
import static com.emc.sa.service.ServiceParams.TYPE;
import static com.emc.sa.service.ServiceParams.VOLUMES;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.emc.sa.asset.providers.BlockProvider;
import com.emc.sa.engine.ExecutionTask;
import com.emc.sa.engine.ExecutionUtils;
import com.emc.sa.engine.ViPRTaskMonitor;
import com.emc.sa.engine.bind.Param;
import com.emc.sa.engine.service.Service;
import com.emc.sa.service.vipr.ViPRService;
import com.emc.sa.service.vipr.block.tasks.CreateBlockSnapshot;
import com.emc.sa.service.vipr.block.tasks.CreateBlockSnapshotSession;
import com.emc.sa.service.vipr.block.tasks.DeactivateBlockSnapshot;
import com.emc.storageos.db.client.model.StringSet;
import com.emc.storageos.db.client.model.uimodels.ScheduledEvent;
import com.emc.storageos.model.DataObjectRestRep;
import com.emc.storageos.model.block.BlockObjectRestRep;
import com.emc.storageos.model.block.VolumeDeleteTypeEnum;
import com.emc.vipr.client.Task;
import com.emc.vipr.client.Tasks;

@Service("CreateBlockSnapshot")
public class CreateBlockSnapshotService extends ViPRService {

    @Param(value = STORAGE_TYPE, required = false)
    protected String storageType;

    @Param(VOLUMES)
    protected List<String> volumeIds;

    @Param(NAME)
    protected String nameParam;

    @Param(value = TYPE, required = false)
    protected String type;

    @Param(value = READ_ONLY, required = false)
    protected Boolean readOnly;
    
    @Param(value = LINKED_SNAPSHOT_NAME, required = false)
    protected String linkedSnapshotName;
    
    @Param(value = LINKED_SNAPSHOT_COUNT, required = false)
    protected Integer linkedSnapshotCount;
        
    @Param(value = LINKED_SNAPSHOT_COPYMODE, required = false)
    protected String linkedSnapshotCopyMode;

    private List<BlockObjectRestRep> volumes;

    @Override
    public void precheck() {
        if (ConsistencyUtils.isVolumeStorageType(storageType)) {
            volumes = new ArrayList<>();
            volumes = BlockStorageUtils.getBlockResources(uris(volumeIds));
        }
            
        if (BlockProvider.SNAPSHOT_SESSION_TYPE_VALUE.equals(type)
                || BlockProvider.CG_SNAPSHOT_SESSION_TYPE_VALUE.equals(type)) {               
            if (linkedSnapshotName != null && !linkedSnapshotName.isEmpty()) {
                // If trying to create a Snapshot Session and the optional linkedSnapshotName 
                // is populated, make sure that linkedSnapshotCount > 0.
                if (linkedSnapshotCount == null || linkedSnapshotCount.intValue() <= 0) {
                    ExecutionUtils.fail("failTask.CreateBlockSnapshot.linkedSnapshotCount.precheck", new Object[] {}, new Object[] {});
                }
                // Ensure that copy mode is selected
                if (linkedSnapshotCopyMode == null
                        || !(BlockProvider.LINKED_SNAPSHOT_COPYMODE_VALUE.equals(linkedSnapshotCopyMode)
                                || BlockProvider.LINKED_SNAPSHOT_NOCOPYMODE_VALUE.equals(linkedSnapshotCopyMode))) {
                    ExecutionUtils.fail("failTask.CreateBlockSnapshot.linkedSnapshotCopyMode.precheck", new Object[] {}, new Object[] {});
                }
            }
        }
    }

    @Override
    public void execute() {
        checkRetainedSnapshots();
        
    	Tasks<? extends DataObjectRestRep> tasks;
        if (ConsistencyUtils.isVolumeStorageType(storageType)) {
            for (BlockObjectRestRep volume : volumes) {
                if (BlockProvider.SNAPSHOT_SESSION_TYPE_VALUE.equals(type)) {
                    tasks = execute(new CreateBlockSnapshotSession(volume.getId(), nameParam, 
                                                                    linkedSnapshotName, linkedSnapshotCount, linkedSnapshotCopyMode));
                } else {
                    tasks = execute(new CreateBlockSnapshot(volume.getId(), type, nameParam, readOnly));
                    
                }
                addAffectedResources(tasks);
                addRetainedSnapshots(tasks);
            }
        } else {
            for (String consistencyGroupId : volumeIds) {
                if (BlockProvider.CG_SNAPSHOT_SESSION_TYPE_VALUE.equals(type)) {
                    tasks = ConsistencyUtils.createSnapshotSession(uri(consistencyGroupId), nameParam, 
                                                                    linkedSnapshotName, linkedSnapshotCount, linkedSnapshotCopyMode);
                } else {
                    tasks = ConsistencyUtils.createSnapshot(uri(consistencyGroupId), nameParam, readOnly);
                }
                addAffectedResources(tasks);
            }
        }
    }

    private void addRetainedSnapshots(Tasks<? extends DataObjectRestRep> tasks) {
    	ScheduledEvent event = ExecutionUtils.currentContext().getScheduledEvent();
        if (event == null) {
        	return;
        }
    	if (tasks != null) {
            for (Task<? extends DataObjectRestRep> task : tasks.getTasks()) {
                URI resourceId = task.getResourceId();
                info("Get snapshot id %s", resourceId);
                if (resourceId != null) {
                	StringSet retainedCopies = event.getRetainedCopies();
                	if (retainedCopies == null) {
                		retainedCopies = new StringSet();
                		event.setRetainedCopies(retainedCopies);
                	}
                	retainedCopies.add(resourceId.toString());
                	info("Save %s", event.getId());
                	getModelClient().save(event);
                }
            }
        }
    }
    
    private void checkRetainedSnapshots() {
    	ScheduledEvent event = ExecutionUtils.currentContext().getScheduledEvent();
        if (event == null) {
        	return;
        }
        Integer maxNumOfCopies = event.getMaxNumOfRetainedCopies();
        if (maxNumOfCopies == null) {
        	return;
        }
        info("Max number of copies %d", maxNumOfCopies);
        StringSet retainedCopies = event.getRetainedCopies();
        if (retainedCopies != null && retainedCopies.size() > maxNumOfCopies) {
        	String snapshotId = retainedCopies.iterator().next();//TODO - find the old one
        	info("Remove snapshot %s", snapshotId);
        	Tasks<? extends DataObjectRestRep> task = execute(new DeactivateBlockSnapshot(uri(snapshotId), VolumeDeleteTypeEnum.FULL));
        	task.firstTask().doTaskWait();
        	if (task.firstTask().isComplete()) {
	        	retainedCopies.remove(snapshotId);
	        	getModelClient().save(event);
        	} else {
        		error("Deactivate previous snapshot error");
        	}
        }
    }
}
