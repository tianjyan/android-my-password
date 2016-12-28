package com.home.young.myPassword.service;

import java.util.List;

import com.home.young.myPassword.model.PasswordGroup;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnGetAllPasswordGroupCallback {
    void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
