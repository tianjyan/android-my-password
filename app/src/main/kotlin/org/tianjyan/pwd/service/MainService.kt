package org.tianjyan.pwd.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.tianjyan.pwd.application.App
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.SettingKey

class MainService : Service() {

    private var mainBinder: PasswordDBRealm? = null

    override fun onBind(intent: Intent): IBinder? {
        return mainBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        mainBinder!!.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as App
        val key = app.getSetting(SettingKey.KEY, "")
        mainBinder = PasswordDBRealm(applicationContext, key)
    }
}