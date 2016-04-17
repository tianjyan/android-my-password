package young.home.com.mypassword.service;

import java.util.List;

import young.home.com.mypassword.model.Password;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String groupName, List<Password> passwords);
}
