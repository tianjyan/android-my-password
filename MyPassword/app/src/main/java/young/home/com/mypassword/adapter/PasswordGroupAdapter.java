package young.home.com.mypassword.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import young.home.com.mypassword.R;
import young.home.com.mypassword.model.PasswordGroup;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class PasswordGroupAdapter extends BaseAdapter {

    //region field
    private List<PasswordGroup> passwordGroups = new ArrayList<PasswordGroup>();
    private Context context;
    private String currentGroupName;
    //endregion

    //region constructor
    public PasswordGroupAdapter(Context context) {
        super();
        this.context = context;
    }
    //endregion

    //region function

    //region override
    @Override
    public int getCount() {
        return passwordGroups.size();
    }

    @Override
    public PasswordGroup getItem(int position) {
        return passwordGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.password_group_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.fragment_password_group_nameView);
            viewHolder.arrowView = convertView.findViewById(R.id.fragment_password_group_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PasswordGroup passwordGroup = getItem(position);
        viewHolder.name.setText(passwordGroup.getGroupName());

        if (passwordGroup.getGroupName().equals(currentGroupName)) {
            viewHolder.arrowView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.arrowView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
    //endregion

    //region public
    public String getCurrentGroupName() {
        return currentGroupName;
    }

    public void setCurrentGroupName(String currentGroupName) {
        this.currentGroupName = currentGroupName;
        notifyDataSetChanged();
    }

    public void setData(List<PasswordGroup> passwordGroups) {
        this.passwordGroups.clear();
        this.passwordGroups.addAll(passwordGroups);
        notifyDataSetChanged();
    }

    public void addPasswordGroup(PasswordGroup passwordGroup) {
        passwordGroups.add(passwordGroup);
        notifyDataSetChanged();
    }

    public boolean removePasswordGroup(String passwordGroupName) {
        boolean result = false;
        for (int i = 0; i < passwordGroups.size(); i++) {
            PasswordGroup passwordGroup = passwordGroups.get(i);
            if (passwordGroup.getGroupName().equals(passwordGroupName)) {
                result = true;
                passwordGroups.remove(i);
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
