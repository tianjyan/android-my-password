package org.tianjyan.pwd.activity

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.home.young.myPassword.R
import org.tianjyan.pwd.application.BaseActivity
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.Password
import org.tianjyan.pwd.model.PasswordGroup
import org.tianjyan.pwd.service.MainService
import org.tianjyan.pwd.service.OnGetAllPasswordCallback
import org.tianjyan.pwd.service.OnGetAllPasswordGroupCallback
import org.tianjyan.pwd.service.OnGetPasswordCallback
import java.util.ArrayList
import java.util.HashSet

class EditPasswordActivity : BaseActivity(), OnGetPasswordCallback, OnGetAllPasswordCallback, OnGetAllPasswordGroupCallback {

    private var mMode = MODE_ADD
    private var mId: String? = null
    private var mMainBinder: PasswordDBRealm? = null
    @BindView(R.id.edit_password_title) internal var mTitleView: EditText? = null
    @BindView(R.id.edit_password_name) internal var mNameView: AutoCompleteTextView? = null
    @BindView(R.id.edit_password_password) internal var mPasswordView: AutoCompleteTextView? = null
    @BindView(R.id.edit_password_pay_password) internal var mPayPasswordView: AutoCompleteTextView? = null
    @BindView(R.id.edit_password_note) internal var mNoteView: EditText? = null
    @BindView(R.id.edit_password_group) internal var mSpinner: Spinner? = null
    private var mPasswordGroup: String? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as PasswordDBRealm
            if (mMode == MODE_MODIFY) {
                mMainBinder!!.getPassword(mId!!, this@EditPasswordActivity)
            }
            // 获得所有密码、用户名，用于自动完成
            mMainBinder!!.getAllPasswordByGroupName(null, this@EditPasswordActivity)
            mMainBinder!!.getAllPasswordGroup(this@EditPasswordActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_password)
        ButterKnife.bind(this)

        mId = intent.getStringExtra(ID)
        if (TextUtils.isEmpty(mId)) {
            mMode = MODE_ADD
        } else {
            mMode = MODE_MODIFY
        }

        mPasswordGroup = intent.getStringExtra(PASSWORD_GROUP)

        if (mPasswordGroup == null || mPasswordGroup == "") {
            mPasswordGroup = getString(R.string.default_password_group_name)
        }

        initActionBar()

        val intent = Intent(this, MainService::class.java)
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_password, menu)
        if (mMode == MODE_ADD) {
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_action_ok)
        } else {
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_action_save)
            menu.findItem(R.id.action_delete).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            if (mMainBinder != null) {
                onSaveBtnClick()
            }
            return true
        } else if (id == R.id.action_delete) {
            if (mMainBinder != null) {
                deletePassword()
            }
            return true
        } else if (id == android.R.id.home) {
            finish()
            return true
        } else if (id == R.id.action_gen) {
            val dialog = GenPasswordDialog(activity)
            dialog.show()
            dialog.setOnDismissListener { dialog ->
                val result = (dialog as GenPasswordDialog).password
                if (result != null && result != "") {
                    if (mPasswordView!!.isFocused) {
                        mPasswordView!!.setText(result)
                    } else if (mPayPasswordView!!.isFocused) {
                        mPayPasswordView!!.setText(result)
                    }
                }
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onGetPassword(password: Password) {
        if (password == null) {
            Toast.makeText(this, R.string.deleted_password_message, Toast.LENGTH_SHORT).show()
            finish()
        }

        mTitleView!!.setText(password!!.title)
        mNameView!!.setText(password.userName)
        mPasswordView!!.setText(password.password)
        mPayPasswordView!!.setText(password.payPassword)
        mNoteView!!.setText(password.note)
        mTitleView!!.setSelection(mTitleView!!.text.length)
    }

    override fun onGetAllPassword(groupName: String, passwords: List<Password>) {
        // 去掉重复
        val arrays = HashSet<String>()
        for (i in passwords.indices) {
            val password = passwords[i]
            arrays.add(password.userName!!)
            arrays.add(password.password!!)
            arrays.add(password.payPassword!!)
        }

        // 自动完成
        val id = R.layout.simple_dropdown_item
        val arrayAdapter = ArrayAdapter(this, id, ArrayList(arrays))
        mNameView!!.setAdapter(arrayAdapter)
        mPasswordView!!.setAdapter(arrayAdapter)
        mPayPasswordView!!.setAdapter(arrayAdapter)
    }

    override fun onGetAllPasswordGroup(passwordGroups: List<PasswordGroup>) {
        val arrays = ArrayList<String>()

        for (i in passwordGroups.indices) {
            val passwordGroup = passwordGroups[i]
            arrays.add(passwordGroup.groupName!!)
        }

        if (!arrays.contains(mPasswordGroup))
            arrays.add(mPasswordGroup!!)

        var position = 0
        for (passwordGroupName in arrays) {
            if (passwordGroupName == mPasswordGroup)
                break
            position++
        }

        val id = R.layout.simple_dropdown_item
        val spinnerAdapter = ArrayAdapter(this, id, ArrayList(arrays))

        mSpinner!!.adapter = spinnerAdapter

        mSpinner!!.setSelection(position)
        mSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPasswordGroup = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowTitleEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        if (mMode == MODE_ADD) {
            actionBar.setTitle(R.string.add)
        } else {
            actionBar.setTitle(R.string.edit)
        }
    }

    private fun deletePassword() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_password_message)
        builder.setNeutralButton(R.string.yes) { dialog, which ->
            mMainBinder!!.deletePassword(mId!!)
            finish()
        }
        builder.setNegativeButton(R.string.no, null)
        builder.show()
    }

    private fun onSaveBtnClick() {
        if (mTitleView!!.text.toString().trim { it <= ' ' } == "") {
            Toast.makeText(this, R.string.empty_title, Toast.LENGTH_SHORT).show()
        } else {
            val password = Password()
            password.title = mTitleView!!.text.toString().trim { it <= ' ' }
            password.userName = mNameView!!.text.toString().trim { it <= ' ' }
            password.password = mPasswordView!!.text.toString().trim { it <= ' ' }
            password.payPassword = mPayPasswordView!!.text.toString().trim { it <= ' ' }
            password.note = mNoteView!!.text.toString().trim { it <= ' ' }
            password.groupName = mPasswordGroup
            password.publish = System.currentTimeMillis()
            if (mMode == MODE_ADD) {
                // 添加
                mMainBinder!!.addPassword(password)
            } else {
                // 修改密码
                password.id = mId
                mMainBinder!!.updatePassword(password)
            }
            finish()
        }
    }

    companion object {
        val ID = "password_id"
        val PASSWORD_GROUP = "password_group"
        private val MODE_ADD = 0
        private val MODE_MODIFY = 1
    }
}