package org.tianjyan.pwd.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.home.young.myPassword.R
import org.tianjyan.pwd.application.PwdGen

class GenPasswordDialog(context: Context) : Dialog(context) {
    @BindView(R.id.gen_password_lows) internal var lows: CheckBox? = null
    @BindView(R.id.gen_password_caps) internal var caps: CheckBox? = null
    @BindView(R.id.gen_password_numbers) internal var numbers: CheckBox? = null
    @BindView(R.id.gen_password_special) internal var special: CheckBox? = null
    @BindView(R.id.gen_password_len_eight) internal var eight: RadioButton? = null
    @BindView(R.id.gen_password_result) internal var result: EditText? = null

    lateinit var password: String
        internal set

    @OnClick(R.id.gen_password_cancel)
    fun cancelClick() {
        dismiss()
    }

    @OnClick(R.id.gen_password_sure)
    fun sureClick() {
        password = result!!.text.toString()
        dismiss()
    }

    @OnClick(R.id.gen_password_gen)
    fun genClick() {
        if (lows!!.isChecked || caps!!.isChecked || numbers!!.isChecked || special!!.isChecked) {

            val password = PwdGen.generatePassword(
                    if (eight!!.isChecked) LENGTH_8 else LENGTH_16,
                    if (lows!!.isChecked) PwdGen.Optionality.MANDATORY else PwdGen.Optionality.PROHIBITED,
                    if (caps!!.isChecked) PwdGen.Optionality.MANDATORY else PwdGen.Optionality.PROHIBITED,
                    if (numbers!!.isChecked) PwdGen.Optionality.MANDATORY else PwdGen.Optionality.PROHIBITED,
                    if (special!!.isChecked) PwdGen.Optionality.MANDATORY else PwdGen.Optionality.PROHIBITED)
            result!!.setText(password)
        } else {
            Toast.makeText(context, context.getString(R.string.gen_password_msg),
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_gen_password)
        ButterKnife.bind(this)
    }

    companion object {
        private val LENGTH_8 = 8
        private val LENGTH_16 = 16
    }
}
