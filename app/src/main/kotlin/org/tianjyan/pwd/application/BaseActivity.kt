package org.tianjyan.pwd.application

import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import org.tianjyan.pwd.model.SettingKey

open class BaseActivity : AppCompatActivity() {
    private val app: App
        get() = application as App

    protected val activity: BaseActivity
        get() = this

    fun putSetting(key: SettingKey, value: String) {
        app.putSetting(key, value)
    }

    fun getSetting(key: SettingKey, defaultValue: String): String {
        return app.getSetting(key, defaultValue)
    }

    @JvmOverloads
    fun showToast(id: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, id, duration).show()
    }
}