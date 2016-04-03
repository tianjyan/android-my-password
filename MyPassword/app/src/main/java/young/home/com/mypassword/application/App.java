package young.home.com.mypassword.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import young.home.com.mypassword.model.SettingKey;

/**
 * Created by YOUNG on 2016/3/30.
 */
public class App extends Application implements OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private Map<SettingKey, List<OnSettingChangeListener>> onSettingChangeListenerMap = new HashMap<SettingKey,List<OnSettingChangeListener>>();
    @Override
    public void onCreate(){
        super.onCreate();
        loadSettings();
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
}
