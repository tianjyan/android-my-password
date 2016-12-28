package com.home.young.myPassword.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.home.young.myPassword.R;
import com.home.young.myPassword.application.BaseActivity;
import com.home.young.myPassword.application.IOHelper;
import com.home.young.myPassword.application.JsonHelper;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.PasswordGroup;
import com.home.young.myPassword.model.SettingKey;
import com.home.young.myPassword.service.MainBinder;
import com.home.young.myPassword.service.MainService;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;
import com.home.young.myPassword.service.OnGetAllPasswordGroupCallback;
import com.home.young.myPassword.service.OnPasswordGroupSelected;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements OnGetAllPasswordCallback, OnGetAllPasswordGroupCallback {

    //region field
    private static final int CODE_OUT = 0;
    private static final int CODE_IN = 1;
    @BindView(R.id.main_layout) DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.main_navigation_drawer) View drawerView;
    private MainBinder mainBinder;
    private PasswordListFragment passwordListFragment;
    private PasswordGroupFragment passwordGroupFragment;
    private String fullPath;
    //endregion

    //region anonymous class
    private OnPasswordGroupSelected onPasswordGroupSelected = new OnPasswordGroupSelected() {
        @Override
        public void onPasswordGroupSelected(String passwordGroupName) {
            drawerLayout.closeDrawer(drawerView);
            if (passwordListFragment != null){
                passwordListFragment.showPasswordGroup(passwordGroupName);
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainBinder = (MainBinder) service;
            initFragment();
        }
    };
    //endregion

    //region function

    //region override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!drawerLayout.isDrawerOpen(drawerView)) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        switch (id){
            case R.id.action_add_password:
                if (mainBinder == null)
                    break;
                Intent intent = new Intent(this, EditPasswordActivity.class);
                if (passwordListFragment != null)
                    intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordListFragment.getPasswordGroupName());
                startActivity(intent);
                break;
            case R.id.action_change_login_password:
                Intent setIntent = new Intent(this, SetPasswordActivity.class);
                startActivity(setIntent);
                break;
            case R.id.action_import_password:
                Intent intent2 = new Intent(getActivity(), FilePickerActivity.class);
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                intent2.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent2.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent2.putExtra(FilePickerActivity.EXTRA_MODE, AbstractFilePickerFragment.MODE_FILE);
                startActivityForResult(intent2, CODE_IN);
                break;
            case R.id.action_output_password:
                Intent intent1 = new Intent(getActivity(), FilePickerActivity.class);
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent1.putExtra(FilePickerActivity.EXTRA_MODE, AbstractFilePickerFragment.MODE_DIR);
                startActivityForResult(intent1, CODE_OUT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        String jsonPasswords = JsonHelper.toJSON(passwords);
        try {
            //IOHelper.writeSDFile(Environment.getExternalStorageDirectory().getPath() + "/Download/MyPasswordBackup.json", jsonPasswords);
            IOHelper.writeSDFile(fullPath + "/MyPasswordBackup.json", jsonPasswords);
        }
        catch (IOException ex){
            showToast(R.string.output_error);
        }
        showToast(R.string.output_done);
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        try {
            String json = IOHelper.readSDFile(fullPath);
            Password[] passwords = JsonHelper.parseArray(json,Password.class);
            for(int i=0;i<passwords.length;i++){
                boolean existGroup = false;
                for(int j=0;j<passwordGroups.size();j++){
                    if(passwordGroups.get(j).getGroupName().equals(passwords[i].getGroupName())){
                        existGroup = true;
                        break;
                    }
                }
                if(!existGroup){
                    PasswordGroup group = new PasswordGroup();
                    group.setGroupName(passwords[i].getGroupName());
                    mainBinder.insertPasswordGroup(group);
                    passwordGroups.add(group);
                }
                mainBinder.insertPassword(passwords[i]);
            }

        }
        catch (IOException ex){
            showToast(R.string.import_error);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fullPath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                    false)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();
                    StringBuilder sb = new StringBuilder();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            sb.append(clip.getItemAt(i).getUri().toString());
                            sb.append("\n");
                        }
                    }
                    fullPath = sb.toString();
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra(
                            FilePickerActivity.EXTRA_PATHS);
                    StringBuilder sb = new StringBuilder();

                    if (paths != null) {
                        for (String path : paths) {
                            sb.append(path);
                            sb.append("\n");
                        }
                    }
                    fullPath = sb.toString();
                }
            } else {
                fullPath = data.getData().getPath();
            }
        }
        if(!TextUtils.isEmpty(fullPath)){
            if(CODE_OUT == requestCode)
                mainBinder.getAllPassword(this);
            else if(CODE_IN == requestCode)
                mainBinder.getAllPasswordGroup(this);
        }
    }
    //endregion

    //region init
    private void initDrawer(){
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
                getSupportActionBar().setTitle(getActivity().getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, getString(R.string.app_name)));
            }
        };

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        drawerLayout.setDrawerListener(mDrawerToggle);

        if (getSetting(SettingKey.IS_SHOWED_DRAWER, "false").equals("false")) {
            putSetting(SettingKey.IS_SHOWED_DRAWER, "true");
            drawerLayout.openDrawer(drawerView);
        } else {
            String lastGroupName = getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, "");
            if (lastGroupName.equals(""))
                lastGroupName = getString(R.string.app_name);
            getSupportActionBar().setTitle(lastGroupName);
        }
    }

    private void initFragment(){
        FragmentManager fragmentManager = getFragmentManager();

        passwordListFragment = (PasswordListFragment) fragmentManager.findFragmentByTag("PasswordListFragment");
        if (passwordListFragment == null)
            passwordListFragment = new PasswordListFragment();
        passwordListFragment.setDataSource(mainBinder);

        passwordGroupFragment = (PasswordGroupFragment) fragmentManager.findFragmentByTag("PasswordGroupFragment");
        if (passwordGroupFragment == null)
            passwordGroupFragment = new PasswordGroupFragment();
        passwordGroupFragment.setDataSource(mainBinder, onPasswordGroupSelected);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_navigation_drawer, passwordGroupFragment, "PasswordGroupFragment");
        fragmentTransaction.replace(R.id.main_container, passwordListFragment, "PasswordListFragment");
        fragmentTransaction.commitAllowingStateLoss();
    }

    //endregion

    //endregion
}
