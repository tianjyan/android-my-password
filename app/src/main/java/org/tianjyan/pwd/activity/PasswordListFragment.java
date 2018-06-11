package org.tianjyan.pwd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import org.tianjyan.pwd.R;
import org.tianjyan.pwd.adapter.PasswordListAdapter;
import org.tianjyan.pwd.application.BaseFragment;
import org.tianjyan.pwd.database.PasswordDBRealm;
import org.tianjyan.pwd.model.Password;
import org.tianjyan.pwd.model.SettingKey;
import org.tianjyan.pwd.service.OnGetAllPasswordCallback;
import org.tianjyan.pwd.service.OnPasswordChangeListener;

public class PasswordListFragment extends BaseFragment implements OnGetAllPasswordCallback,
        android.view.View.OnClickListener {

    //region field
    private PasswordListAdapter mMainAdapter;
    private PasswordDBRealm mMainBinder;
    private ListView mListView;
    private View mNoDataView;
    private String mPasswordGroupName;
    //endregion

    //region anonymous class
    private OnPasswordChangeListener onPasswordListener = new OnPasswordChangeListener() {
        @Override
        public void onNewPassword(Password password) {
            if (password.getGroupName().equals(mPasswordGroupName)) {
                mMainAdapter.onNewPassword(password);
                initView();
            }
        }

        @Override
        public void onDeletePassword(String id) {
            mMainAdapter.onDeletePassword(id);
            initView();
        }

        @Override
        public void onUpdatePassword(Password newPassword) {
            mMainAdapter.onUpdatePassword(newPassword);
            initView();
        }
    };
    //endregion

    //region function

    //region override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainAdapter = new PasswordListAdapter(getActivity());
        mMainBinder.registOnPasswordListener(onPasswordListener);
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
        mListView = (ListView) rootView.findViewById(R.id.fragment_password_listView);
        mListView.setAdapter(mMainAdapter);

        mNoDataView = rootView.findViewById(R.id.fragment_password_noData);
        mNoDataView.setOnClickListener(this);
        if (mMainBinder == null) {
            mNoDataView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            initView();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView = null;
        mNoDataView = null;
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        if (mPasswordGroupName.equals(groupName)) {
            mMainAdapter.setPasswordGroup(mPasswordGroupName);
            mMainAdapter.setData(passwords, mMainBinder);
            initView();
            if (mListView != null)
                mListView.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_password_noData:
                Intent intent = new Intent(getActivity(), EditPasswordActivity.class);
                intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordGroupName);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }
    //endregion

    //region private
    public void setDataSource(PasswordDBRealm mainBinder) {
        this.mMainBinder = mainBinder;
    }

    public void showPasswordGroup(String passwordGroupName) {
        this.mPasswordGroupName = passwordGroupName;
        mMainBinder.getAllPasswordByGroupName(passwordGroupName, this);
    }

    public String getPasswordGroupName() {
        return mPasswordGroupName;
    }

    private void unregistOnPasswordListener() {
        if (mMainBinder != null) {
            mMainBinder.unregistOnPasswordListener(onPasswordListener);
            mMainBinder = null;
        }
    }

    private void initView() {
        if (mNoDataView != null) {
            if (mMainAdapter.getCount() == 0) {
                mNoDataView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            } else {
                mNoDataView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }
    //endregion

    //endregion
}
