package org.tianjyan.pwd.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.home.young.myPassword.R
import org.tianjyan.pwd.adapter.PasswordListAdapter
import org.tianjyan.pwd.application.BaseFragment
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.Password
import org.tianjyan.pwd.model.SettingKey
import org.tianjyan.pwd.service.OnGetAllPasswordCallback
import org.tianjyan.pwd.service.OnPasswordChangeListener

class PasswordListFragment : BaseFragment(), OnGetAllPasswordCallback, android.view.View.OnClickListener {

    private var mMainAdapter: PasswordListAdapter? = null
    private var mMainBinder: PasswordDBRealm? = null
    private var mListView: ListView? = null
    private var mNoDataView: View? = null
    var passwordGroupName: String? = null
        private set

    private val onPasswordListener = object : OnPasswordChangeListener {
        override fun onNewPassword(password: Password) {
            if (password.groupName == passwordGroupName) {
                mMainAdapter!!.onNewPassword(password)
                initView()
            }
        }

        override fun onDeletePassword(id: String) {
            mMainAdapter!!.onDeletePassword(id)
            initView()
        }

        override fun onUpdatePassword(newPassword: Password) {
            mMainAdapter!!.onUpdatePassword(newPassword)
            initView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainAdapter = PasswordListAdapter(activity)
        mMainBinder!!.registOnPasswordListener(onPasswordListener)
        showPasswordGroup(baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME,
                getString(R.string.default_password_group_name)))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregistOnPasswordListener()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val rootView = inflater.inflate(R.layout.fragment_password_list, container, false)
        mListView = rootView.findViewById<View>(R.id.fragment_password_listView) as ListView
        mListView!!.adapter = mMainAdapter

        mNoDataView = rootView.findViewById(R.id.fragment_password_noData)
        mNoDataView!!.setOnClickListener(this)
        if (mMainBinder == null) {
            mNoDataView!!.visibility = View.GONE
            mListView!!.visibility = View.VISIBLE
        } else {
            initView()
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mListView = null
        mNoDataView = null
    }

    override fun onGetAllPassword(groupName: String, passwords: List<Password>) {
        if (passwordGroupName == groupName) {
            mMainAdapter!!.setPasswordGroup(passwordGroupName!!)
            mMainAdapter!!.setData(passwords, mMainBinder!!)
            initView()
            if (mListView != null)
                mListView!!.setSelection(0)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_password_noData -> {
                val intent = Intent(activity, EditPasswordActivity::class.java)
                intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroupName)
                activity.startActivity(intent)
            }
            else -> {
            }
        }
    }

    fun setDataSource(mainBinder: PasswordDBRealm) {
        this.mMainBinder = mainBinder
    }

    fun showPasswordGroup(passwordGroupName: String) {
        this.passwordGroupName = passwordGroupName
        mMainBinder!!.getAllPasswordByGroupName(passwordGroupName, this)
    }

    private fun unregistOnPasswordListener() {
        if (mMainBinder != null) {
            mMainBinder!!.unregistOnPasswordListener(onPasswordListener)
            mMainBinder = null
        }
    }

    private fun initView() {
        if (mNoDataView != null) {
            if (mMainAdapter!!.count == 0) {
                mNoDataView!!.visibility = View.VISIBLE
                mListView!!.visibility = View.GONE
            } else {
                mNoDataView!!.visibility = View.GONE
                mListView!!.visibility = View.VISIBLE
            }
        }
    }
}
