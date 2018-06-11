package org.tianjyan.pwd.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PasswordGroup extends RealmObject {
    @PrimaryKey
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

