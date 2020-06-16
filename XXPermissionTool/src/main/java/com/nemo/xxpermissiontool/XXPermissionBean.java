package com.nemo.xxpermissiontool;

import java.io.Serializable;

public class XXPermissionBean implements Serializable {
    static final long serialVersionUID = 1L;
    private String permission = "";
    private String name = "";
    private String tip = "";
    private int drawId = 0;

    public XXPermissionBean() {
    }

    public XXPermissionBean(String permission, String name, String tip, int drawId) {
        this.permission = permission;
        this.name = name;
        this.tip = tip;
        this.drawId = drawId;
    }

    public String getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String getTip() {
        return tip;
    }

    public int getDrawId() {
        return drawId;
    }
}
