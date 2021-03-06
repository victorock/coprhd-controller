/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.volumecontroller.impl.block.taskcompleter;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.BlockMirror;
import com.emc.storageos.db.client.model.Operation;
import com.emc.storageos.db.client.model.Operation.Status;
import com.emc.storageos.db.client.model.Volume;
import com.emc.storageos.db.client.util.NullColumnValueGetter;
import com.emc.storageos.exceptions.DeviceControllerException;
import com.emc.storageos.services.OperationTypeEnum;
import com.emc.storageos.svcs.errorhandling.model.ServiceCoded;
import com.google.common.base.Joiner;

public class BlockMirrorDetachCompleter extends BlockMirrorTaskCompleter {
    private static final Logger _log = LoggerFactory.getLogger(BlockMirrorCreateCompleter.class);
    public static final String MIRROR_DETACHMENT_MSG = "Mirror %s detached from volume %s";
    public static final String MIRROR_DETACHMENT_FAILED_MSG = "Failed to detach mirror %s for volume %s";

    public BlockMirrorDetachCompleter(URI mirror, String opId) {
        super(BlockMirror.class, mirror, opId);
    }

    public BlockMirrorDetachCompleter(List<URI> mirrorList, String opId) {
        super(BlockMirror.class, mirrorList, opId);
    }

    @Override
    protected void complete(DbClient dbClient, Operation.Status status, ServiceCoded coded)
            throws DeviceControllerException {
        try {
            _log.info("START BlockMirrorDetachCompleter " + status);
            super.complete(dbClient, status, coded);
            List<BlockMirror> mirrorList = dbClient.queryObject(BlockMirror.class, getIds());
            for (BlockMirror mirror : mirrorList) {
                Volume volume = dbClient.queryObject(Volume.class, mirror.getSource());
                if (status == Status.ready) {
                    _log.info("Removing sync details for mirror " + mirror.getId());
                    mirror.setSynchronizedInstance(NullColumnValueGetter.getNullStr());
                    mirror.setSyncState(NullColumnValueGetter.getNullStr());
                    dbClient.persistObject(mirror);
                    _log.info("Removing mirror {} from source volume {}", mirror.getId().toString(),
                            volume.getId().toString());
                    if (volume.getMirrors() != null) {
                        volume.getMirrors().remove(mirror.getId().toString());
                        dbClient.persistObject(volume);
                    }
                }

                switch (status) {
                    case error:
                        dbClient.error(Volume.class, volume.getId(), getOpId(), coded);
                        break;
                    default:
                        dbClient.ready(Volume.class, volume.getId(), getOpId());
                }
                recordBlockMirrorOperation(dbClient, OperationTypeEnum.DETACH_VOLUME_MIRROR,
                        status, eventMessage(status, volume, mirror), mirror, volume);
            }
        } catch (Exception e) {
            _log.error("Failed updating status. BlockMirrorDetach {}, for task " + getOpId(), Joiner.on("\t").join(getIds()), e);
        }
    }

    private String eventMessage(Operation.Status status, Volume volume, BlockMirror mirror) {
        return (Operation.Status.ready == status) ?
                String.format(MIRROR_DETACHMENT_MSG, mirror.getLabel(), volume.getLabel()) :
                String.format(MIRROR_DETACHMENT_FAILED_MSG, mirror.getLabel(), volume.getLabel());
    }
}
