package com.home.young.myPassword.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Looper;

import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.SettingKey;
import com.home.young.myPassword.service.OnPasswordChangeListener;
import com.home.young.myPassword.service.OnPasswordGroupChangeListener;
import com.home.young.myPassword.service.OnSettingChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application implements OnSharedPreferenceChangeListener {
    private SharedPreferences mSharedPreferences;
    private Map<SettingKey, List<OnSettingChangeListener>> onSettingChangeListenerMap = new HashMap<>();
    private PasswordDBRealm mPasswordDBRealm;

    private List<OnPasswordChangeListener> onPasswordListeners = new ArrayList<>();
    private List<OnPasswordGroupChangeListener> onPasswordGroupListeners = new ArrayList<>();

    @Override
    public void onCreate(){
        super.onCreate();
        loadSettings();
        final String key = getSetting(SettingKey.KEY, "");
        mPasswordDBRealm = new PasswordDBRealm(getApplicationContext(), key);
    }

    private  void loadSettings(){
        mSharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void registOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
        checkUIThread();

        List<OnSettingChangeListener> onSettingChangeListeners;
        if (onSettingChangeListenerMap.containsKey(key)) {
            onSettingChangeListeners = onSettingChangeListenerMap.get(key);
        } else {
            onSettingChangeListeners = new ArrayList<>();
            onSettingChangeListenerMap.put(key, onSettingChangeListeners);
        }
        onSettingChangeListeners.add(onSettingChangeListener);
    }

    public void unregistOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
        checkUIThread();
        if (onSettingChangeListenerMap.containsKey(key)) {
            List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(key);
            onSettingChangeListeners.remove(onSettingChangeListener);
            if (onSettingChangeListeners.size() == 0) {
                onSettingChangeListenerMap.remove(key);
            }
        }
    }

    private void checkUIThread() {
        if (!isRunOnUIThread())
            throw new RuntimeException("方法只能在主线程调用！");
    }

    private boolean isRunOnUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public String getSetting(SettingKey key, String defaultValue){
        return mSharedPreferences.getString(key.name(),defaultValue);
    }

    public void putSetting(SettingKey key, String value) {
        mSharedPreferences.edit().putString(key.name(),value).apply();
    }

    public PasswordDBRealm getRealm() {
        return mPasswordDBRealm;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SettingKey settingKey = SettingKey.valueOf(SettingKey.class, key);
        List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(settingKey);
        if (onSettingChangeListeners != null) {
            for (OnSettingChangeListener onSettingChangeListener : onSettingChangeListeners) {
                onSettingChangeListener.onSettingChange(settingKey);
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mPasswordDBRealm.close();
    }
}
