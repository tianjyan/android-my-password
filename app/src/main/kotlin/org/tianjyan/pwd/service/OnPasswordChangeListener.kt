package org.tianjyan.pwd.service

import com.home.young.myPassword.model.Password

interface OnPasswordChangeListener {
    fun onNewPassword(password: Password)
    fun onDeletePassword(id: String)
    fun onUpdatePassword(password: Password)
}
