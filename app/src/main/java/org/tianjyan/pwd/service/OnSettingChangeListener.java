package org.tianjyan.pwd.service;

import org.tianjyan.pwd.model.SettingKey;

public interface OnSettingChangeListener {
    void onSettingChange(SettingKey key);
}
