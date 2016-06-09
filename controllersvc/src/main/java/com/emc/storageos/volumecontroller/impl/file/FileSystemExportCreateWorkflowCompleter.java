package com.emc.storageos.volumecontroller.impl.file;

import java.net.URI;

import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.Operation;
import com.emc.storageos.svcs.errorhandling.model.ServiceCoded;

public class FileSystemExportCreateWorkflowCompleter extends FileWorkflowCompleter {

    private static final long serialVersionUID = 1L;

    public FileSystemExportCreateWorkflowCompleter(URI fileUri, String task) {
        super(fileUri, task);
    }

    @Override
    protected void complete(DbClient dbClient, Operation.Status status, ServiceCoded serviceCoded) {
        super.complete(dbClient, status, serviceCoded);
    }
}
