package com.home.young.myPassword.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.home.young.myPassword.R;
import com.home.young.myPassword.model.PasswordGroup;

public class PasswordGroupAdapter extends BaseAdapter {

    //region field
    private List<PasswordGroup> mPasswordGroups = new ArrayList<>();
    private Context mContext;
    private String mCurrentGroupName;
    //endregion

    //region constructor
    public PasswordGroupAdapter(Context context) {
        super();
        this.mContext = context;
    }
    //endregion

    //region function

    //region override
    @Override
    public int getCount() {
        return mPasswordGroups.size();
    }

    @Override
    public PasswordGroup getItem(int position) {
        return mPasswordGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.password_group_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.fragment_password_group_name);
            viewHolder.arrowView = convertView.findViewById(R.id.fragment_password_group_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PasswordGroup passwordGroup = getItem(position);
        viewHolder.name.setText(passwordGroup.getGroupName());

        if (passwordGroup.getGroupName().equals(mCurrentGroupName)) {
            viewHolder.arrowView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.arrowView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
    //endregion

    //region public
    public String getCurrentGroupName() {
        return mCurrentGroupName;
    }

    public void setCurrentGroupName(String currentGroupName) {
        this.mCurrentGroupName = currentGroupName;
        notifyDataSetChanged();
    }

    public void setData(List<PasswordGroup> passwordGroups) {
        this.mPasswordGroups.clear();
        this.mPasswordGroups.addAll(passwordGroups);
        notifyDataSetChanged();
    }

    public void addPasswordGroup(PasswordGroup passwordGroup) {
        mPasswordGroups.add(passwordGroup);
        notifyDataSetChanged();
    }

    public boolean removePasswordGroup(String passwordGroupName) {
        boolean result = false;
        for (int i = 0; i < mPasswordGroups.size(); i++) {
            PasswordGroup passwordGroup = mPasswordGroups.get(i);
            if (passwordGroup.getGroupName().equals(passwordGroupName)) {
                result = true;
                mPasswordGroups.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
        return result;
    }
    //endregion

    //endregion

    //region nested class
    private class ViewHolder {
        TextView name;
        View arrowView;
    }
    //endregion
}
