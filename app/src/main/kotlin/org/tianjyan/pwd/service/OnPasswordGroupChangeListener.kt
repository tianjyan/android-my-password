package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.PasswordGroup

interface OnPasswordGroupChangeListener {
    fun onNewPasswordGroup(passwordGroup : PasswordGroup)
    fun onDeletePasswordGroup(passwordGroupName : String)
    fun onUpdateGroupName(oldGroupName : String, newGroupName : String)
}