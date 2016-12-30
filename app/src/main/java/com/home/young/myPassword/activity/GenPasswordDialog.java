package com.home.young.myPassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.home.young.myPassword.R;
import com.home.young.myPassword.application.PwdGen;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GenPasswordDialog extends Dialog {

    //region field
    @BindView(R.id.gen_password_lows) CheckBox lows;
    @BindView(R.id.gen_password_caps) CheckBox caps;
    @BindView(R.id.gen_password_numbers) CheckBox numbers;
    @BindView(R.id.gen_password_special) CheckBox special;
    @BindView(R.id.gen_password_len_eight) RadioButton eight;
    @BindView(R.id.gen_password_result) EditText result;
    String genPassword;
    //endregion

    //region onclick
    @OnClick(R.id.gen_password_cancel)
    public void cancelClick() {
        dismiss();
    }

    @OnClick(R.id.gen_password_sure)
    public void sureClick() {
        genPassword = result.getText().toString();
        dismiss();
    }

    @OnClick(R.id.gen_password_gen)
    public void genClick() {
        if(lows.isChecked() || caps.isChecked() || numbers.isChecked() || special.isChecked()) {

            String password = PwdGen.generatePassword(
                    eight.isChecked() ? 8 : 16,
                    lows.isChecked() ? PwdGen.Optionality.MANDATORY : PwdGen.Optionality.PROHIBITED,
                    caps.isChecked() ? PwdGen.Optionality.MANDATORY : PwdGen.Optionality.PROHIBITED,
                    numbers.isChecked() ? PwdGen.Optionality.MANDATORY : PwdGen.Optionality.PROHIBITED,
                    special.isChecked() ? PwdGen.Optionality.MANDATORY : PwdGen.Optionality.PROHIBITED);
            result.setText(password);
        }
        else {
            Toast.makeText(getContext(), getContext().getString(R.string.gen_password_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region constructor
    public GenPasswordDialog(Context context) {
        super(context);
    }
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gen_password);
        ButterKnife.bind(this);
    }
    //endregion

    //region public
    public String getPassword(){
        return genPassword;
    }

    //endregion

    //endregion
}