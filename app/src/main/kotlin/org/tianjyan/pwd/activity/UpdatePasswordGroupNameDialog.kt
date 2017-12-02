package org.tianjyan.pwd.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.WindowManager
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.home.young.myPassword.R
import org.tianjyan.pwd.database.PasswordDBRealm

class UpdatePasswordGroupNameDialog(context: Context, private val oldGroupName: String, private val mainBinder: PasswordDBRealm) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar), Parcelable {
    @BindView(R.id.update_passwordGroup_name) internal var editText: EditText? = null

    constructor(parcel: Parcel) : this(
            TODO("context"),
            parcel.readString(),
            TODO("mainBinder")) {

    }

    @OnClick(R.id.update_passwordGroup_cancel)

    fun cancelClick() {
        dismiss()
    }

    @OnClick(R.id.update_passwordGroup_sure)
    fun sureClick() {
        val name = editText!!.text.toString().trim { it <= ' ' }
        if (name.length > 0) {
            if (name != oldGroupName) {
                mainBinder.updatePasswordGroup(oldGroupName, name)
            }
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_password_group_name)
        ButterKnife.bind(this)

        editText!!.setText(oldGroupName)
        editText!!.requestFocus()
    }

    init {
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(oldGroupName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpdatePasswordGroupNameDialog> {
        override fun createFromParcel(parcel: Parcel): UpdatePasswordGroupNameDialog {
            return UpdatePasswordGroupNameDialog(parcel)
        }

        override fun newArray(size: Int): Array<UpdatePasswordGroupNameDialog?> {
            return arrayOfNulls(size)
        }
    }
}

