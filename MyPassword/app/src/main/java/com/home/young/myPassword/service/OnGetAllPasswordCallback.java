package com.home.young.myPassword.service;

import java.util.List;

import com.home.young.myPassword.model.Password;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String groupName, List<Password> passwords);
}
