package com.home.young.myPassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.test.suitebuilder.annotation.Smoke;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.home.young.myPassword.R;
import com.home.young.myPassword.service.MainBinder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class UpdatePasswordGroupNameDialog extends Dialog {

    //region field
    @BindView(R.id.update_passwordGroup_name) EditText editText;
    private MainBinder mainBinder;
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
                mainBinder.updatePasswdGroupName(oldGroupName, name);
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
    public UpdatePasswordGroupNameDialog(Context context, String oldGroupName, MainBinder mainBinder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainBinder = mainBinder;
        this.oldGroupName = oldGroupName;
    }
    //endregion

    //endregion
}
