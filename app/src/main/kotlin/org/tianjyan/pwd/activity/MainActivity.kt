package org.tianjyan.pwd.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.home.young.myPassword.R
import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerActivity
import org.tianjyan.pwd.application.BaseActivity
import org.tianjyan.pwd.application.IOHelper
import org.tianjyan.pwd.application.JsonHelper
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.Password
import org.tianjyan.pwd.model.PasswordGroup
import org.tianjyan.pwd.model.SettingKey
import org.tianjyan.pwd.service.MainService
import org.tianjyan.pwd.service.OnGetAllPasswordCallback
import org.tianjyan.pwd.service.OnGetAllPasswordGroupCallback
import org.tianjyan.pwd.service.OnPasswordGroupSelected
import java.io.IOException

class MainActivity : BaseActivity(), OnGetAllPasswordCallback, OnGetAllPasswordGroupCallback {
    @BindView(R.id.main_layout) internal var mDrawerLayout: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    @BindView(R.id.main_navigation_drawer) internal var mDrawerView: View? = null
    private var mMainBinder: PasswordDBRealm? = null
    private var mPasswordListFragment: PasswordListFragment? = null
    private var mPasswordGroupFragment: PasswordGroupFragment? = null
    private var mFullPath: String? = null

    private val onPasswordGroupSelected = object : OnPasswordGroupSelected {
        override fun onPasswordGroupSelected(passwordGroupName : String) {
            mDrawerLayout!!.closeDrawer(mDrawerView)
            if (mPasswordListFragment != null) {
                mPasswordListFragment!!.showPasswordGroup(passwordGroupName)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as PasswordDBRealm
            initFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        val intent = Intent(this, MainService::class.java)
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        initDrawer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!mDrawerLayout!!.isDrawerOpen(mDrawerView!!)) {
            menuInflater.inflate(R.menu.main, menu)
            return true
        } else {
            return super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        val id = item.itemId
        when (id) {
            R.id.action_add_password -> {
                if (mMainBinder != null) {
                    val intent = Intent(this, EditPasswordActivity::class.java)
                    if (mPasswordListFragment != null)
                        intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordListFragment!!.passwordGroupName)
                    startActivity(intent)
                }
            }
            R.id.action_change_login_password -> {
                val setIntent = Intent(this, SetPasswordActivity::class.java)
                startActivity(setIntent)
            }
            R.id.action_import_password -> {
                val intent2 = Intent(activity, FilePickerActivity::class.java)
                intent2.action = Intent.ACTION_GET_CONTENT
                intent2.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                intent2.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                intent2.putExtra(FilePickerActivity.EXTRA_MODE, AbstractFilePickerFragment.MODE_FILE)
                startActivityForResult(intent2, CODE_IN)
            }
            R.id.action_output_password -> {
                val intent1 = Intent(activity, FilePickerActivity::class.java)
                intent1.action = Intent.ACTION_GET_CONTENT
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                intent1.putExtra(FilePickerActivity.EXTRA_MODE, AbstractFilePickerFragment.MODE_DIR)
                startActivityForResult(intent1, CODE_OUT)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onGetAllPassword(groupName: String, passwords: List<Password>) {
        val jsonPasswords = JsonHelper.toJSON(passwords)
        try {
            //IOHelper.writeSDFile(Environment.getExternalStorageDirectory().getPath()
            // + "/Download/MyPasswordBackup.json", jsonPasswords);
            IOHelper.writeSDFile(mFullPath!! + "/MyPasswordBackup.json", jsonPasswords)
        } catch (ex: IOException) {
            showToast(R.string.output_error)
        }

        showToast(R.string.output_done)
    }

    override fun onGetAllPasswordGroup(passwordGroups: List<PasswordGroup>) {
        try {
            val json = IOHelper.readSDFile(mFullPath!!)
            val passwords = JsonHelper.parseArray(json, Password::class.java)!!.toList()
            for (password in passwords) {
                var existGroup = false
                for (j in passwordGroups.indices) {
                    if (passwordGroups[j].groupName == password.groupName) {
                        existGroup = true
                        break
                    }
                }
                if (!existGroup) {
                    val group = PasswordGroup()
                    group.groupName = password.groupName
                    mMainBinder!!.addPasswordGroup(group)
                    passwordGroups.plus(group)
                }
                mMainBinder!!.addPassword(password)
            }

        } catch (ex: IOException) {
            showToast(R.string.import_error)
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        mFullPath = null
        if (resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                    false)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    val clip = data.clipData
                    val sb = StringBuilder()

                    if (clip != null) {
                        for (i in 0 until clip.itemCount) {
                            sb.append(clip.getItemAt(i).uri.toString())
                            sb.append("\n")
                        }
                    }
                    mFullPath = sb.toString()
                } else {
                    val paths = data.getStringArrayListExtra(
                            FilePickerActivity.EXTRA_PATHS)
                    val sb = StringBuilder()

                    if (paths != null) {
                        for (path in paths) {
                            sb.append(path)
                            sb.append("\n")
                        }
                    }
                    mFullPath = sb.toString()
                }
            } else {
                mFullPath = data.data!!.path
            }
        }
        if (!TextUtils.isEmpty(mFullPath)) {
            if (CODE_OUT == requestCode)
                mMainBinder!!.getAllPasswordByGroupName(null, this)
            else if (CODE_IN == requestCode)
                mMainBinder!!.getAllPasswordGroup(this)
        }
    }
    //endregion

    //region init
    private fun initDrawer() {
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        mDrawerToggle = object : ActionBarDrawerToggle(activity, mDrawerLayout,
                R.string.open, R.string.close) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                activity.invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
                activity.invalidateOptionsMenu()
                supportActionBar!!.title = activity.getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, getString(R.string.app_name))
            }
        }

        mDrawerLayout!!.post { mDrawerToggle!!.syncState() }
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        if (getSetting(SettingKey.IS_SHOWED_DRAWER, "false") == "false") {
            putSetting(SettingKey.IS_SHOWED_DRAWER, "true")
            mDrawerLayout!!.openDrawer(mDrawerView)
        } else {
            var lastGroupName = getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, "")
            if (lastGroupName == "")
                lastGroupName = getString(R.string.app_name)
            supportActionBar!!.title = lastGroupName
        }
    }

    private fun initFragment() {
        val fragmentManager = fragmentManager

        mPasswordListFragment = fragmentManager.findFragmentByTag("PasswordListFragment") as PasswordListFragment
        if (mPasswordListFragment == null)
            mPasswordListFragment = PasswordListFragment()
        mPasswordListFragment!!.setDataSource(mMainBinder!!)

        mPasswordGroupFragment = fragmentManager.findFragmentByTag("PasswordGroupFragment") as PasswordGroupFragment
        if (mPasswordGroupFragment == null)
            mPasswordGroupFragment = PasswordGroupFragment()
        mPasswordGroupFragment!!.setDataSource(mMainBinder!!, onPasswordGroupSelected )


        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_navigation_drawer, mPasswordGroupFragment, "PasswordGroupFragment")
        fragmentTransaction.replace(R.id.main_container, mPasswordListFragment, "PasswordListFragment")
        fragmentTransaction.commitAllowingStateLoss()
    }

    companion object {
        private val CODE_OUT = 0
        private val CODE_IN = 1
    }
}