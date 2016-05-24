package com.home.young.myPassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.home.young.myPassword.R;
import com.home.young.myPassword.application.BaseActivity;
import com.home.young.myPassword.application.MD5;
import com.home.young.myPassword.application.PwdGen;
import com.home.young.myPassword.model.SettingKey;

public class SetPasswordActivity extends BaseActivity implements TextWatcher {

    //region field
    private EditText pwdEt;
    private EditText rePwdEt;
    private Button nextBtn;
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        pwdEt = (EditText)findViewById(R.id.set_password_first);
        rePwdEt = (EditText)findViewById(R.id.set_password_second);
        nextBtn = (Button)findViewById(R.id.set_password_next);

        pwdEt.addTextChangedListener(this);
        rePwdEt.addTextChangedListener(this);

        pwdEt.requestFocus();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(pwdEt.getText().length() > 0 && rePwdEt.getText().length() > 0){
            nextBtn.setEnabled(true);
        }
        else{
            nextBtn.setEnabled(false);
        }
    }
    //endregion

    //region public
    public void nextClick(View v) {
        if(pwdEt.getText().toString().equals(rePwdEt.getText().toString())){
            String pwd = pwdEt.getText().toString();
            super.putSetting(SettingKey.LOCK_PWD, MD5.getMD5(pwd));
            super.putSetting(SettingKey.NO_LOCK_PWD, "false");
            GenKey();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, R.string.different_password, Toast.LENGTH_SHORT).show();
        }
    }

    public void skipClick(View v) {
        super.putSetting(SettingKey.NO_LOCK_PWD, "true");
        GenKey();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //endregion

    //region private
    private void GenKey(){
        String key = super.getSetting(SettingKey.KEY, "");
        if(key.equals("")){
            key = PwdGen.generatePassword(8,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY);
            super.putSetting(SettingKey.KEY, key);
        }
    }
    //endregion

    //endregion
}
