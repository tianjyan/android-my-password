package com.home.young.myPassword.application;

import android.app.Fragment;
public class BaseFragment extends Fragment {

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    protected void showToast(int resId) {
        getBaseActivity().showToast(resId);
    }

    protected void showToast(int resId, int duration) {
        getBaseActivity().showToast(resId, duration);
    }
}
