package org.tianjyan.pwd.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.home.young.myPassword.R
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.PasswordGroup

class AddPasswordGroupDialog(context: Context, private val mMainBinder: PasswordDBRealm) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
    @BindView(R.id.create_passwordGroup_name) internal var mEditText: EditText? = null

    @OnClick(R.id.create_passwordGroup_cancel)
    fun cancelClick() {
        dismiss()
    }

    @OnClick(R.id.create_passwordGroup_sure)
    fun sureClick() {
        val name = mEditText!!.text.toString().trim { it <= ' ' }
        if (name.isNotEmpty()) {
            val passwordGroup = PasswordGroup()
            passwordGroup.groupName = name
            mMainBinder.addPasswordGroup(passwordGroup)
            dismiss()
        }
    }

    init {
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_create_password_group)
        ButterKnife.bind(this)

        mEditText!!.requestFocus()
    }
}