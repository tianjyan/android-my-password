package org.tianjyan.pwd.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.home.young.myPassword.R
import org.tianjyan.pwd.application.BaseActivity
import org.tianjyan.pwd.application.MD5
import org.tianjyan.pwd.model.SettingKey

class StartActivity : BaseActivity(), TextView.OnEditorActionListener {

    private lateinit var inputPwd : EditText
    private lateinit var pwd: String
    private lateinit var noPwd: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        noPwd = super.getSetting(SettingKey.NO_LOCK_PWD, "false")
        pwd = super.getSetting(SettingKey.LOCK_PWD, "")
        if (noPwd == "true") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else if (pwd.isEmpty()) {
            val intent = Intent(this, SetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        inputPwd = findViewById(R.id.start_password)
        inputPwd.setOnEditorActionListener(this)
        inputPwd.requestFocus()
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        when (i) {
            EditorInfo.IME_ACTION_DONE -> if (pwd == MD5.getMD5(inputPwd.text.toString())) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }

        return true
    }
}