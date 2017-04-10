package com.home.young.myPassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

import com.home.young.myPassword.R;
import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.PasswordGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPasswordGroupDialog extends Dialog {

    //region field
    @BindView(R.id.create_passwordGroup_name) EditText editText;
    private PasswordDBRealm mainBinder;
    //endregion

    //region onclick
    @OnClick(R.id.create_passwordGroup_cancel)
    public void cancelClick() {
        dismiss();
    }

    @OnClick(R.id.create_passwordGroup_sure)
    public void sureClick() {
        String name = editText.getText().toString().trim();
        if(name.length() > 0){
            PasswordGroup passwordGroup = new PasswordGroup();
            passwordGroup.setGroupName(name);
            mainBinder.addPasswordGroup(passwordGroup);
            dismiss();
        }
    }
    //endregion

    //region constructor
    public AddPasswordGroupDialog(Context context, PasswordDBRealm mainBinder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainBinder = mainBinder;
    }
    //endregion

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_password_group);
        ButterKnife.bind(this);

        editText.requestFocus();
    }
    //endregion
}
