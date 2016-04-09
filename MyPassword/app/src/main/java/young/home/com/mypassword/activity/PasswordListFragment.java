package young.home.com.mypassword.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import young.home.com.mypassword.R;
import young.home.com.mypassword.application.BaseFragment;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class PasswordListFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_list, container, false);
        return rootView;
    }
}
