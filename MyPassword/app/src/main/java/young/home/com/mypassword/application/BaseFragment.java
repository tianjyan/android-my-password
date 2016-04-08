package young.home.com.mypassword.application;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by YOUNG on 2016/4/7.
 */
public class BaseFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
