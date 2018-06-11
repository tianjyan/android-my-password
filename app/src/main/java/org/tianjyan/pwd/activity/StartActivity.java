package org.tianjyan.pwd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tianjyan.pwd.R;
import org.tianjyan.pwd.application.BaseActivity;
import org.tianjyan.pwd.application.MD5;
import org.tianjyan.pwd.model.SettingKey;

public class StartActivity extends BaseActivity implements TextView.OnEditorActionListener {

    //region field
    EditText inputPwd;
    String pwd;
    String noPwd;
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        noPwd = super.getSetting(SettingKey.NO_LOCK_PWD, "false");
        pwd = super.getSetting(SettingKey.LOCK_PWD, "");
        if(noPwd.equals("true"))
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if (pwd.isEmpty()) {
            Intent intent = new Intent(this, SetPasswordActivity.class);
            startActivity(intent);
            finish();
        }

        inputPwd = (EditText) findViewById(R.id.start_password);
        inputPwd.setOnEditorActionListener(this);
        inputPwd.requestFocus();
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        switch (i) {
            case EditorInfo.IME_ACTION_DONE:
                if (pwd.equals(MD5.getMD5(inputPwd.getText().toString()))) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        return true;
    }
    //endregion

    //endregion
}
