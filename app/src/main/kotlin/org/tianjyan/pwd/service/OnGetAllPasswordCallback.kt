package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.Password

interface OnGetAllPasswordCallback {
    fun onGetAllPassword(groupName: String, passwords: List<Password>)
}
