package young.home.com.mypassword.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseActivity;
import young.home.com.mypassword.model.Password;
import young.home.com.mypassword.model.PasswordGroup;
import young.home.com.mypassword.service.MainBinder;
import young.home.com.mypassword.service.MainService;
import young.home.com.mypassword.service.OnGetAllPasswordCallback;
import young.home.com.mypassword.service.OnGetAllPasswordGroupCallback;
import young.home.com.mypassword.service.OnGetPasswordCallback;

public class EditPasswordActivity extends BaseActivity implements OnGetPasswordCallback, OnGetAllPasswordCallback,
        OnGetAllPasswordGroupCallback {

    //region field
    public static final String ID = "password_id";
    public static final String PASSWORD_GROUP = "password_group";
    private static final int MODE_ADD = 0;
    private static final int MODE_MODIFY = 1;
    private int MODE = MODE_ADD;
    private int id;
    private MainBinder mainBinder;
    private EditText titleView;
    private AutoCompleteTextView nameView;
    private AutoCompleteTextView passwordView;
    private EditText noteView;
    private Spinner spinner;
    private String passwordGroup;
    //endregion

    //region lambda
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainBinder = (MainBinder) service;
            if (MODE == MODE_MODIFY) {
                mainBinder.getPassword(id, EditPasswordActivity.this);
            }
            // 获得所有密码、用户名，用于自动完成
            mainBinder.getAllPassword(EditPasswordActivity.this);
            mainBinder.getAllPasswordGroup(EditPasswordActivity.this);
        }
    };
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        titleView = (EditText) findViewById(R.id.edit_password_title);
        nameView = (AutoCompleteTextView) findViewById(R.id.edit_password_name);
        passwordView = (AutoCompleteTextView) findViewById(R.id.edit_password_password);
        noteView = (EditText) findViewById(R.id.edit_password_note);
        spinner = (Spinner) findViewById(R.id.edit_password_group);

        id = getIntent().getIntExtra(ID, -1);
        if (id == -1) {
            MODE = MODE_ADD;
        } else {
            MODE = MODE_MODIFY;
        }

        passwordGroup = getIntent().getStringExtra(PASSWORD_GROUP);

        if (passwordGroup == null || passwordGroup.equals("")) {
            passwordGroup = getString(R.string.default_password_group_name);
        }

        initActionBar();

        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_password, menu);
        if (MODE == MODE_ADD) {
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_action_ok);
        } else {
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_action_save);
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (mainBinder != null) {
                onSaveBtnClick();
            }
            return true;
        } else if (id == R.id.action_delete) {
            if (mainBinder != null) {
                deletePassword();
            }
            return true;
        } else if(id == android.R.id.home){
            finish();
            return true;
        } else if(id == R.id.action_gen){
            GenPasswordDialog dialog = new GenPasswordDialog(getActivity());
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    String result = ((GenPasswordDialog)dialog).getPassword();
                    if(!result.equals("")){
                        passwordView.setText(result);
                    }
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGetPassword(Password password) {
        if (password == null) {
            Toast.makeText(this, R.string.deleted_password_message, Toast.LENGTH_SHORT).show();
            finish();
        }

        titleView.setText(password.getTitle());
        nameView.setText(password.getUserName());
        passwordView.setText(password.getPassword());
        noteView.setText(password.getNote());
        titleView.setSelection(titleView.getText().length());
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        // 去掉重复
        Set<String> arrays = new HashSet<>();
        for (int i = 0; i < passwords.size(); i++) {
            Password password = passwords.get(i);
            arrays.add(password.getUserName());
            arrays.add(password.getPassword());
        }

        // 自动完成
        int id = R.layout.simple_dropdown_item;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, id, new ArrayList<>(arrays));
        nameView.setAdapter(arrayAdapter);
        passwordView.setAdapter(arrayAdapter);
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        List<String> arrays = new ArrayList<>();

        for (int i = 0; i < passwordGroups.size(); i++) {
            PasswordGroup passwordGroup = passwordGroups.get(i);
            arrays.add(passwordGroup.getGroupName());
        }

        if (!arrays.contains(passwordGroup))
            arrays.add(passwordGroup);

        int position = 0;
        for (String passwordGroupName : arrays) {
            if (passwordGroupName.equals(passwordGroup))
                break;
            position++;
        }

        int id = R.layout.simple_dropdown_item;
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(this, id, new ArrayList<>(arrays));

        spinner.setAdapter(spinnerAdapter);

        spinner.setSelection(position);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                passwordGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    //endregion

    //region private
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (MODE == MODE_ADD) {
            actionBar.setTitle(R.string.add);
        } else {
            actionBar.setTitle(R.string.edit);
        }
    }

    private void deletePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_password_message);
        builder.setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainBinder.deletePassword(id);
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void onSaveBtnClick() {
        if (titleView.getText().toString().trim().equals("")) {
            Toast.makeText(this, R.string.empty_title, Toast.LENGTH_SHORT).show();
        } else {
            Password password = new Password();
            password.setTitle(titleView.getText().toString().trim());
            password.setUserName(nameView.getText().toString().trim());
            password.setPassword(passwordView.getText().toString().trim());
            password.setNote(noteView.getText().toString().trim());
            password.setGroupName(passwordGroup);
            if (MODE == MODE_ADD) {
                // 添加
                password.setPublish(System.currentTimeMillis());
                mainBinder.insertPassword(password);
            } else {
                // 修改密码
                password.setId(id);
                mainBinder.updatePassword(password);
            }
            finish();
        }
    }
    //endregion

    //endregion
}
