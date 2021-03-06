/*
 * Copyright (c) 2013 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.coordinator.client.model;

import com.emc.storageos.svcs.errorhandling.resources.APIException;

/**
 * Product name class
 * 
 * Product name is initialized by bean file. It is used
 * SoftwareVersion and the whole upgrade machinery depends on this name
 * e.g. "vipr"
 */
public class ProductName {
    private static String _name;

    protected ProductName() {
    }

    public void setName(String name) {
        // This method is only called in test cases and when Spring initialization, safe to suppress
        _name = name; // NOSONAR("findbugs:ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    }

    public static String getName() {
        if (_name == null) {
            throw APIException.internalServerErrors.targetIsNullOrEmpty("product name");
        }
        return _name;
    }
}
