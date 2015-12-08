/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.db.client.model;

/**
 * FilePolicy will contain the details of file schedule policy.
 * It will hold information about the policyId, policyName, policySchedule, policyExpiration
 * 
 * @author prasaa9
 * 
 */

@Cf("FilePolicy")
public class FilePolicy extends DiscoveredDataObject {

    // Name of the policy
    private String policyName;

    // File policy schedule at
    private String policySchedule;

    // File policy expire at
    private Long policyExpire;

    @Name("policyName")
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
        setChanged("policyName");
    }

    @Name("policySchedule")
    public String getPolicySchedule() {
        return policySchedule;
    }

    public void setPolicySchedule(String policySchedule) {
        this.policySchedule = policySchedule;
        setChanged("policySchedule");
    }

    @Name("policyExpire")
    public Long getPolicyExpire() {
        return policyExpire;
    }

    public void setPolicyExpire(Long policyExpire) {
        this.policyExpire = policyExpire;
        setChanged("policyExpire");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Policy [name=");
        builder.append(policyName);
        builder.append(", schedule at=");
        builder.append(policySchedule);
        builder.append(", expire at=");
        builder.append(policyExpire);
        builder.append("]");
        return builder.toString();
    }
}
