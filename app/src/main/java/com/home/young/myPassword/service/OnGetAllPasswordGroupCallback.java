package com.home.young.myPassword.service;

import com.home.young.myPassword.model.PasswordGroup;

import java.util.List;

public interface OnGetAllPasswordGroupCallback {
    void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
