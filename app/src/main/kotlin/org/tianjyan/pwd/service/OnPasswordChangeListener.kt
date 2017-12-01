package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.Password

interface OnPasswordChangeListener {
    fun onNewPassword(password: Password)
    fun onDeletePassword(id: String)
    fun onUpdatePassword(password: Password)
}
