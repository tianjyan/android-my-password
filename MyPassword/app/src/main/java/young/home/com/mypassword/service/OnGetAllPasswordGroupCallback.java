package young.home.com.mypassword.service;

import java.util.List;

import young.home.com.mypassword.model.PasswordGroup;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnGetAllPasswordGroupCallback {
    void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
