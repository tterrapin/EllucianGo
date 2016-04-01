/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.directory;

public class DirectoryType {
    public String displayName;
    public String internalName;
    public boolean authenticatedOnly;

    public DirectoryType(String displayName, String internalName, boolean authenticatedOnly) {
        this.displayName = displayName;
        this.internalName = internalName;
        this.authenticatedOnly = authenticatedOnly;
    }
}
