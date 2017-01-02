package com.home.young.myPassword.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.home.young.myPassword.application.App;
import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.SettingKey;

public class MainService extends Service {

    private PasswordDBRealm mainBinder;

    @Override
    public IBinder onBind(Intent intent) {
        return mainBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainBinder.onDestroy();
    }

    @Override
    public  void onCreate(){
        super.onCreate();
        App app = (App)getApplicationContext();
        final String key = app.getSetting(SettingKey.KEY, "");
        mainBinder = new PasswordDBRealm(getApplicationContext(), key);
    }
}
