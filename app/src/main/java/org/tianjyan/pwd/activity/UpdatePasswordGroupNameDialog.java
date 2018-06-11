package org.tianjyan.pwd.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

import org.tianjyan.pwd.R;
import org.tianjyan.pwd.database.PasswordDBRealm;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpdatePasswordGroupNameDialog extends Dialog {

    //region field
    @BindView(R.id.update_passwordGroup_name) EditText editText;
    private PasswordDBRealm mainBinder;
    private String oldGroupName;
    //endregion

    //region onclick
    @OnClick(R.id.update_passwordGroup_cancel)
    public void cancelClick() {
        dismiss();
    }

    @OnClick(R.id.update_passwordGroup_sure)
    public void sureClick() {
        String name = editText.getText().toString().trim();
        if(name.length() > 0){
            if(!name.equals(oldGroupName)){
                mainBinder.updatePasswordGroup(oldGroupName, name);
            }
            dismiss();
        }
    }
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_password_group_name);
        ButterKnife.bind(this);

        editText.setText(oldGroupName);
        editText.requestFocus();
    }
    //endregion

    //region private
    public UpdatePasswordGroupNameDialog(Context context, String oldGroupName, PasswordDBRealm mainBinder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainBinder = mainBinder;
        this.oldGroupName = oldGroupName;
    }
    //endregion

    //endregion
}
