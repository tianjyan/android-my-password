package org.tianjyan.pwd.service;

import org.tianjyan.pwd.model.Password;

import java.util.List;

public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String groupName, List<Password> passwords);
}
