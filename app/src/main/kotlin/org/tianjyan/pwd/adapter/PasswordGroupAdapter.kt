package org.tianjyan.pwd.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.home.young.myPassword.R
import com.home.young.myPassword.model.PasswordGroup
import java.util.ArrayList

class PasswordGroupAdapter(private val mContext: Context) : BaseAdapter() {
    private val mPasswordGroups = ArrayList<PasswordGroup>()

    var currentGroupName: String? = null
        set(currentGroupName) {
            field = currentGroupName
            notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return mPasswordGroups.size
    }

    override fun getItem(position: Int): PasswordGroup {
        return mPasswordGroups[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView = LayoutInflater.from(mContext).inflate(R.layout.password_group_item, null)
            viewHolder.name = convertView!!.findViewById<View>(R.id.fragment_password_group_name) as TextView
            viewHolder.arrowView = convertView.findViewById(R.id.fragment_password_group_arrow)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val passwordGroup = getItem(position)
        viewHolder.name!!.text = passwordGroup.groupName

        if (passwordGroup.groupName == currentGroupName) {
            viewHolder.arrowView!!.visibility = View.VISIBLE
        } else {
            viewHolder.arrowView!!.visibility = View.INVISIBLE
        }

        return convertView
    }

    fun setData(passwordGroups: List<PasswordGroup>) {
        this.mPasswordGroups.clear()
        this.mPasswordGroups.addAll(passwordGroups)
        notifyDataSetChanged()
    }

    fun addPasswordGroup(passwordGroup: PasswordGroup) {
        mPasswordGroups.add(passwordGroup)
        notifyDataSetChanged()
    }

    fun removePasswordGroup(passwordGroupName: String): Boolean {
        var result = false
        for (i in mPasswordGroups.indices) {
            val passwordGroup = mPasswordGroups[i]
            if (passwordGroup.groupName == passwordGroupName) {
                result = true
                mPasswordGroups.removeAt(i)
                break
            }
        }
        notifyDataSetChanged()
        return result
    }

    private inner class ViewHolder {
        internal var name: TextView? = null
        internal var arrowView: View? = null
    }
}