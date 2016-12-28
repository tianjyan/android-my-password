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

public class GenPasswordDialog extends Dialog {

    //region field
    View cancel;
    View sure;
    View gen;
    CheckBox lows;
    CheckBox caps;
    CheckBox numbers;
    CheckBox special;
    RadioButton eight;
    RadioButton sixteen;
    EditText result;
    String genPassword;
    //endregion

    //region lambda
    private View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private View.OnClickListener sureClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            genPassword = result.getText().toString();
            dismiss();
        }
    };

    private View.OnClickListener genClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
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
    };
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

        cancel = findViewById(R.id.gen_password_cancel);
        sure = findViewById(R.id.gen_password_sure);
        gen = findViewById(R.id.gen_password_gen);
        lows = (CheckBox) findViewById(R.id.gen_password_lows);
        caps = (CheckBox) findViewById(R.id.gen_password_caps);
        numbers = (CheckBox) findViewById(R.id.gen_password_numbers);
        special = (CheckBox) findViewById(R.id.gen_password_special);
        eight = (RadioButton) findViewById(R.id.gen_password_len_eight);
        sixteen = (RadioButton) findViewById(R.id.gen_password_len_sixteen);
        result = (EditText) findViewById(R.id.gen_password_result);

        cancel.setOnClickListener(cancelClick);
        sure.setOnClickListener(sureClick);
        gen.setOnClickListener(genClick);
    }
    //endregion

    //region public
    public String getPassword(){
        return genPassword;
    }

    //endregion

    //endregion
}
