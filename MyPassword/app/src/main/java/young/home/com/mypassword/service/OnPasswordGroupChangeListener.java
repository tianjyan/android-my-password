package young.home.com.mypassword.service;

import young.home.com.mypassword.model.PasswordGroup;

/**
 * Created by YOUNG on 2016/4/9.
 */
public interface OnPasswordGroupChangeListener {
    void onNewPasswordGroup(PasswordGroup passwordGroup);
    void onDeletePasswordGroup(String passwordGroupName);
    void onUpdateGroupName(String oldGroupName, String newGroupName);
}
