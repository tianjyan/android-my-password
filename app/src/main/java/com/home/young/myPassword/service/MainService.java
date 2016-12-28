package com.home.young.myPassword.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.home.young.myPassword.application.App;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class MainService extends Service {

    private MainBinder mainBinder;

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
        mainBinder = new MainBinder(this, (App) getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
