package com.home.young.myPassword.application;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.home.young.myPassword.model.SettingKey;

/**
 * Created by YOUNG on 2016/3/31.
 */
public class BaseActivity extends AppCompatActivity {
    public App getApp(){
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

    public BaseActivity getActivity() {
        return this;
    }
}
