/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.fileorchestrationcontroller;

import java.net.URI;
import java.util.List;

import com.emc.storageos.Controller;
import com.emc.storageos.db.client.model.StoragePort;
import com.emc.storageos.model.file.CifsShareACLUpdateParams;
import com.emc.storageos.model.file.FileExportUpdateParams;
import com.emc.storageos.volumecontroller.ControllerException;
import com.emc.storageos.volumecontroller.FileSMBShare;
import com.emc.storageos.volumecontroller.FileShareExport;

public interface FileOrchestrationController extends Controller {
    public final static String FILE_ORCHESTRATION_DEVICE = "file-orchestration";

    /**
     * Creates one or more volumes and executes them.
     * This method is responsible for creating a Workflow and invoking the
     * FileOrchestrationInterface.addStepsForCreateFileSystems
     * 
     * @param fileDescriptors
     *            - The complete list of FileDescriptors received from the API layer.
     *            This defines what FileSharess need to be created, and in which pool each Fileshare should be created.
     * @param taskId
     *            - The overall taskId for the operation.
     * @throws ControllerException
     */
    public abstract void createFileSystems(List<FileDescriptor> fileDescriptors, String taskId)
            throws ControllerException;

    /**
     * Deletes one or more Filesystems
     * 
     * @param fileDescriptors
     * @param taskId
     * @throws ControllerException
     */
    public abstract void deleteFileSystems(List<FileDescriptor> fileDescriptors, String taskId)
            throws ControllerException;

    /**
     * Expands a single fileshare
     * 
     * @param fileDescriptors
     * @param taskId
     * @throws ControllerException
     */
    public abstract void expandFileSystem(List<FileDescriptor> fileDescriptors, String taskId)
            throws ControllerException;

    /**
     * create mirror copies for existing file system
     * This method is responsible for creating a Workflow and invoking the
     * FileOrchestrationInterface.addStepsForCreateFileSystems
     * 
     * @param fileDescriptors
     *            - The complete list of FileDescriptors received from the API layer.
     *            This defines what FileSharess need to be created, and in which pool each Fileshare should be created.
     * @param taskId
     *            - The overall taskId for the operation.
     * @throws ControllerException
     */
    public abstract void createTargetsForExistingSource(String sourceFs, List<FileDescriptor> fileDescriptors, String taskId)
            throws ControllerException;

    /**
     * Create CIFS Share for file system
     * 
     * @param storageSystem
     * @param fileSystem
     * @param smbShare
     * @param taskId
     * @throws ControllerException
     */
    void createCIFSShare(URI storageSystem, URI fileSystem, FileSMBShare smbShare, String taskId)
            throws ControllerException;

    /**
     * Create NFS Exports for file system
     * 
     * @param storage
     * @param fsURI
     * @param exports
     * @param opId
     * @throws ControllerException
     */
    void createNFSExport(URI storage, URI fsURI, List<FileShareExport> exports, String opId)
            throws ControllerException;

    /**
     * Update NFS Export Rules for the FileSystem
     * 
     * @param storage
     * @param fsURI
     * @param param
     * @param opId
     * @throws ControllerException
     */
    void updateExportRules(URI storage, URI fsURI, FileExportUpdateParams param, boolean unmountExport, String opId)
            throws ControllerException;

    /**
     * Update File System CIFS Share ACLs
     * 
     * @param storage
     * @param fsURI
     * @param shareName
     * @param param
     * @param opId
     * @throws ControllerException
     */
    void updateShareACLs(URI storage, URI fsURI, String shareName, CifsShareACLUpdateParams param, String opId)
            throws ControllerException;

    /**
     * Delete FileSystem Share
     * 
     * @param storage
     * @param uri
     * @param fileSMBShare
     * @param task
     * @throws ControllerException
     */
    void deleteShare(URI storage, URI uri, FileSMBShare fileSMBShare, String task) throws ControllerException;

    /**
     * Delete FileSystem Export Rules
     * 
     * @param storage
     * @param uri
     * @param allDirs
     * @param subDirs
     * @param taskId
     * @throws ControllerException
     */
    void deleteExportRules(URI storage, URI uri, boolean allDirs, String subDirs, boolean unmountExport, String taskId)
            throws ControllerException;

    /**
     * Create File System Snapshot
     * 
     * @param storage
     * @param snapshot
     * @param fsURI
     * @param opId
     * @throws ControllerException
     */
    void snapshotFS(URI storage, URI snapshot, URI fsURI, String opId)
            throws ControllerException;

    /**
     * Restore File System Snapshot
     * 
     * @param storage
     * @param fs
     * @param snapshot
     * @param opId
     * @throws ControllerException
     */
    void restoreFS(URI storage, URI fs, URI snapshot, String opId)
            throws ControllerException;

    /**
     * 
     * @param storage
     * @param pool
     * @param uri
     * @param forceDelete
     * @param deleteType
     * @param opId
     * @throws ControllerException
     */
    void deleteSnapshot(URI storage, URI pool, URI uri, boolean forceDelete, String deleteType, String opId) throws ControllerException;

    /**
     * 
     * @param storage
     * @param uri
     * @param shareName
     * @param taskId
     * @throws ControllerException
     */
    void deleteShareACLs(URI storage, URI uri, String shareName, String taskId) throws ControllerException;

    /**
     * 
     * @param fsURI
     *            - URI of the Source File System that has to be failed over.
     * @param nfsPort
     *            - NFS Export StoragePort for target File System
     * @param cifsPort
     *            - CIFS Share StoragePort for target File System
     * @param replicateConfiguration
     * @param taskId
     * @throws ControllerException
     */

    void failoverFileSystem(URI fsURI, StoragePort nfsPort, StoragePort cifsPort, boolean replicateConfiguration, String taskId)
            throws ControllerException;

    /**
     * Failback to Source FS from Target FS.
     * 
     * @param fsURI
     *            - URI of the Source File System that has to be failed back from target.
     * @param nfsPort
     *            - NFS Export StoragePort for source File System
     * @param cifsPort
     *            - CIFS Share StoragePort for source File System
     * @param replicateConfiguration
     * @param taskId
     * @throws ControllerException
     */
    void failbackFileSystem(URI fsURI, StoragePort nfsPort, StoragePort cifsPort, boolean replicateConfiguration, String taskId)
            throws ControllerException;
}
