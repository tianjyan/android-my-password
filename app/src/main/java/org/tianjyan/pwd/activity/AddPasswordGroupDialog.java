package org.tianjyan.pwd.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

import org.tianjyan.pwd.R;
import org.tianjyan.pwd.database.PasswordDBRealm;
import org.tianjyan.pwd.model.PasswordGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPasswordGroupDialog extends Dialog {

    //region field
    @BindView(R.id.create_passwordGroup_name) EditText mEditText;
    private PasswordDBRealm mMainBinder;
    //endregion

    //region onclick
    @OnClick(R.id.create_passwordGroup_cancel)
    public void cancelClick() {
        dismiss();
    }

    @OnClick(R.id.create_passwordGroup_sure)
    public void sureClick() {
        String name = mEditText.getText().toString().trim();
        if(name.length() > 0){
            PasswordGroup passwordGroup = new PasswordGroup();
            passwordGroup.setGroupName(name);
            mMainBinder.addPasswordGroup(passwordGroup);
            dismiss();
        }
    }
    //endregion

    //region constructor
    public AddPasswordGroupDialog(Context context, PasswordDBRealm mainBinder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mMainBinder = mainBinder;
    }
    //endregion

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_password_group);
        ButterKnife.bind(this);

        mEditText.requestFocus();
    }
    //endregion
}
