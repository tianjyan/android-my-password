package young.home.com.mypassword.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import young.home.com.mypassword.R;
import young.home.com.mypassword.service.MainBinder;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class UpdatePasswordGroupNameDialog extends Dialog {

    //region field
    private EditText editText;
    private MainBinder mainBinder;
    private String oldGroupName;
    private View cancelBtn;
    private View sureBtn;
    private View container;
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
                if(!name.equals(oldGroupName)){
                    mainBinder.updatePasswdGroupName(oldGroupName, name);
                }
                dismiss();
            }
        }
    };
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_password_group_name);

        cancelBtn = findViewById(R.id.edit_passwordGroup_cancelBtn);
        sureBtn = findViewById(R.id.edit_passwordGroup_sureBtn);
        editText = (EditText) findViewById(R.id.edit_passwordGroup_editView);
        container = findViewById(R.id.container);

        cancelBtn.setOnClickListener(onCancelClickListener);
        container.setOnClickListener(onCancelClickListener);
        sureBtn.setOnClickListener(onSureClickListener);

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
