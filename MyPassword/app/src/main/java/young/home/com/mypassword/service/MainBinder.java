package young.home.com.mypassword.service;

import android.content.Context;
import android.os.Binder;

import java.util.ArrayList;
import java.util.List;

import young.home.com.mypassword.application.App;
import young.home.com.mypassword.database.PasswordDatabase;
import young.home.com.mypassword.model.AsyncResult;
import young.home.com.mypassword.model.AsyncSingleTask;
import young.home.com.mypassword.model.Password;
import young.home.com.mypassword.model.PasswordGroup;
import young.home.com.mypassword.model.SettingKey;
import young.home.com.mypassword.service.Task.GetAllPasswordTask;

/**
 * Created by YOUNG on 2016/4/9.
 */
public class MainBinder extends Binder {
    private App app;
    private PasswordDatabase passwordDatabase;

    private List<OnPasswordChangeListener> onPasswordListeners = new ArrayList<OnPasswordChangeListener>();
    private List<OnPasswordGroupChangeListener> onPasswordGroupListeners = new ArrayList<OnPasswordGroupChangeListener>();

    public MainBinder(Context context, App app){
        passwordDatabase = new PasswordDatabase(context);
        this.app = app;
        final String key = app.getSetting(SettingKey.KEY, "");
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                passwordDatabase.setEncryptKey(key);
                passwordDatabase.getWritableDatabase();
                return asyncResult;
            }
        }.execute();
    }

    public void onDestroy(){
        passwordDatabase.close();
        new AsyncSingleTask<Void>(){
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                super.runOnUIThread(asyncResult);
                onPasswordListeners.clear();
                onPasswordGroupListeners.clear();
            }
        }.execute();
    }

    public void registOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordGroupListeners.add(onPasswordGroupListener);
            }
        }.execute();
    }

    public void unregistOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordGroupListeners.remove(onPasswordGroupListener);
            }
        }.execute();
    }

    public void registOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordListeners.add(onPasswordListener);
            }
        }.execute();
    }

    public void unregistOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordListeners.remove(onPasswordListener);
            }
        }.execute();
    }

    public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback, String groupName) {
        GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(passwordDatabase, onGetAllPasswordCallback,
                groupName);
        getAllPasswordTask.execute();
    }

    public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback) {
        GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(passwordDatabase, onGetAllPasswordCallback, null);
        getAllPasswordTask.execute();
    }

    public void deletePassword(final int id) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int result = passwordDatabase.deletePassword(id);
                asyncResult.setResult(result);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onDeletePassword(id);
                }
            }
        }.execute();
    }

    public void updatePassword(final Password password) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int result = passwordDatabase.updatePassword(password);
                asyncResult.setResult(result);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onUpdatePassword(password);
                }
            }
        }.execute();
    }

    public void insertPassword(final Password password) {
        new AsyncSingleTask<Password>() {
            @Override
            protected AsyncResult<Password> doInBackground(AsyncResult<Password> asyncResult) {
                String newGroupName = password.getGroupName();

                /** 是否是新的分组 */
                boolean isNew = true;
                List<PasswordGroup> passwordGroups = passwordDatabase.getAllPasswordGroup();
                for (int i = 0; i < passwordGroups.size(); i++) {
                    PasswordGroup passwordGroup = passwordGroups.get(i);
                    if (passwordGroup.getGroupName().equals(newGroupName)) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    // 不存在的分组，添加
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(newGroupName);
                    passwordDatabase.addPasswordGroup(passwordGroup);
                }
                asyncResult.getBundle().putBoolean("isNew", isNew);

                int result = (int) passwordDatabase.insertPassword(password);
                password.setId(result);
                asyncResult.setData(password);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Password> asyncResult) {
                if (asyncResult.getBundle().getBoolean("isNew")) {
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(asyncResult.getData().getGroupName());

                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onNewPasswordGroup(passwordGroup);
                    }
                }

                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onNewPassword(asyncResult.getData());
                }
            }
        }.execute();
    }

    public void insertPasswordGroup(final PasswordGroup passwordGroup) {
        new AsyncSingleTask<PasswordGroup>() {
            @Override
            protected AsyncResult<PasswordGroup> doInBackground(AsyncResult<PasswordGroup> asyncResult) {
                String newGroupName = passwordGroup.getGroupName();

                boolean isNew = true;
                List<PasswordGroup> passwordGroups = passwordDatabase.getAllPasswordGroup();
                for (int i = 0; i < passwordGroups.size(); i++) {
                    PasswordGroup passwordGroup = passwordGroups.get(i);
                    if (passwordGroup.getGroupName().equals(newGroupName)) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(newGroupName);
                    passwordDatabase.addPasswordGroup(passwordGroup);
                }
                asyncResult.getBundle().putBoolean("isNew", isNew);
                asyncResult.setData(passwordGroup);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<PasswordGroup> asyncResult) {
                if (asyncResult.getBundle().getBoolean("isNew")) {
                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onNewPasswordGroup(asyncResult.getData());
                    }
                }
            }
        }.execute();
    }

    /**
     * 删除密码分组，包括密码分住下的所有密码都会被删除
     *
     * @param passwordGroupName 分组名
     */
    public void deletePasswordgroup(final String passwordGroupName) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int count = passwordDatabase.deletePasswordGroup(passwordGroupName);
                asyncResult.setResult(count);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                if (asyncResult.getResult() > 0) {
                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onDeletePasswordGroup(passwordGroupName);
                    }
                }
            }
        }.execute();
    }

    public void updatePasswdGroupName(final String oldGroupName, final String newGroupName) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                passwordDatabase.updatePasswordGroupName(oldGroupName, newGroupName);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                    onPasswordGroupListener.onUpdateGroupName(oldGroupName, newGroupName);
                }
            }
        }.execute();
    }

    public void getAllPasswordGroup(final OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback) {
        new AsyncSingleTask<List<PasswordGroup>>() {
            @Override
            protected AsyncResult<List<PasswordGroup>> doInBackground(AsyncResult<List<PasswordGroup>> asyncResult) {
                List<PasswordGroup> list = passwordDatabase.getAllPasswordGroup();
                asyncResult.setData(list);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<List<PasswordGroup>> asyncResult) {
                onGetAllPasswordGroupCallback.onGetAllPasswordGroup(asyncResult.getData());
            }
        }.execute();
    }
}
