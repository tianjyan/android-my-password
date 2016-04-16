package young.home.com.mypassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import young.home.com.mypassword.R;
import young.home.com.mypassword.model.PasswordGroup;
import young.home.com.mypassword.service.MainBinder;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class AddPasswordGroupDialog extends Dialog {

    //region field
    private EditText editText;
    private View cancelBtn;
    private View sureBtn;
    private View container;
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

        cancelBtn = findViewById(R.id.add_password_group_cancle_btn);
        sureBtn = findViewById(R.id.add_password_group_sure_btn);
        editText = (EditText)findViewById(R.id.add_passwrdGroup_editview);
        container = findViewById(R.id.container);

        cancelBtn.setOnClickListener(onCancelClickListener);
        sureBtn.setOnClickListener(onSureClickListener);
        container.setOnClickListener(onCancelClickListener);

        editText.requestFocus();
    }
    //endregion
}
