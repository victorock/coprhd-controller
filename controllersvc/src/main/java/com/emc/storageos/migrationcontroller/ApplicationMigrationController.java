/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.migrationcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationMigrationController implements MigrationController {
    protected final static String CONTROLLER_SVC = "controllersvc";
    protected final static String CONTROLLER_SVC_VER = "1";

    static final Logger log = LoggerFactory.getLogger(ApplicationMigrationController.class);

	@Override
	public void migrationCreate() {
		log.info("ApplicationMigrationController : Create Migration");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationMigrate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationCommit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationRecover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationRemoveEnv() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationSyncStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrationSyncStop() {
		// TODO Auto-generated method stub
		
	}
}