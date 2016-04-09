package young.home.com.mypassword.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View drawerView;

    private PasswordListFragment passwordListFragment;
    private PasswordGroupFragment passwordGroupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.navigation_drawer);

        initDrawer();

        intiFragment();
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
                getSupportActionBar().setTitle("Main");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
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
            String lastGroupName = getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, "");
            if (lastGroupName.equals(""))
                lastGroupName = getString(R.string.app_name);
            getSupportActionBar().setTitle(lastGroupName);
        }
    }

    private void intiFragment(){
        FragmentManager fragmentManager = getFragmentManager();

        passwordListFragment = (PasswordListFragment) fragmentManager.findFragmentByTag("PasswordListFragment");
        if (passwordListFragment == null)
            passwordListFragment = new PasswordListFragment();

        passwordGroupFragment = (PasswordGroupFragment) fragmentManager.findFragmentByTag("PasswordGroupFragment");
        if (passwordGroupFragment == null)
            passwordGroupFragment = new PasswordGroupFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navigation_drawer, passwordGroupFragment, "PasswordGroupFragment");
        fragmentTransaction.replace(R.id.container, passwordListFragment, "PasswordListFragment");
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
