package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.Password

interface OnGetPasswordCallback {
    fun onGetPassword(password : Password)
}