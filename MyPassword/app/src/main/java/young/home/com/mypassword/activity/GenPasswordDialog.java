package young.home.com.mypassword.activity;

import android.app.Dialog;
import android.content.Context;

import young.home.com.mypassword.R;

public class GenPasswordDialog extends Dialog {

    public GenPasswordDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_gen_password);
    }
}
