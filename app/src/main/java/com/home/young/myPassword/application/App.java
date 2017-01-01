package com.home.young.myPassword.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.AsyncResult;
import com.home.young.myPassword.model.AsyncSingleTask;
import com.home.young.myPassword.model.SettingKey;
import com.home.young.myPassword.service.OnPasswordChangeListener;
import com.home.young.myPassword.service.OnPasswordGroupChangeListener;
import com.home.young.myPassword.service.OnSettingChangeListener;

import io.realm.Realm;

public class App extends Application implements OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private Map<SettingKey, List<OnSettingChangeListener>> onSettingChangeListenerMap = new HashMap<SettingKey,List<OnSettingChangeListener>>();
    private PasswordDBRealm passwordDBRealm;

    private List<OnPasswordChangeListener> onPasswordListeners = new ArrayList<OnPasswordChangeListener>();
    private List<OnPasswordGroupChangeListener> onPasswordGroupListeners = new ArrayList<OnPasswordGroupChangeListener>();

    @Override
    public void onCreate(){
        super.onCreate();
        loadSettings();
        final String key = getSetting(SettingKey.KEY, "");
        passwordDBRealm = new PasswordDBRealm(getApplicationContext(), key);
    }

    private  void loadSettings(){
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void registOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
        checkUIThread();

        List<OnSettingChangeListener> onSettingChangeListeners;
        if (onSettingChangeListenerMap.containsKey(key)) {
            onSettingChangeListeners = onSettingChangeListenerMap.get(key);
        } else {
            onSettingChangeListeners = new ArrayList<OnSettingChangeListener>();
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
        return sharedPreferences.getString(key.name(),defaultValue);
    }

    public void putSetting(SettingKey key, String value) {
        sharedPreferences.edit().putString(key.name(),value).commit();
    }

    public PasswordDBRealm getRealm() {
        return passwordDBRealm;
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
        passwordDBRealm.close();
    }
}
