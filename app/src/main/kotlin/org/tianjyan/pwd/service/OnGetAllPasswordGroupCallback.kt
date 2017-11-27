package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.PasswordGroup

interface OnGetAllPasswordGroupCallback {
    fun onGetAllPasswordGroup(passwordGroups : List<PasswordGroup>)
}
