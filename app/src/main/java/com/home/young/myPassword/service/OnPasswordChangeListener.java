package com.home.young.myPassword.service;

import com.home.young.myPassword.model.Password;

public interface OnPasswordChangeListener {
    void onNewPassword(Password password);
    void onDeletePassword(String id);
    void onUpdatePassword(Password password);
}
