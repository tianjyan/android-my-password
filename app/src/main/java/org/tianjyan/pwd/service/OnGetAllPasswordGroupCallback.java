package org.tianjyan.pwd.service;

import org.tianjyan.pwd.model.PasswordGroup;

import java.util.List;

public interface OnGetAllPasswordGroupCallback {
    void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
