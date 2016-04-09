package young.home.com.mypassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import young.home.com.mypassword.R;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class AddPasswordGroupDialog extends Dialog {

    private EditText editText;
    private View cancelBtn;
    private View sureBtn;
    private View container;
    private Binder binder;

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
                dismiss();
            }
        }
    };

    public AddPasswordGroupDialog(Context context, Binder binder){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.binder = binder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_password_group);

        cancelBtn = findViewById(R.id.add_password_group_cancle_btn);
        sureBtn = findViewById(R.id.add_password_group_sure_btn);
        editText = (EditText)findViewById(R.id.add_passwrdGroup_editview);
        container = findViewById(R.id.container);

        cancelBtn.setOnClickListener(onCancelClickListener);
        sureBtn.setOnClickListener(onSureClickListener);
        container.setOnClickListener(onCancelClickListener);

        editText.requestFocus();
    }


}
