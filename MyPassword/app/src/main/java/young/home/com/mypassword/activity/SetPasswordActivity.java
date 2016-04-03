package young.home.com.mypassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseActivity;
import young.home.com.mypassword.application.MD5;
import young.home.com.mypassword.model.SettingKey;

public class SetPasswordActivity extends BaseActivity {

    private EditText pwdEt;
    private EditText rePwdEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        pwdEt = (EditText)findViewById(R.id.PwdEt);
        rePwdEt = (EditText)findViewById(R.id.RePwdEt);
    }

    public void EnterClick(View v) {
        if(pwdEt.getText().toString().equals(rePwdEt.getText().toString())){
            String pwd = pwdEt.getText().toString();
            super.putSetting(SettingKey.LOCK_PWD, MD5.getMD5(pwd));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, "两次密码不同", Toast.LENGTH_SHORT).show();
        }
    }

}
