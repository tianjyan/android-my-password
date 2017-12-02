package org.tianjyan.pwd.activity

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.home.young.myPassword.R
import org.tianjyan.pwd.adapter.PasswordGroupAdapter
import org.tianjyan.pwd.application.BaseActivity
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.PasswordGroup
import org.tianjyan.pwd.model.SettingKey
import org.tianjyan.pwd.service.OnGetAllPasswordGroupCallback
import org.tianjyan.pwd.service.OnPasswordGroupChangeListener
import org.tianjyan.pwd.service.OnPasswordGroupSelected
import java.util.ArrayList

class PasswordGroupFragment : Fragment(), AdapterView.OnItemClickListener, OnGetAllPasswordGroupCallback {
    private var mMainBinder: PasswordDBRealm? = null
    private var mPasswordGroupAdapter: PasswordGroupAdapter? = null
    private var mOnPasswordGroupSelected: OnPasswordGroupSelected? = null

    private val onAddClickListener = View.OnClickListener {
        val dialog = AddPasswordGroupDialog(activity, mMainBinder!!)
        dialog.show()
    }

    private val onPasswordGroupListener = object : OnPasswordGroupChangeListener {
        override fun onNewPasswordGroup(passwordGroup: PasswordGroup) {
            mPasswordGroupAdapter!!.addPasswordGroup(passwordGroup!!)
            if (mPasswordGroupAdapter!!.count == 1) {
                selectItem(passwordGroup.groupName!!)
            }
        }

        override fun onDeletePasswordGroup(passwordGroupName: String) {
            val result = mPasswordGroupAdapter!!.removePasswordGroup(passwordGroupName)
            if (result && passwordGroupName == mPasswordGroupAdapter!!.currentGroupName) {
                var selectedname = ""
                if (mPasswordGroupAdapter!!.count > 0)
                    selectedname = mPasswordGroupAdapter!!.getItem(0).groupName!!

                selectItem(selectedname)
            }
        }

        override fun onUpdateGroupName(oldGroupName: String, newGroupName: String) {
            val count = mPasswordGroupAdapter!!.count
            var hasMerge = false
            for (i in 0 until count) {
                val item = mPasswordGroupAdapter!!.getItem(i)
                if (item.groupName == newGroupName) {
                    hasMerge = true
                    break
                }
            }

            if (hasMerge) {
                // 有合并的， 移除老的分组
                for (i in 0 until count) {
                    val item = mPasswordGroupAdapter!!.getItem(i)
                    if (item.groupName == oldGroupName) {
                        mPasswordGroupAdapter!!.removePasswordGroup(oldGroupName)
                        break
                    }
                }

            } else {
                /** 分组变化了，改变现在的分组名称  */
                for (i in 0 until count) {
                    val item = mPasswordGroupAdapter!!.getItem(i)
                    if (item.groupName == oldGroupName) {
                        item.groupName = newGroupName
                        mPasswordGroupAdapter!!.notifyDataSetChanged()
                        break
                    }
                }
            }

            // 当前选中的名称变了 重新加载
            if (mPasswordGroupAdapter!!.currentGroupName == oldGroupName || mPasswordGroupAdapter!!.currentGroupName == newGroupName) {
                selectItem(newGroupName)
            }
        }
    }

    private val onDeleteClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
        // 长按删除密码
        val passwordGroupName = (parent.getItemAtPosition(position) as PasswordGroup).groupName
        val builder = AlertDialog.Builder(activity)

        val items = arrayOf(getString(R.string.edit_password_group_name), getString(R.string.merge_password_group), getString(R.string.delete_password_group))

        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> {
                    // 修改分组名
                    val updatePasswdGroupName = UpdatePasswordGroupNameDialog(
                            activity, passwordGroupName!!, mMainBinder!!)
                    updatePasswdGroupName.show()
                }

                1 -> mergeGroup(passwordGroupName!!)

                2 ->
                    // 删除分组
                    showDeleteDialog(passwordGroupName!!)
                else -> {
                }
            }
        }
        builder.show()
        true
    }

    private val baseActivity: BaseActivity?
        get() = activity as BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPasswordGroupAdapter = PasswordGroupAdapter(activity)
        mMainBinder!!.registOnPasswordGroupListener(onPasswordGroupListener)
        mMainBinder!!.getAllPasswordGroup(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainBinder!!.unregistOnPasswordGroupListener(onPasswordGroupListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val rootView = inflater.inflate(R.layout.fragment_password_group, container, false)
        val listView = rootView.findViewById<View>(R.id.fragment_password_group_listView) as ListView
        listView.adapter = mPasswordGroupAdapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = onDeleteClickListener
        val addView = rootView.findViewById<View>(R.id.fragment_password_group_add)
        addView.setOnClickListener(onAddClickListener)
        return rootView
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val passwordGroup = mPasswordGroupAdapter!!.getItem(position)
        selectItem(passwordGroup.groupName!!)
    }

    override fun onGetAllPasswordGroup(passwordGroups: List<PasswordGroup>) {
        val baseActivity = baseActivity
        if (baseActivity != null) {
            val lastGroupName = baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME,
                    getString(R.string.default_password_group_name))

            mPasswordGroupAdapter!!.currentGroupName = lastGroupName

            mPasswordGroupAdapter!!.setData(passwordGroups)
        }
    }

    private fun mergeGroup(passwordGroupName: String) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage(getString(R.string.merge_password_group_loading))
        progressDialog.setCancelable(true)
        progressDialog.isIndeterminate = false
        progressDialog.show()

        // 获取分组回调
        val onGetAllPasswordGroupCallback = object : OnGetAllPasswordGroupCallback {
            override fun onGetAllPasswordGroup(passwordGroups : List<PasswordGroup>) {
                progressDialog.dismiss()
                // 分组获取成功

                if (passwordGroups.size <= 1) {
                    baseActivity!!.showToast(R.string.merge_password_group_error)
                    return
                }

                // 用户选择需要合并到的分组
                val items = ArrayList<String>()
                for (passwordGroup in passwordGroups) {
                    if (passwordGroup.groupName != passwordGroupName) {
                        items.add(passwordGroup.groupName!!)
                    }
                }
                val builder = AlertDialog.Builder(activity)
                builder.setItems(items.toTypedArray()
                ) { dialog, which ->
                    val newGroupName = items[which]
                    mMainBinder!!.updatePasswordGroup(passwordGroupName, newGroupName)
                }
                builder.show()
            }
        }

        // 获取所有的分组
        mMainBinder!!.getAllPasswordGroup(onGetAllPasswordGroupCallback)
    }

    private fun showDeleteDialog(passwordGroupName: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.delete_password_group_msg, passwordGroupName))
        builder.setTitle(R.string.delete_password_group)
        builder.setNeutralButton(R.string.sure) { dialog, which -> mMainBinder!!.deletePasswordGroup(passwordGroupName) }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    fun setDataSource(mainBinder: PasswordDBRealm, onPasswordGroupSelected: OnPasswordGroupSelected) {
        this.mMainBinder = mainBinder
        this.mOnPasswordGroupSelected = onPasswordGroupSelected
    }

    private fun selectItem(selectedname: String) {
        val baseActivity = baseActivity
        baseActivity!!.putSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, selectedname)

        mPasswordGroupAdapter!!.currentGroupName = selectedname
        mOnPasswordGroupSelected!!.onPasswordGroupSelected(selectedname)
    }
}