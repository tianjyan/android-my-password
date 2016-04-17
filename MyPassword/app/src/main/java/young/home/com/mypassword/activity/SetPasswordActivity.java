package young.home.com.mypassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseActivity;
import young.home.com.mypassword.application.MD5;
import young.home.com.mypassword.model.SettingKey;

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

    //region private
    public void nextClick(View v) {
        if(pwdEt.getText().toString().equals(rePwdEt.getText().toString())){
            String pwd = pwdEt.getText().toString();
            super.putSetting(SettingKey.LOCK_PWD, MD5.getMD5(pwd));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, R.string.different_password, Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //endregion
}
