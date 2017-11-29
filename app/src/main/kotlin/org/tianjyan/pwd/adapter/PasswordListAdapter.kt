package org.tianjyan.pwd.adapter

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.home.young.myPassword.R
import com.home.young.myPassword.activity.EditPasswordActivity
import org.tianjyan.pwd.database.PasswordDBRealm
import org.tianjyan.pwd.model.Password
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class PasswordListAdapter(private val mContext: Context) : BaseAdapter() {
    private val mPasswords = ArrayList<PasswordItem>()
    private val mSimpleDateFormatYear = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val mPadding: Int
    private var mMainBinder: PasswordDBRealm? = null
    private val mSimpleDateFormatMonth = SimpleDateFormat("MM-dd", Locale.getDefault())
    private var mPasswordGroup: String? = null

    private var comparator = Comparator<PasswordItem> { lhs, rhs ->
        val value = rhs.password.publish - lhs.password.publish

        if (value > 0)
            1
        else if (value == 0L)
            0
        else
            -1
    }

    init {
        mPadding = dip2px(NUMBER_6.toFloat())
    }

    override fun getCount(): Int {
        return mPasswords.size
    }

    override fun getItem(position: Int): PasswordItem {
        return mPasswords[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun notifyDataSetChanged() {
        for (passwordItem in mPasswords) {
            passwordItem.initDataString()
        }
        super.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView = LayoutInflater.from(mContext).inflate(R.layout.password_item, parent, false)
            convertView!!.tag = viewHolder

            viewHolder.mTitleView = convertView.findViewById<View>(R.id.password_item_title) as TextView
            viewHolder.mDateView = convertView.findViewById<View>(R.id.password_item_date) as TextView
            viewHolder.mNameView = convertView.findViewById<View>(R.id.password_item_name) as TextView
            viewHolder.mPasswordView = convertView.findViewById<View>(R.id.password_item_password) as TextView
            viewHolder.mPayPasswordView = convertView.findViewById<View>(R.id.password_item_pay_password) as TextView
            viewHolder.mPayConainer = convertView.findViewById(R.id.password_item_pay_container)
            viewHolder.mNoteView = convertView.findViewById<View>(R.id.password_item_note) as TextView
            viewHolder.mNoteConainer = convertView.findViewById(R.id.password_item_note_container)
            viewHolder.mCopyView = convertView.findViewById(R.id.password_item_copy)
            viewHolder.mDeleteView = convertView.findViewById(R.id.password_item_delete)
            viewHolder.mEditView = convertView.findViewById(R.id.password_item_edit)
            viewHolder.mShowOrHideView = convertView.findViewById(R.id.password_item_showOrHide)
            viewHolder.mShowOrHideTextView = convertView.findViewById<View>(R.id.password_item_showOrHide_text) as TextView
            viewHolder.mCopyView!!.setOnClickListener(viewHolder)
            viewHolder.mDeleteView!!.setOnClickListener(viewHolder)
            viewHolder.mEditView!!.setOnClickListener(viewHolder)
            viewHolder.mShowOrHideView!!.setOnClickListener(viewHolder)

        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        if (position == 0) {
            convertView.setPadding(mPadding, mPadding, mPadding, mPadding)
        } else {
            convertView.setPadding(mPadding, 0, mPadding, mPadding)
        }

        val passwordItem = getItem(position)

        viewHolder.bindView(passwordItem)

        return convertView
    }

    fun dip2px(dipValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dipValue * scale + NUMBER_DOT5).toInt()
    }

    fun setData(passwords: List<Password>, mainBinder: PasswordDBRealm) {
        this.mMainBinder = mainBinder
        this.mPasswords.clear()
        for (password in passwords) {
            this.mPasswords.add(PasswordItem(password))
        }
        Collections.sort(this.mPasswords, comparator)
        notifyDataSetChanged()
    }

    fun onNewPassword(password: Password) {
        mPasswords.add(0, PasswordItem(password))
        Collections.sort(this.mPasswords, comparator)
        notifyDataSetChanged()
    }

    fun onDeletePassword(id: String) {
        for (i in mPasswords.indices) {
            val passwordItem = mPasswords[i]
            if (passwordItem.password.id == id) {
                mPasswords.removeAt(i)
                break
            }
        }
        notifyDataSetChanged()
    }

    fun onUpdatePassword(newPassword: Password) {
        var needSort = false

        var hasFind = false

        for (i in mPasswords.indices) {
            val oldPassword = mPasswords[i].password
            if (oldPassword.id == newPassword.id) {
                if (newPassword.publish != 0L)
                    oldPassword.publish = newPassword.publish
                if (newPassword.title != null)
                    oldPassword.title = newPassword.title
                if (newPassword.userName != null)
                    oldPassword.userName = newPassword.userName
                if (newPassword.password != null)
                    oldPassword.password = newPassword.password
                if (newPassword.note != null)
                    oldPassword.note = newPassword.note
                if (newPassword.payPassword != null)
                    oldPassword.payPassword = newPassword.payPassword

                if (oldPassword.groupName != newPassword.groupName)
                    mPasswords.removeAt(i)
                hasFind = true
                break
            }
        }

        if (!hasFind) {
            mPasswords.add(0, PasswordItem(newPassword))
            needSort = true
        }

        if (needSort)
            Collections.sort(this.mPasswords, comparator)
        notifyDataSetChanged()
    }

    fun setPasswordGroup(passwordGroup: String) {
        this.mPasswordGroup = passwordGroup
    }

    inner class PasswordItem constructor(var password: Password) {
        var dataString: String = ""

        init {
            initDataString()
        }

        fun initDataString() {
            dataString = formatDate(password.publish)
        }

        private fun formatDate(createDate: Long): String {
            val result: String
            val currentTime = System.currentTimeMillis()
            val distance = currentTime - createDate
            if (createDate > currentTime) {
                result = mSimpleDateFormatYear.format(createDate)
            } else if (distance < MINUTE) {
                result = mContext.getString(R.string.just)
            } else if (distance < HOUR) {
                val dateString = mContext.getString(R.string.minute_ago)
                result = String.format(Locale.getDefault(), dateString, distance / MINUTE)
            } else if (distance < DAY) {
                val dateString = mContext.getString(R.string.hour_ago)
                result = String.format(Locale.getDefault(), dateString, distance / HOUR)
            } else if (distance < DAY * YEAR) {
                result = mSimpleDateFormatMonth.format(createDate)
            } else {
                result = mSimpleDateFormatYear.format(createDate)
            }

            return result
        }
    }

    private inner class ViewHolder : android.view.View.OnClickListener {

        var mTitleView: TextView? = null
        var mDateView: TextView? = null
        var mNameView: TextView? = null
        var mPasswordView: TextView? = null
        var mPayPasswordView: TextView? = null
        var mNoteView: TextView? = null
        var mNoteConainer: View? = null
        var mPayConainer: View? = null
        var mCopyView: View? = null
        var mDeleteView: View? = null
        var mEditView: View? = null
        var mShowOrHideView: View? = null
        private var mPasswordItem: PasswordItem? = null
        var mShowOrHideTextView: TextView? = null
        override fun onClick(view: View) {
            when (view.id) {
                R.id.password_item_copy -> onCopyClick()
                R.id.password_item_delete -> onDeleteClick()
                R.id.password_item_edit -> onEditClick()
                R.id.password_item_showOrHide -> onShowOrHideClick()
                else -> {
                }
            }
        }

        private fun onCopyClick() {
            val builder = AlertDialog.Builder(mContext)

            val item: Array<String>
            val payPassword = mPasswordItem!!.password.payPassword
            if (!TextUtils.isEmpty(payPassword)) {
                item = arrayOf(mContext.resources.getString(R.string.copy_user_name), mContext.resources.getString(R.string.copy_password), mContext.resources.getString(R.string.copy_pay_password))
            } else {
                item = arrayOf(mContext.resources.getString(R.string.copy_user_name), mContext.resources.getString(R.string.copy_password))
            }

            builder.setItems(item) { dialog, which ->
                when (which) {
                    0 -> {
                        // 复制名字
                        val cmbName = mContext
                                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipDataName = ClipData.newPlainText(null, mPasswordItem!!.password.userName)
                        cmbName.primaryClip = clipDataName
                        Toast.makeText(mContext, R.string.copy_use_name_msg, Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // 复制密码
                        val cmbPassword = mContext
                                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText(null, mPasswordItem!!.password.password)
                        cmbPassword.primaryClip = clipData
                        Toast.makeText(mContext, R.string.copy_password_msg, Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        //复制支付密码
                        val cmbPayPassword = mContext
                                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(null, mPasswordItem!!.password.payPassword)
                        cmbPayPassword.primaryClip = clip
                        Toast.makeText(mContext, R.string.copy_pay_password_msg, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                    }
                }
            }
            builder.show()
        }

        private fun onEditClick() {
            val intent = Intent(mContext, EditPasswordActivity::class.java)
            intent.putExtra(EditPasswordActivity.ID, mPasswordItem!!.password.id)
            intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordGroup)
            mContext.startActivity(intent)
        }

        private fun onDeleteClick() {
            val builder = AlertDialog.Builder(mContext)
            builder.setMessage(R.string.delete_password_message)
            builder.setTitle(mPasswordItem!!.password.title)
            builder.setNeutralButton(R.string.yes) { dialog, which -> mMainBinder!!.deletePassword(mPasswordItem!!.password.id!!) }
            builder.setNegativeButton(R.string.no, null)
            builder.show()
        }

        private fun onShowOrHideClick() {
            if (mPasswordView!!.inputType == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD or EditorInfo.TYPE_CLASS_TEXT) {
                mPasswordView!!.inputType = EditorInfo.TYPE_CLASS_TEXT

                if (mPayConainer!!.visibility == View.VISIBLE) {
                    mPayPasswordView!!.inputType = EditorInfo.TYPE_CLASS_TEXT
                }

                mShowOrHideTextView!!.setText(R.string.hide)
            } else {
                mPasswordView!!.inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD or EditorInfo.TYPE_CLASS_TEXT

                if (mPayConainer!!.visibility == View.VISIBLE) {
                    mPayPasswordView!!.inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD or EditorInfo.TYPE_CLASS_TEXT
                }

                mShowOrHideTextView!!.setText(R.string.show)
            }
        }

        internal fun bindView(passwordItem: PasswordItem) {
            this.mPasswordItem = passwordItem
            mTitleView!!.text = passwordItem.password.title
            mDateView!!.text = passwordItem.dataString
            mNameView!!.text = passwordItem.password.userName
            mPasswordView!!.text = passwordItem.password.password

            val note = passwordItem.password.note
            if (TextUtils.isEmpty(note)) {
                mNoteConainer!!.visibility = View.GONE
            } else {
                mNoteConainer!!.visibility = View.VISIBLE
                mNoteView!!.text = note
            }

            val payPassword = passwordItem.password.payPassword
            if (TextUtils.isEmpty(payPassword)) {
                mPayConainer!!.visibility = View.GONE
            } else {
                mPayConainer!!.visibility = View.VISIBLE
                mPayPasswordView!!.text = payPassword
            }
        }
    }

    companion object {
        private val DAY = (1000 * 60 * 60 * 24).toLong()
        private val MINUTE = 1000 * 60
        private val HOUR = 1000 * 60 * 60
        private val YEAR = 365
        private val NUMBER_6 = 6
        private val NUMBER_DOT5 = 0.5f
    }
}