package com.home.young.myPassword.service;

import com.home.young.myPassword.model.Password;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnPasswordChangeListener {
    void onNewPassword(Password password);
    void onDeletePassword(int password);
    void onUpdatePassword(Password password);

}
