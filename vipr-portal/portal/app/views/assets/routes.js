var routes = {
  Security_authenticated: #{jsAction @security.Security.authenticated() /},
  Notifications_notifications: #{jsAction @Notifications.notifications(':lastUpdated', ':count') /},
  Notifications_showTarget: #{jsAction @Notifications.showTarget(':id') /},
  Orders_receipt: #{jsAction @catalog.Orders.receipt(':orderId') /},
  Orders_receiptContent: #{jsAction @catalog.Orders.receiptContent(':orderId', ':lastUpdated') /},
  Dashboard_index: #{jsAction @Dashboard.index() /},
  
  ServiceCatalog_view: #{jsAction @catalog.ServiceCatalog.view() /},
  ServiceCatalog_edit: #{jsAction @catalog.EditCatalog.edit() /},
  ServiceCatalog_showService: #{jsAction @catalog.Services.showForm(':serviceId') /},
  ServiceCatalog_createService: #{jsAction @catalog.EditCatalog.createService(':parentId', ':fromId') /},
  ServiceCatalog_editService: #{jsAction @catalog.EditCatalog.editService(':serviceId', ':fromId') /},
  ServiceCatalog_copyService: #{jsAction @catalog.EditCatalog.copyService(':serviceId', ':fromId') /},
  ServiceCatalog_deleteService: #{jsAction @catalog.EditCatalog.deleteService(':serviceId') /},
  ServiceCatalog_createCategory: #{jsAction @catalog.EditCatalog.createCategory(':parentId', ':fromId') /},
  ServiceCatalog_editCategory: #{jsAction @catalog.EditCatalog.editCategory(':categoryId', ':fromId') /},
  ServiceCatalog_deleteCategory: #{jsAction @catalog.EditCatalog.deleteCategory(':categoryId') /},
  ServiceCatalog_restoreCatalog: #{jsAction @catalog.EditCatalog.restoreCatalog() /},
  ServiceCatalog_updateCatalog: #{jsAction @catalog.EditCatalog.updateCatalog() /},
  ServiceCatalog_docCategory: #{jsAction @catalog.ServiceCatalog.docCategory(':categoryId') /},
  CatalogImages_view: #{jsAction @catalog.CatalogImages.view(':image')/},
  EditCatalog_images: #{jsAction @catalog.EditCatalog.imagesJson()/},
  ServiceCatalog_CreateCustomService: #{jsAction @catalog.EditCatalog.createCustomService() /},
  
  ExecutionWindows_events: #{jsAction @catalog.ExecutionWindows.events(':timezoneOffsetInMinutes') /},
  ExecutionWindows_create: #{jsAction @catalog.ExecutionWindows.create(':start', ':end', ':timezoneOffsetInMinutes') /},
  ExecutionWindows_edit: #{jsAction @catalog.ExecutionWindows.edit(':id', ':timezoneOffsetInMinutes') /},
  ExecutionWindows_move: #{jsAction @catalog.ExecutionWindows.move(':id', ':start', ':timezoneOffsetInMinutes')/},
  ExecutionWindows_resize: #{jsAction @catalog.ExecutionWindows.resize(':id', ':start', ':end', ':timezoneOffsetInMinutes')/},
  ExecutionWindows_save: #{jsAction @catalog.ExecutionWindows.save()/},
  ExecutionWindows_delete: #{jsAction @catalog.ExecutionWindows.delete()/},

  Tasks_activeTaskCount: #{jsAction @Tasks.getActiveCount() /},
  Tasks_recentTasks: #{jsAction @Tasks.getRecentTasks() /},
  Tasks_taskDetailsJson: #{jsAction @Tasks.detailsJson(':taskId')/},
  Tasks_taskDetails: #{jsAction @Tasks.details(':taskId')/},
  Tasks_countSummary: #{jsAction @Tasks.getCountSummary(':tenantId') /},
  
  Events_countSummary: #{jsAction @Events.getCountSummary(':tenantId') /},
  Events_pendingEventCount: #{jsAction @Events.getPendingCount() /},
  Events_details: #{jsAction @Events.details(':id') /},

  BlockVolumes_volume: #{jsAction @resources.BlockVolumes.volume(':volumeId') /},
  BlockExportGroups_exportGroup: #{jsAction @resources.BlockExportGroups.exportGroup(':exportGroupId')/},
  FileSystems_fileSystem: #{jsAction @resources.FileSystems.fileSystem(':fileSystemId')/}, 
  FileSystems_validateQuotaSize: #{jsAction @resources.FileSystems.validateQuotaSize(':quotaSize')/},
  FileSnapshots_snapshot: #{jsAction @resources.FileSnapshots.snapshot(':snapshotId')/},
  BlockSnapshots_snapshotDetails: #{jsAction @resources.BlockSnapshots.snapshotDetails(':snapshotId')/},
  Hosts_edit: #{jsAction @compute.Hosts.edit(':id')/},
  HostClusters_edit: #{jsAction @compute.HostClusters.edit(':id')/},
  StorageSystems_edit: #{jsAction @arrays.StorageSystems.edit(':id')/},
  StorageSystems_list: #{jsAction @arrays.StorageSystems.listJson()/},
  StorageSystems_itemDetails: #{jsAction @arrays.StorageSystems.arrayPoolsJson(':id')/},
  StorageSystems_itemsJson: #{jsAction @arrays.StorageSystems.itemsJson(':ids')/},
  StorageProviders_itemsJson: #{jsAction @arrays.StorageProviders.itemsJson(':ids')/},
  StorageProviders_discoveryCheckJson: #{jsAction @arrays.StorageProviders.discoveryCheckJson(':ids')/},
  StorageSystems_discoveryCheckJson: #{jsAction @arrays.StorageSystems.discoveryCheckJson(':ids')/},
  StorageProviders_getAllFlashStorageSystemsList: #{jsAction @arrays.StorageProviders.getAllFlashStorageSystemsList(':ids')/},
  StorageSystems_getProjectsForNas: #{jsAction @arrays.StorageSystems.getProjectsForNas()/},
  SanSwitches_edit: #{jsAction @arrays.SanSwitches.edit(':id')/},
  SanSwitches_list: #{jsAction @arrays.SanSwitches.listJson()/},
  DataProtectionSystems_edit: #{jsAction @arrays.DataProtectionSystems.edit(':id')/},
  StorageProviders_edit: #{jsAction @arrays.StorageProviders.edit(':id')/},
  VCenter_edit: #{jsAction @compute.VCenters.edit(':id')/},

  BlockVirtualPools_listHighAvailabilityVirtualArraysJson: #{jsAction @arrays.BlockVirtualPools.listHighAvailabilityVirtualArraysJson() /},
  BlockVirtualPools_listHighAvailabilityVirtualPoolsJson: #{jsAction @arrays.BlockVirtualPools.listHighAvailabilityVirtualPoolsJson() /},
  BlockVirtualPools_listAutoTierPoliciesJson: #{jsAction @arrays.BlockVirtualPools.listAutoTierPoliciesJson() /},
  BlockVirtualPools_listContinuousCopyVirtualPoolsJson: #{jsAction @arrays.BlockVirtualPools.listContinuousCopyVirtualPoolsJson() /},
  BlockVirtualPools_listVirtualArrayAttributesJson: #{jsAction @arrays.BlockVirtualPools.listVirtualArrayAttributesJson() /},
  BlockVirtualPools_listRecoverPointVirtualArraysJson: #{jsAction @arrays.BlockVirtualPools.listRecoverPointVirtualArraysJson() /},
  BlockVirtualPools_listRecoverPointVirtualPoolsJson: #{jsAction @arrays.BlockVirtualPools.listRecoverPointVirtualPoolsJson() /},
  BlockVirtualPools_listRecoverPointJournalVPoolsJson: #{jsAction @arrays.BlockVirtualPools.listRecoverPointJournalVPoolsJson() /},
  BlockVirtualPools_listSourceRpJournalVPoolsJson:#{jsAction @arrays.BlockVirtualPools.listSourceRpJournalVPoolsJson() /},
  BlockVirtualPools_listHaRpJournalVPoolsJson:#{jsAction @arrays.BlockVirtualPools.listHaRpJournalVPoolsJson() /},
  BlockVirtualPools_validateRecoverPointCopy: #{jsAction @arrays.BlockVirtualPools.validateRecoverPointCopy() /},

  SystemHealth_renderNodeDetailsJson: #{jsAction @SystemHealth.renderNodeDetailsJson() /},
  SystemHealth_listDiagnosticsJson: #{jsAction @SystemHealth.listDiagnosticsJson() /},
  SystemHealth_logsJson: #{jsAction @SystemHealth.logsJson() /},
  SystemHealth_logs: #{jsAction @SystemHealth.logs() /},
  SystemHealth_download: #{jsAction @SystemHealth.download() /},

  AuditLog_list: #{jsAction @infra.AuditLog.list() /},
  AuditLog_download: #{jsAction @infra.AuditLog.download() /},

  FileSystems_fileSystemExportsJson: #{jsAction @resources.FileSystems.fileSystemExportsJson() /},
  FileSystems_save: #{jsAction @resources.FileSystems.save() /},
  FileSystems_fileSystemQuotaJson: #{jsAction @resources.FileSystems.fileSystemQuotaJson() /},
  FileSystems_getScheculePolicies: #{jsAction @resources.FileSystems.getScheculePolicies() /},

  FileSnapshots_fileSnapshotExportsJson: #{jsAction @resources.FileSnapshots.fileSnapshotExportsJson() /},
  FileSnapshots_save: #{jsAction @resources.FileSnapshots.save() /},

  ComputeSystem_edit: #{jsAction @compute.ComputeSystems.edit(':id  ') /},
  
  Common_clusterInfoJson: #{jsAction @Common.clusterInfoWithRoleCheckJson() /},

  Setup_license: #{jsAction @Setup.isLicensed() /},
  Setup_initialSetup: #{jsAction @Setup.isInitialSetupComplete() /},
  Projects_list: #{jsAction @tenant.Projects.listJson() /},
  BlockVirtualPools_list: #{jsAction @arrays.BlockVirtualPools.listJson() /},
  BlockVirtualPools_pools: #{jsAction @arrays.BlockVirtualPools.listStoragePoolsbyIdJson(':id') /},
  FileVirtualPools_list: #{jsAction @arrays.FileVirtualPools.listJson() /},
  VirtualArrays_list: #{jsAction @VirtualArrays.listJson() /},
  VirtualArrays_pools: #{jsAction @VirtualArrays.storagePoolsJson(':id') /},
  Networks_getDisconnectedStorage: #{jsAction @arrays.Networks.getDisconnectedStorage(':ids') /},
  VirtualArrays_getDisconnectedStorage: #{jsAction @VirtualArrays.getDisconnectedStorage(':ids') /},
  VirtualPools_checkDisconnectedStoragePools: #{jsAction @arrays.BlockVirtualPools.checkDisconnectedStoragePools(':ids') /}

};
