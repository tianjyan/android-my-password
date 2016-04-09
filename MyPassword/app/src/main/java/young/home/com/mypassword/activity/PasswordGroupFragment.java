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

    private View.OnClickListener onAddClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            AddPasswordGroupDialog dialog = new AddPasswordGroupDialog(getActivity(),null);
            dialog.show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_group, container, false);
        View addView = rootView.findViewById(R.id.fragment_password_group_add);
        addView.setOnClickListener(onAddClickListener);

        return rootView;
    }
}
