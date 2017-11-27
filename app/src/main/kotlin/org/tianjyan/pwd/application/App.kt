package org.tianjyan.pwd.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import com.home.young.myPassword.database.PasswordDBRealm
import org.tianjyan.pwd.model.SettingKey
import org.tianjyan.pwd.service.*

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mSharedPreferences: SharedPreferences? = null
    private val onSettingChangeListenerMap = HashMap<SettingKey, MutableList<OnSettingChangeListener>>()
    var realm: PasswordDBRealm? = null
        private set

    private val onPasswordListeners = ArrayList<OnPasswordChangeListener>()
    private val onPasswordGroupListeners = ArrayList<OnPasswordGroupChangeListener>()

    private val isRunOnUIThread: Boolean
        get() = Looper.getMainLooper().thread === Thread.currentThread()

    override fun onCreate() {
        super.onCreate()
        loadSettings()
        val key = getSetting(SettingKey.KEY, "")
        realm = PasswordDBRealm(applicationContext, key)
    }

    private fun loadSettings() {
        mSharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        mSharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    fun registOnSettingChangeListener(key: SettingKey, onSettingChangeListener: OnSettingChangeListener) {
        checkUIThread()

        val onSettingChangeListeners: MutableList<OnSettingChangeListener>?
        if (onSettingChangeListenerMap.containsKey(key)) {
            onSettingChangeListeners = onSettingChangeListenerMap[key]
        } else {
            onSettingChangeListeners = ArrayList()
            onSettingChangeListenerMap.put(key, onSettingChangeListeners)
        }
        onSettingChangeListeners!!.add(onSettingChangeListener)
    }

    fun unregistOnSettingChangeListener(key: SettingKey, onSettingChangeListener: OnSettingChangeListener) {
        checkUIThread()
        if (onSettingChangeListenerMap.containsKey(key)) {
            val onSettingChangeListeners = onSettingChangeListenerMap[key]
            onSettingChangeListeners!!.remove(onSettingChangeListener)
            if (onSettingChangeListeners!!.size == 0) {
                onSettingChangeListenerMap.remove(key)
            }
        }
    }

    private fun checkUIThread() {
        if (!isRunOnUIThread)
            throw RuntimeException("方法只能在主线程调用！")
    }

    fun getSetting(key: SettingKey, defaultValue: String): String {
        return mSharedPreferences!!.getString(key.name, defaultValue)
    }

    fun putSetting(key: SettingKey, value: String) {
        mSharedPreferences!!.edit().putString(key.name, value).apply()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val settingKey = SettingKey.valueOf(key)
        val onSettingChangeListeners = onSettingChangeListenerMap[settingKey]
        if (onSettingChangeListeners != null) {
            for (onSettingChangeListener in onSettingChangeListeners) {
                onSettingChangeListener.onSettingChange(settingKey)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        realm!!.close()
    }
}