package org.tianjyan.pwd.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.home.young.myPassword.R
import org.tianjyan.pwd.application.BaseActivity
import org.tianjyan.pwd.application.MD5
import org.tianjyan.pwd.application.PwdGen
import org.tianjyan.pwd.model.SettingKey

class SetPasswordActivity : BaseActivity(), TextWatcher {
    @BindView(R.id.set_password_first) internal var pwdEt: EditText? = null
    @BindView(R.id.set_password_second) internal var rePwdEt: EditText? = null
    @BindView(R.id.set_password_next) internal var nextBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)
        ButterKnife.bind(this)

        pwdEt!!.addTextChangedListener(this)
        rePwdEt!!.addTextChangedListener(this)
        pwdEt!!.requestFocus()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        if (pwdEt!!.text.length > 0 && rePwdEt!!.text.length > 0) {
            nextBtn!!.isEnabled = true
        } else {
            nextBtn!!.isEnabled = false
        }
    }

    @OnClick(R.id.set_password_next)
    fun nextClick(v: View) {
        if (pwdEt!!.text.toString() == rePwdEt!!.text.toString()) {
            val pwd = pwdEt!!.text.toString()
            super.putSetting(SettingKey.LOCK_PWD, MD5.getMD5(pwd))
            super.putSetting(SettingKey.NO_LOCK_PWD, "false")
            genKey()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, R.string.different_password, Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.set_password_skip)
    fun skipClick(v: View) {
        super.putSetting(SettingKey.NO_LOCK_PWD, "true")
        genKey()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun genKey() {
        var key = super.getSetting(SettingKey.KEY, "")
        if (key == "") {
            key = PwdGen.generatePassword(LENGTH,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY,
                    PwdGen.Optionality.MANDATORY)
            super.putSetting(SettingKey.KEY, key)
        }
    }

    companion object {
        private val LENGTH = 8
    }
}
