package young.home.com.mypassword.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import young.home.com.mypassword.R;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class PasswordGroupFragment  extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_group, container, false);
        return rootView;
    }
}
