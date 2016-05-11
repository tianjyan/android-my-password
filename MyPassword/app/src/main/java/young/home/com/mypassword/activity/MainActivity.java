package young.home.com.mypassword.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseActivity;
import young.home.com.mypassword.model.SettingKey;
import young.home.com.mypassword.service.MainBinder;
import young.home.com.mypassword.service.MainService;
import young.home.com.mypassword.service.OnPasswordGroupSelected;

public class MainActivity extends BaseActivity {

    //region field
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View drawerView;
    private MainBinder mainBinder;
    private PasswordListFragment passwordListFragment;
    private PasswordGroupFragment passwordGroupFragment;
    //endregion

    //region lambda
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

        drawerLayout = (DrawerLayout)findViewById(R.id.main_layout);
        drawerView = findViewById(R.id.main_navigation_drawer);

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
                break;
            case R.id.action_output_password:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
