package com.home.young.myPassword.application;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.home.young.myPassword.model.SettingKey;

public class BaseActivity extends AppCompatActivity {
    private App getApp(){
        return (App)getApplication();
    }

    public void putSetting(SettingKey key, String value){
        getApp().putSetting(key, value);
    }

    public String getSetting(SettingKey key, String defaultValue){
        return getApp().getSetting(key,defaultValue);
    }

    public void showToast(int id) {
        showToast(id, Toast.LENGTH_SHORT);
    }

    public void showToast(int id, int duration) {
        Toast.makeText(this, id, duration).show();
    }

    protected BaseActivity getActivity() {
        return this;
    }
}
