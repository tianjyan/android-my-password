package com.home.young.myPassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.home.young.myPassword.R;
import com.home.young.myPassword.model.PasswordGroup;
import com.home.young.myPassword.service.MainBinder;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class AddPasswordGroupDialog extends Dialog {

    //region field
    private EditText editText;
    private View cancelBtn;
    private View sureBtn;
    private MainBinder mainBinder;
    //endregion

    //region lambda
    private View.OnClickListener onCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private View.OnClickListener onSureClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String name = editText.getText().toString().trim();
            if(name.length() > 0){
                PasswordGroup passwordGroup = new PasswordGroup();
                passwordGroup.setGroupName(name);
                mainBinder.insertPasswordGroup(passwordGroup);
                dismiss();
            }
        }
    };
    //endregion

    //region constructor
    public AddPasswordGroupDialog(Context context, MainBinder mainBinder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainBinder = mainBinder;
    }
    //endregion

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_password_group);

        cancelBtn = findViewById(R.id.create_passwordGroup_cancel);
        sureBtn = findViewById(R.id.create_passwordGroup_sure);
        editText = (EditText)findViewById(R.id.create_passwordGroup_name);

        cancelBtn.setOnClickListener(onCancelClickListener);
        sureBtn.setOnClickListener(onSureClickListener);

        editText.requestFocus();
    }
    //endregion
}
