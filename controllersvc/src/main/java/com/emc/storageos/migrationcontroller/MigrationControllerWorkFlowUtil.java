package com.emc.storageos.migrationcontroller;

import static com.emc.storageos.migrationcontroller.MigrationControllerUtils.getDataObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.BlockConsistencyGroup;
import com.emc.storageos.db.client.model.BlockConsistencyGroup.Types;
import com.emc.storageos.db.client.model.Host;
import com.emc.storageos.db.client.model.Migration;
import com.emc.storageos.db.client.model.StorageSystem;
import com.emc.storageos.db.client.model.util.BlockConsistencyGroupUtils;
import com.emc.storageos.svcs.errorhandling.resources.InternalException;
import com.emc.storageos.volumecontroller.impl.block.BlockDeviceController;
import com.emc.storageos.workflow.Workflow;

public class MigrationControllerWorkFlowUtil {
    private static final Logger _log = LoggerFactory.getLogger(MigrationControllerWorkFlowUtil.class);
    private DbClient _dbClient;
    // Workflow controller method names.
    private static final String MIGRATE_GENERAL_VOLUME_METHOD_NAME = "migrateGeneralVolume";
    private static final String RB_MIGRATE_GENERAL_VOLUME_METHOD_NAME = "rollbackMigrateGeneralVolume";
    private static final String COMMIT_MIGRATION_METHOD_NAME = "commitMigration";
    private static final String RB_COMMIT_MIGRATION_METHOD_NAME = "rollbackCommitMigration";
    private static final String DELETE_MIGRATION_SOURCES_METHOD = "deleteMigrationSources";
    private static final String MIGRATION_VOLUME_EXPORT_METHOD_NAME = "migrateVolumeExport";
    private static final String RB_MIGRATION_VOLUME_EXPORT_METHOD_NAME = "rollbackMigrateVolumeExport";
    private static final String HOST_MIGRATION_VOLUME_EXPORT_METHOD_NAME = "hostMigrateVolumeExport";
    private static final String RB_HOST_MIGRATION_VOLUME_EXPORT_METHOD_NAME = "rollbackHostMigrateVolumeExport";
    // Workflow step identifiers
    private static final String MIGRATION_CREATE_STEP = "migrate";
    private static final String MIGRATION_COMMIT_STEP = "commit";
    private static final String MIGRATION_VOLUME_EXPORT_STEP = "exportVolume";
    private static final String HOST_MIGRATION_VOLUME_EXPORT_STEP = "hostExportVolume";
    private static final String DELETE_MIGRATION_SOURCES_STEP = "deleteSources";

    public MigrationControllerWorkFlowUtil() {

    }
    public String createWorkflowStepsForBlockVolumeExport(Workflow workflow, String waitFor,
            List<URI> volumeURIs, URI hostURI, String taskId)
            throws InternalException {
        Host host = getDataObject(Host.class, hostURI, _dbClient);

        String stepId = workflow.createStepId();
        Workflow.Method migrationExportOrchestrationExecuteMethod = new Workflow.Method(HOST_MIGRATION_VOLUME_EXPORT_METHOD_NAME);
        Workflow.Method migrationExportOrchestrationExecuteRollbackMethod = new Workflow.Method(RB_HOST_MIGRATION_VOLUME_EXPORT_METHOD_NAME);

        workflow.createStep(HOST_MIGRATION_VOLUME_EXPORT_STEP, "Create host Export group orchestration subtask for host migration",
                waitFor, host.getId(), host.getSystemType(), false, this.getClass(),
                migrationExportOrchestrationExecuteMethod, migrationExportOrchestrationExecuteRollbackMethod, stepId);

        stepId = workflow.createStepId();
        Workflow.Method exportOrchestrationExecuteMethod = new Workflow.Method(MIGRATION_VOLUME_EXPORT_METHOD_NAME,
                hostURI, volumeURIs);

        Workflow.Method exportOrchestrationExecutionRollbackMethod =
                new Workflow.Method(RB_MIGRATION_VOLUME_EXPORT_METHOD_NAME, workflow.getWorkflowURI(), stepId);

        waitFor = workflow.createStep(MIGRATION_VOLUME_EXPORT_STEP, "Create export group orchestration subtask for host",
                HOST_MIGRATION_VOLUME_EXPORT_STEP, hostURI, host.getSystemType(), false, this.getClass(),
                exportOrchestrationExecuteMethod, exportOrchestrationExecutionRollbackMethod, stepId);

        return waitFor;

    }


    public String createWorkflowStepsForMigrateGeneralVolumes(Workflow workflow, URI hostURI,
            URI generalVolumeURI, List<URI> targetVolumeURIs, URI newVpoolURI, URI newVarrayURI,
            Map<URI, URI> migrationsMap, String waitFor) throws InternalException {
        try {
            Host host = getDataObject(Host.class, hostURI, _dbClient);
            // Now make a migration Step for each passed target to which data
            // for the passed virtual volume will be migrated. The migrations
            // will be done from this controller.
            Iterator<URI> targetVolumeIter = targetVolumeURIs.iterator();
            while (targetVolumeIter.hasNext()) {
                URI targetVolumeURI = targetVolumeIter.next();
                _log.info("Target volume is {}", targetVolumeURI);
                URI migrationURI = migrationsMap.get(targetVolumeURI);
                _log.info("Migration is {}", migrationURI);
                String stepId = workflow.createStepId();
                _log.info("Migration opId is {}", stepId);
                Workflow.Method generalMigrationExecuteMethod = new Workflow.Method(
                        MIGRATE_GENERAL_VOLUME_METHOD_NAME, hostURI, generalVolumeURI,
                        targetVolumeURI, migrationURI, newVarrayURI);
                Workflow.Method generalMigrationRollbackMethod = new Workflow.Method(
                        RB_MIGRATE_GENERAL_VOLUME_METHOD_NAME, hostURI, migrationURI, stepId);
                _log.info("Creating workflow migration step");
                workflow.createStep(MIGRATION_CREATE_STEP, String.format(
                        "storagesystem %s migrating volume", host.getId().toString()),
                        waitFor, hostURI, host.getSystemType(),
                        getClass(), generalMigrationExecuteMethod, generalMigrationRollbackMethod, stepId);
                _log.info("Created workflow migration step");
            }
            return MIGRATION_CREATE_STEP;
        } catch (Exception e) {
            throw MigrationControllerException.exceptions.addStepsForChangeVirtualPoolFailed(e);
        }

    }

    public String createWorkflowStepsForCommitMigration(Workflow workflow, URI hostURI,
            URI generalVolumeURI, Map<URI, URI> migrationsMap, String waitFor)
            throws InternalException {
        try {
            Host host = getDataObject(Host.class, hostURI, _dbClient);
            // Once the migrations complete, we will commit the migrations.
            // So, now we create the steps to commit the migrations.
            List<URI> migrationURIs = new ArrayList<URI>(migrationsMap.values());
            Iterator<URI> migrationsIter = migrationsMap.values().iterator();
            while (migrationsIter.hasNext()) {
                URI migrationURI = migrationsIter.next();
                _log.info("Migration is {}", migrationURI);
                Migration migration = getDataObject(Migration.class, migrationURI, _dbClient);

                Boolean rename = Boolean.TRUE;
                if (migration.getSource() == null) {
                    rename = Boolean.FALSE;
                }
                _log.info("Added migration source {}", migration.getSource());
                String stepId = workflow.createStepId();
                _log.info("Commit operation id is {}", stepId);
                Workflow.Method commitMigrationExecuteMethod = new Workflow.Method(
                        COMMIT_MIGRATION_METHOD_NAME, hostURI, generalVolumeURI,
                        migrationURI, rename);
                Workflow.Method commitMigrationRollbackMethod = new Workflow.Method(
                        RB_COMMIT_MIGRATION_METHOD_NAME, migrationURIs, stepId);
                _log.info("Creating workflow step to commit migration");
                waitFor = workflow.createStep(MIGRATION_COMMIT_STEP, String.format(
                        "storage sysmtem %s committing volume migration",
                        host.getId().toString()),
                        waitFor, hostURI,
                        host.getSystemType(), getClass(), commitMigrationExecuteMethod,
                        commitMigrationRollbackMethod, stepId);
                _log.info("Created workflow step to commit migration");
            }
            return waitFor;
        } catch (Exception e) {
            throw MigrationControllerException.exceptions.addStepsForChangeVirtualPoolFailed(e);
        }
    }

    public String createWorkflowStepsForDeleteMigrationSource(Workflow workflow, URI hostURI,
            URI generalVolumeURI, URI newVpoolURI, URI newVarrayURI, Map<URI, URI> migrationsMap,
            String waitFor) throws InternalException {
        try {
            Host host = getDataObject(Host.class, hostURI, _dbClient);
            List<URI> migrationSources = new ArrayList<URI>();
            Iterator<URI> migrationsIter = migrationsMap.values().iterator();
            while (migrationsIter.hasNext()) {
                URI migrationURI = migrationsIter.next();
                _log.info("Migration is {}", migrationURI);
                Migration migration = getDataObject(Migration.class, migrationURI, _dbClient);
                if (migration.getSource() != null) {
                    migrationSources.add(migration.getSource());
                }
            }
            String stepId = workflow.createStepId();
            Workflow.Method deleteMigrationExecuteMethod = new Workflow.Method(
                    DELETE_MIGRATION_SOURCES_METHOD, hostURI, generalVolumeURI,
                    newVpoolURI, newVarrayURI, migrationSources);
            workflow.createStep(DELETE_MIGRATION_SOURCES_STEP,
                    String.format("Creating workflow to delete migration sources"),
                    waitFor, hostURI, host.getSystemType(),
                    getClass(), deleteMigrationExecuteMethod, null, stepId);
            _log.info("Created workflow step to create sub workflow for source deletion");

            return DELETE_MIGRATION_SOURCES_STEP;
        } catch (Exception e) {
            throw MigrationControllerException.exceptions.addStepsForChangeVirtualPoolFailed(e);
        }

    }

    public String createWorkflowStepsForDeleteConsistencyGroup(Workflow workflow, URI cgURI,
            List<URI> localSystemsToRemoveCG, String lastStep) throws InternalException {

        BlockConsistencyGroup cg = getDataObject(BlockConsistencyGroup.class, cgURI, _dbClient);
        if (cg.checkForType(Types.LOCAL)) {
            _log.info("CG {} has local type", cgURI);
            // If any of the VPLEX volumes involved in the vpool change
            // is in a VPLEX CG with corresponding local CGs for the backend
            // volumes, then it is required that all VPLEX volumes in the
            // CG are part of the vpool change. If the backend volumes are being
            // migrated to a new storage system, then we need to add a step
            // to delete the local CG.
            boolean localCGDeleted = false;
            List<URI> localSystemURIs = BlockConsistencyGroupUtils.getLocalSystems(cg, _dbClient);
            for (URI localSystemURI : localSystemURIs) {
                _log.info("CG exists on local system {}", localSystemURI);
                if (localSystemsToRemoveCG.contains(localSystemURI)) {
                    localCGDeleted = true;
                    _log.info("Adding step to remove CG on local system {}", localSystemURI);
                    StorageSystem localSystem = getDataObject(StorageSystem.class, localSystemURI, _dbClient);
                    Workflow.Method deleteCGMethod = new Workflow.Method(
                            "deleteConsistencyGroup", localSystemURI, cgURI, Boolean.FALSE);
                    workflow.createStep("deleteLocalCG", String.format(
                            "Delete consistency group from storage system: %s", localSystemURI),
                            lastStep, localSystemURI, localSystem.getSystemType(),
                            BlockDeviceController.class, deleteCGMethod, null,
                            null);
                }
            }
            if (localCGDeleted) {
                lastStep = "deleteLocalCG";
            }
        }
        return lastStep;
    }
}