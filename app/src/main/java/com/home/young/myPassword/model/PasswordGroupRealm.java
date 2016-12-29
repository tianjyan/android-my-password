package com.home.young.myPassword.model;

import io.realm.RealmObject;

public class PasswordGroupRealm extends RealmObject {
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "PasswordGroup [groupName=" + groupName + "]";
    }
}

