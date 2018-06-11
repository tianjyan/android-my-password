package org.tianjyan.pwd.service;

import org.tianjyan.pwd.model.PasswordGroup;

public interface OnPasswordGroupChangeListener {
    void onNewPasswordGroup(PasswordGroup passwordGroup);
    void onDeletePasswordGroup(String passwordGroupName);
    void onUpdateGroupName(String oldGroupName, String newGroupName);
}
