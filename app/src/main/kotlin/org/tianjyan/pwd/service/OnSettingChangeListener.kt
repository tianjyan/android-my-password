package org.tianjyan.pwd.service

import org.tianjyan.pwd.model.SettingKey

interface OnSettingChangeListener {
    fun onSettingChange(key: SettingKey)
}