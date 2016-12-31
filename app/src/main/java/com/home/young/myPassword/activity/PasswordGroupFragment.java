package com.home.young.myPassword.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.home.young.myPassword.R;
import com.home.young.myPassword.adapter.PasswordGroupAdapter;
import com.home.young.myPassword.application.BaseActivity;
import com.home.young.myPassword.model.SettingKey;
import com.home.young.myPassword.service.MainBinder;
import com.home.young.myPassword.service.OnGetAllPasswordGroupCallback;
import com.home.young.myPassword.service.OnPasswordGroupChangeListener;
import com.home.young.myPassword.service.OnPasswordGroupSelected;

public class PasswordGroupFragment  extends Fragment implements AdapterView.OnItemClickListener, OnGetAllPasswordGroupCallback {

    //region field
    private MainBinder mainBinder;
    private PasswordGroupAdapter passwordGroupAdapter;
    private OnPasswordGroupSelected onPasswordGroupSelected;
    //endregion

    //region anonymous class
    private View.OnClickListener onAddClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            AddPasswordGroupDialog dialog = new AddPasswordGroupDialog(getActivity(), mainBinder);
            dialog.show();
        }
    };

    private OnPasswordGroupChangeListener onPasswordGroupListener = new OnPasswordGroupChangeListener() {
        @Override
        public void onNewPasswordGroup(PasswordGroup passwordGroup) {
            passwordGroupAdapter.addPasswordGroup(passwordGroup);
            if (passwordGroupAdapter.getCount() == 1) {
                selectItem(passwordGroup.getGroupName());
            }
        }

        @Override
        public void onDeletePasswordGroup(String passwordGroupName) {
            boolean result = passwordGroupAdapter.removePasswordGroup(passwordGroupName);
            if (result && passwordGroupName.equals(passwordGroupAdapter.getCurrentGroupName())) {
                String selectedname = "";
                if (passwordGroupAdapter.getCount() > 0)
                    selectedname = passwordGroupAdapter.getItem(0).getGroupName();

                selectItem(selectedname);
            }
        }

        @Override
        public void onUpdateGroupName(String oldGroupName, String newGroupName) {
            int count = passwordGroupAdapter.getCount();
            boolean hasMerge = false;
            for (int i = 0; i < count; i++) {
                PasswordGroup item = passwordGroupAdapter.getItem(i);
                if (item.getGroupName().equals(newGroupName)) {
                    hasMerge = true;
                    break;
                }
            }

            if (hasMerge) {
                // 有合并的， 移除老的分组
                for (int i = 0; i < count; i++) {
                    PasswordGroup item = passwordGroupAdapter.getItem(i);
                    if (item.getGroupName().equals(oldGroupName)) {
                        passwordGroupAdapter.removePasswordGroup(oldGroupName);
                        break;
                    }
                }

            } else {
                /** 分组变化了，改变现在的分组名称 */
                for (int i = 0; i < count; i++) {
                    PasswordGroup item = passwordGroupAdapter.getItem(i);
                    if (item.getGroupName().equals(oldGroupName)) {
                        item.setGroupName(newGroupName);
                        passwordGroupAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            // 当前选中的名称变了 重新加载
            if (passwordGroupAdapter.getCurrentGroupName().equals(oldGroupName)
                    || passwordGroupAdapter.getCurrentGroupName().equals(newGroupName)) {
                selectItem(newGroupName);
            }
        }
    };

    private AdapterView.OnItemLongClickListener onDeleteClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // 长按删除密码
            final String passwordGroupName = ((PasswordGroup) (parent.getItemAtPosition(position))).getGroupName();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            CharSequence[] items = new String[]{getString(R.string.edit_password_group_name),
                    getString(R.string.merge_password_group), getString(R.string.delete_password_group)};

            builder.setItems(items, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // 修改分组名
                            UpdatePasswordGroupNameDialog updatePasswdGroupName = new UpdatePasswordGroupNameDialog(
                                    getActivity(), passwordGroupName, mainBinder);
                            updatePasswdGroupName.show();
                            break;

                        case 1:
                            mergeGroup(passwordGroupName);
                            break;

                        case 2:
                            // 删除分组
                            showDeleteDialog(passwordGroupName);
                            break;
                    }
                }

            });
            builder.show();
            return true;
        }

    };

    //endregion

    //region function

    //region override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordGroupAdapter = new PasswordGroupAdapter(getActivity());
        mainBinder.registOnPasswordGroupListener(onPasswordGroupListener);
        mainBinder.getAllPasswordGroup(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainBinder.unregistOnPasswordGroupListener(onPasswordGroupListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_group, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.fragment_password_group_listView);
        listView.setAdapter(passwordGroupAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(onDeleteClickListener);
        View addView = rootView.findViewById(R.id.fragment_password_group_add);
        addView.setOnClickListener(onAddClickListener);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PasswordGroup passwordGroup = passwordGroupAdapter.getItem(position);
        selectItem(passwordGroup.getGroupName());
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        BaseActivity baseActivity = getBaseActivity();
        if (baseActivity != null) {
            String lastGroupName = baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME,
                    getString(R.string.default_password_group_name));

            passwordGroupAdapter.setCurrentGroupName(lastGroupName);

            passwordGroupAdapter.setData(passwordGroups);
        }
    }
    //endregion

    //region private
    private void mergeGroup(final String passwordGroupName) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.merge_password_group_loading));
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        // 获取分组回调
        OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback = new OnGetAllPasswordGroupCallback() {
            @Override
            public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
                progressDialog.dismiss();
                // 分组获取成功

                if (passwordGroups.size() <= 1) {
                    getBaseActivity().showToast(R.string.merge_password_group_error);
                    return;
                }

                // 用户选择需要合并到的分组
                final List<String> items = new ArrayList<>();
                for (PasswordGroup passwordGroup : passwordGroups) {
                    if (!passwordGroup.getGroupName().equals(passwordGroupName)) {
                        items.add(passwordGroup.getGroupName());
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items.toArray(new String[items.size()]),
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newGroupName = items.get(which);
                                mainBinder.updatePasswdGroupName(passwordGroupName, newGroupName);
                            }
                        });
                builder.show();
            }
        };

        // 获取所有的分组
        mainBinder.getAllPasswordGroup(onGetAllPasswordGroupCallback);
    }

    private void showDeleteDialog(final String passwordGroupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_password_group_msg, passwordGroupName));
        builder.setTitle(R.string.delete_password_group);
        builder.setNeutralButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainBinder.deletePasswordgroup(passwordGroupName);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    public void setDataSource(MainBinder mainBinder, OnPasswordGroupSelected onPasswordGroupSelected) {
        this.mainBinder = mainBinder;
        this.onPasswordGroupSelected = onPasswordGroupSelected;
    }

    private BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    private void selectItem(String selectedname) {
        BaseActivity baseActivity = getBaseActivity();
        baseActivity.putSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME, selectedname);

        passwordGroupAdapter.setCurrentGroupName(selectedname);
        onPasswordGroupSelected.onPasswordGroupSelected(selectedname);
    }
    //endregion

    //endregion
}
