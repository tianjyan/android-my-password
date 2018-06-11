package org.tianjyan.pwd.service;

import org.tianjyan.pwd.model.Password;

public interface OnPasswordChangeListener {
    void onNewPassword(Password password);
    void onDeletePassword(String id);
    void onUpdatePassword(Password password);
}
