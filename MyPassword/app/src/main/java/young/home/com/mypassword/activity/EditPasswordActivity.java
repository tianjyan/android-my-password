package young.home.com.mypassword.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseActivity;

public class EditPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
    }
}
