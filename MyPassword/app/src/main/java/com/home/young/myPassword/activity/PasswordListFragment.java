package com.home.young.myPassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import com.home.young.myPassword.R;
import com.home.young.myPassword.adapter.PasswordListAdapter;
import com.home.young.myPassword.application.BaseFragment;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.SettingKey;
import com.home.young.myPassword.service.MainBinder;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;
import com.home.young.myPassword.service.OnPasswordChangeListener;
import com.home.young.myPassword.service.OnSettingChangeListener;

public class PasswordListFragment extends BaseFragment implements OnGetAllPasswordCallback,
        android.view.View.OnClickListener {

    //region field
    private PasswordListAdapter mainAdapter;
    private MainBinder mainBinder;
    private ListView listView;
    private View noDataView;
    private String passwordGroupName;
    //endregion

    //region lambda
    private OnPasswordChangeListener onPasswordListener = new OnPasswordChangeListener() {
        @Override
        public void onNewPassword(Password password) {
            if (password.getGroupName().equals(passwordGroupName)) {
                mainAdapter.onNewPassword(password);
                initView();
            }
        }

        @Override
        public void onDeletePassword(int id) {
            mainAdapter.onDeletePassword(id);
            initView();
        }

        @Override
        public void onUpdatePassword(Password newPassword) {
            mainAdapter.onUpdatePassword(newPassword);
            initView();
        }
    };
    //endregion

    //region function

    //region override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainAdapter = new PasswordListAdapter(getActivity());
        mainBinder.registOnPasswordListener(onPasswordListener);
        showPasswordGroup(getBaseActivity().getSetting(SettingKey.LAST_SHOW_PASSWORD_GROUP_NAME,
                getString(R.string.default_password_group_name)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistOnPasswordListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.fragment_password_listView);
        listView.setAdapter(mainAdapter);

        noDataView = rootView.findViewById(R.id.fragment_password_noData);
        noDataView.setOnClickListener(this);
        if (mainBinder == null) {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            initView();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
        noDataView = null;
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        if (passwordGroupName.equals(groupName)) {
            mainAdapter.setPasswordGroup(passwordGroupName);
            mainAdapter.setData(passwords, mainBinder);
            initView();
            if (listView != null)
                listView.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_password_noData:
                Intent intent = new Intent(getActivity(), EditPasswordActivity.class);
                intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroupName);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }
    //endregion

    //region private
    public void setDataSource(MainBinder mainBinder) {
        this.mainBinder = mainBinder;
    }

    public void showPasswordGroup(String passwordGroupName) {
        this.passwordGroupName = passwordGroupName;
        mainBinder.getAllPassword(this, passwordGroupName);
    }

    public String getPasswordGroupName() {
        return passwordGroupName;
    }

    private void unregistOnPasswordListener() {
        if (mainBinder != null) {
            mainBinder.unregistOnPasswordListener(onPasswordListener);
            mainBinder = null;
        }
    }

    private void initView() {
        if (noDataView != null) {
            if (mainAdapter.getCount() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } else {
                noDataView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        }
    }
    //endregion

    //endregion
}
