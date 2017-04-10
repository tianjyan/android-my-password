package com.home.young.myPassword.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.home.young.myPassword.R;
import com.home.young.myPassword.application.BaseActivity;
import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.PasswordGroup;
import com.home.young.myPassword.service.MainService;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;
import com.home.young.myPassword.service.OnGetAllPasswordGroupCallback;
import com.home.young.myPassword.service.OnGetPasswordCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPasswordActivity extends BaseActivity implements OnGetPasswordCallback, OnGetAllPasswordCallback,
        OnGetAllPasswordGroupCallback {

    //region field
    public static final String ID = "password_id";
    public static final String PASSWORD_GROUP = "password_group";
    private static final int MODE_ADD = 0;
    private static final int MODE_MODIFY = 1;
    private int mMode = MODE_ADD;
    private String mId;
    private PasswordDBRealm mMainBinder;
    @BindView(R.id.edit_password_title) EditText mTitleView;
    @BindView(R.id.edit_password_name) AutoCompleteTextView mNameView;
    @BindView(R.id.edit_password_password) AutoCompleteTextView mPasswordView;
    @BindView(R.id.edit_password_pay_password) AutoCompleteTextView mPayPasswordView;
    @BindView(R.id.edit_password_note) EditText mNoteView;
    @BindView(R.id.edit_password_group) Spinner mSpinner;
    private String mPasswordGroup;
    //endregion

    //region anonymous class
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMainBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainBinder = (PasswordDBRealm) service;
            if (mMode == MODE_MODIFY) {
                mMainBinder.getPassword(mId, EditPasswordActivity.this);
            }
            // 获得所有密码、用户名，用于自动完成
            mMainBinder.getAllPasswordByGroupName(null, EditPasswordActivity.this);
            mMainBinder.getAllPasswordGroup(EditPasswordActivity.this);
        }
    };
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra(ID);
        if (TextUtils.isEmpty(mId)) {
            mMode = MODE_ADD;
        } else {
            mMode = MODE_MODIFY;
        }

        mPasswordGroup = getIntent().getStringExtra(PASSWORD_GROUP);

        if (mPasswordGroup == null || mPasswordGroup.equals("")) {
            mPasswordGroup = getString(R.string.default_password_group_name);
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
        if (mMode == MODE_ADD) {
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
            if (mMainBinder != null) {
                onSaveBtnClick();
            }
            return true;
        } else if (id == R.id.action_delete) {
            if (mMainBinder != null) {
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
                    if(result != null && !result.equals("")){
                        if(mPasswordView.isFocused()) {
                            mPasswordView.setText(result);
                        }else if(mPayPasswordView.isFocused()){
                            mPayPasswordView.setText(result);
                        }
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

        mTitleView.setText(password.getTitle());
        mNameView.setText(password.getUserName());
        mPasswordView.setText(password.getPassword());
        mPayPasswordView.setText(password.getPayPassword());
        mNoteView.setText(password.getNote());
        mTitleView.setSelection(mTitleView.getText().length());
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        // 去掉重复
        Set<String> arrays = new HashSet<>();
        for (int i = 0; i < passwords.size(); i++) {
            Password password = passwords.get(i);
            arrays.add(password.getUserName());
            arrays.add(password.getPassword());
            arrays.add(password.getPayPassword());
        }

        // 自动完成
        int id = R.layout.simple_dropdown_item;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, id, new ArrayList<>(arrays));
        mNameView.setAdapter(arrayAdapter);
        mPasswordView.setAdapter(arrayAdapter);
        mPayPasswordView.setAdapter(arrayAdapter);
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        List<String> arrays = new ArrayList<>();

        for (int i = 0; i < passwordGroups.size(); i++) {
            PasswordGroup passwordGroup = passwordGroups.get(i);
            arrays.add(passwordGroup.getGroupName());
        }

        if (!arrays.contains(mPasswordGroup))
            arrays.add(mPasswordGroup);

        int position = 0;
        for (String passwordGroupName : arrays) {
            if (passwordGroupName.equals(mPasswordGroup))
                break;
            position++;
        }

        int id = R.layout.simple_dropdown_item;
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(this, id, new ArrayList<>(arrays));

        mSpinner.setAdapter(spinnerAdapter);

        mSpinner.setSelection(position);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPasswordGroup = (String) parent.getItemAtPosition(position);
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
        if (mMode == MODE_ADD) {
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
                mMainBinder.deletePassword(mId);
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void onSaveBtnClick() {
        if (mTitleView.getText().toString().trim().equals("")) {
            Toast.makeText(this, R.string.empty_title, Toast.LENGTH_SHORT).show();
        } else {
            Password password = new Password();
            password.setTitle(mTitleView.getText().toString().trim());
            password.setUserName(mNameView.getText().toString().trim());
            password.setPassword(mPasswordView.getText().toString().trim());
            password.setPayPassword(mPayPasswordView.getText().toString().trim());
            password.setNote(mNoteView.getText().toString().trim());
            password.setGroupName(mPasswordGroup);
            password.setPublish(System.currentTimeMillis());
            if (mMode == MODE_ADD) {
                // 添加
                mMainBinder.addPassword(password);
            } else {
                // 修改密码
                password.setId(mId);
                mMainBinder.updatePassword(password);
            }
            finish();
        }
    }
    //endregion

    //endregion
}
