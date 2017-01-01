package com.home.young.myPassword.service;

import com.home.young.myPassword.model.Password;

import java.util.List;

public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String groupName, List<Password> passwords);
}
