package young.home.com.mypassword.application;

import android.app.Activity;

import young.home.com.mypassword.model.SettingKey;

/**
 * Created by YOUNG on 2016/3/31.
 */
public class BaseActivity extends Activity {
    public App getApp(){
        return (App)getApplication();
    }

    public void putSetting(SettingKey key, String value){
        getApp().putSetting(key, value);
    }

    public String getSetting(SettingKey key, String defaultValue){
        return getApp().getSetting(key,defaultValue);
    }
}
