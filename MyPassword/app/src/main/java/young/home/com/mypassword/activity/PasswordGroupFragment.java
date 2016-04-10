package young.home.com.mypassword.activity;

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

import young.home.com.mypassword.R;
import young.home.com.mypassword.adapter.PasswordGroupAdapter;
import young.home.com.mypassword.application.BaseActivity;
import young.home.com.mypassword.model.PasswordGroup;
import young.home.com.mypassword.model.SettingKey;
import young.home.com.mypassword.service.MainBinder;
import young.home.com.mypassword.service.OnGetAllPasswordGroupCallback;
import young.home.com.mypassword.service.OnPasswordGroupChangeListener;
import young.home.com.mypassword.service.OnPasswordGroupSelected;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class PasswordGroupFragment  extends Fragment implements AdapterView.OnItemClickListener, OnGetAllPasswordGroupCallback {

    private MainBinder mainBinder;
    private PasswordGroupAdapter passwordGroupAdapter;
    private OnPasswordGroupSelected onPasswordGroupSelected;

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

            CharSequence[] items = new String[]{getString(R.string.password_group_update_group_name),
                    getString(R.string.password_group_merge), getString(R.string.password_group_delete_group)};

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

    /**
     * 合并分组
     *
     * @param passwordGroupName 原分组名
     */
    private void mergeGroup(final String passwordGroupName) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.password_group_merge_loading));
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
                    getBaseActivity().showToast(R.string.password_group_merge_error);
                    return;
                }

                // 用户选择需要合并到的分组
                final List<String> items = new ArrayList<String>();
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

    /**
     * 显示删除密码分组对话框
     *
     * @param passwordGroupName 要删除的密码分组
     */
    private void showDeleteDialog(final String passwordGroupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_password_group_message, passwordGroupName));
        builder.setTitle(R.string.delete_password_group_title);
        builder.setNeutralButton(R.string.delete_password_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainBinder.deletePasswordgroup(passwordGroupName);
            }
        });
        builder.setNegativeButton(R.string.delete_password_cancle, null);
        builder.show();
    }

    public void setDataSource(MainBinder mainBinder, OnPasswordGroupSelected onPasswordGroupSelected) {
        this.mainBinder = mainBinder;
        this.onPasswordGroupSelected = onPasswordGroupSelected;
    }

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

    private BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
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
            String lastGroupName = baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME,
                    getString(R.string.password_group_default_name));

            passwordGroupAdapter.setCurrentGroupName(lastGroupName);

            passwordGroupAdapter.setData(passwordGroups);
        }
    }

    private void selectItem(String selectedname) {
        BaseActivity baseActivity = getBaseActivity();
        baseActivity.putSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, selectedname);

        passwordGroupAdapter.setCurrentGroupName(selectedname);
        onPasswordGroupSelected.onPasswordGroupSelected(selectedname);
    }
}
