package com.home.young.myPassword.service.Task;

import java.util.List;

import com.home.young.myPassword.database.PasswordDatabase;
import com.home.young.myPassword.model.AsyncResult;
import com.home.young.myPassword.model.AsyncSingleTask;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class GetAllPasswordTask extends AsyncSingleTask<List<Password>> {
    private PasswordDatabase passwordDatabase;
    private OnGetAllPasswordCallback onGetAllPasswordCallback;
    private String groupName;

    public GetAllPasswordTask(PasswordDatabase passwordDatabase, OnGetAllPasswordCallback onGetAllPasswordCallback,
                              String groupName) {
        this.passwordDatabase = passwordDatabase;
        this.onGetAllPasswordCallback = onGetAllPasswordCallback;
        this.groupName = groupName;
    }

    @Override
    protected AsyncResult<List<Password>> doInBackground(AsyncResult<List<Password>> asyncResult) {
        List<Password> passwords;
        if (groupName == null)
            passwords = passwordDatabase.getAllPassword();
        else
            passwords = passwordDatabase.getAllPasswordByGroupName(groupName);
        asyncResult.setData(passwords);
        return asyncResult;
    }

    @Override
    protected void runOnUIThread(AsyncResult<List<Password>> asyncResult) {
        onGetAllPasswordCallback.onGetAllPassword(groupName, asyncResult.getData());
    }
}
