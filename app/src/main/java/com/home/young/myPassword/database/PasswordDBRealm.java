package com.home.young.myPassword.database;

import android.content.Context;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;

import com.home.young.myPassword.application.AES;
import com.home.young.myPassword.model.AsyncResult;
import com.home.young.myPassword.model.AsyncSingleTask;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.PasswordGroup;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;
import com.home.young.myPassword.service.OnGetAllPasswordGroupCallback;
import com.home.young.myPassword.service.OnGetPasswordCallback;
import com.home.young.myPassword.service.OnPasswordChangeListener;
import com.home.young.myPassword.service.OnPasswordGroupChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

//TODO: 添加版本的代码（RealmMigration must be provided）
public class PasswordDBRealm extends Binder{

    private static final String GROUPNAME = "groupName";
    private static final String TAG = "PasswordDBRealm";

    private Realm mRealm;
    private String encryptKey;

    public List<OnPasswordChangeListener> getOnPasswordListeners() {
        return onPasswordListeners;
    }

    public List<OnPasswordGroupChangeListener> getOnPasswordGroupListeners() {
        return onPasswordGroupListeners;
    }

    private List<OnPasswordChangeListener> onPasswordListeners = new ArrayList<>();
    private List<OnPasswordGroupChangeListener> onPasswordGroupListeners = new ArrayList<>();

    public PasswordDBRealm(Context context, String encryptKey) {
        Realm.init(context);
        Realm.setDefaultConfiguration(getConfiguration());
        this.mRealm = Realm.getDefaultInstance();
        this.encryptKey = encryptKey;
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

    private RealmConfiguration getConfiguration() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("mypasswordrealm.realm").deleteRealmIfMigrationNeeded().build();
        return realmConfiguration;
    }

    public void addPassword(final Password password) {
        mRealm.executeTransactionAsync(realm -> {
            password.setId(UUID.randomUUID().toString());
            password.setPassword(encrypt(password.getPassword()));
            password.setPayPassword(encrypt(password.getPayPassword()));
            //当导入密码时，需要插入分组
            PasswordGroup group = new PasswordGroup();
            group.setGroupName(password.getGroupName());
            realm.insertOrUpdate(group);

            realm.insertOrUpdate(password);
        }, () -> {
            password.setPassword(decrypt(password.getPassword()));
            password.setPayPassword(decrypt(password.getPayPassword()));
            callNewPassword(password);
        });
    }

    public void updatePassword(final Password password) {
        mRealm.executeTransactionAsync(realm -> {
            password.setPassword(encrypt(password.getPassword()));
            password.setPayPassword(encrypt(password.getPayPassword()));
            realm.insertOrUpdate(password);
        }, () -> {
            password.setPassword(decrypt(password.getPassword()));
            password.setPayPassword(decrypt(password.getPayPassword()));
            callUpdatePassword(password);
        });
    }

    public void deletePassword(final String id) {
        mRealm.executeTransactionAsync(realm -> {
            RealmResults<Password> realmResults = realm.where(Password.class).equalTo("id", id).findAll();
            if (realmResults.size() > 0) {
                realmResults.deleteAllFromRealm();
            }
        }, () -> callDeletePassword(id));
    }

    public void getPassword(final String id, final OnGetPasswordCallback onGetPasswordCallback) {
        final Password password = new Password();
        mRealm.executeTransactionAsync(realm -> {
            Password result = realm.where(Password.class).equalTo("id", id).findFirst();
            if (result != null) {
                password.setId(result.getId());
                password.setGroupName(result.getGroupName());
                password.setUserName(result.getUserName());
                password.setTitle(result.getTitle());
                password.setNote(result.getNote());
                password.setPassword(decrypt(result.getPassword()));
                password.setPayPassword(decrypt(result.getPayPassword()));
                password.setPublish(result.getPublish());
            }
        }, () -> onGetPasswordCallback.onGetPassword(password));
    }

    public void getAllPasswordByGroupName(final String groupName,
                                          final OnGetAllPasswordCallback onGetAllPasswordCallback) {
        final List<Password> passwords = new ArrayList<>();
        mRealm.executeTransactionAsync(realm -> {
            RealmResults<Password> results;
            if (TextUtils.isEmpty(groupName)) {
                results = realm.where(Password.class).findAll();
            } else {
                results = realm.where(Password.class).equalTo(GROUPNAME, groupName).findAll();
            }

            for (Password result : results) {
                Password password = realm.copyFromRealm(result);
                password.setPassword(decrypt(password.getPassword()));
                password.setPayPassword(decrypt(password.getPayPassword()));
                passwords.add(password);
            }
        }, () -> onGetAllPasswordCallback.onGetAllPassword(groupName, passwords));
    }

    public void addPasswordGroup(final PasswordGroup passwordGroup) {
        mRealm.executeTransactionAsync(realm -> realm.insertOrUpdate(passwordGroup), new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callNewPasswordGroup(passwordGroup);
            }
        });
    }

    public void updatePasswordGroup(final String oldGroupName, final String newGroupName) {
        mRealm.executeTransactionAsync(realm -> {
            PasswordGroup newGroup = realm.where(PasswordGroup.class).equalTo(GROUPNAME, newGroupName).findFirst();
            PasswordGroup oldGroup = realm.where(PasswordGroup.class).equalTo(GROUPNAME, oldGroupName).findFirst();

            //对象创建后，主键不能修改，先删除旧的分组
            oldGroup.deleteFromRealm();
            if(newGroup == null) {
                PasswordGroup group = new PasswordGroup();
                group.setGroupName(newGroupName);
                realm.insertOrUpdate(group);
            }

            RealmResults<Password> passwords = realm.where(Password.class).equalTo(GROUPNAME, oldGroupName).findAll();
            for (Password password : passwords) {
                password.setGroupName(newGroupName);
            }
        }, () -> callUpdatePasswordGroup(oldGroupName, newGroupName));
    }

    /**
     * 删除密码分组，包括密码分住下的所有密码都会被删除
     *
     * @param groupName 分组名
     */
    public void deletePasswordGroup(final String groupName) {
        mRealm.executeTransactionAsync(realm -> {
            realm.where(Password.class).equalTo(GROUPNAME, groupName).findAll().deleteAllFromRealm();
            realm.where(PasswordGroup.class).equalTo(GROUPNAME, groupName).findFirst().deleteFromRealm();
        }, () -> callDeletePasswordGroup(groupName));
    }

    public void getAllPasswordGroup(final OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback) {
        final List<PasswordGroup> groups = new ArrayList<>();
        mRealm.executeTransactionAsync(realm -> {
            RealmResults<PasswordGroup> results = realm.where(PasswordGroup.class).findAll();
            for (PasswordGroup result : results) {
                PasswordGroup group = realm.copyFromRealm(result);
                groups.add(group);
            }

            if (groups.size() == 0) {
                PasswordGroup group = new PasswordGroup();
                group.setGroupName("默认");
                realm.insertOrUpdate(group);
                groups.add(group);
            }

        }, () -> onGetAllPasswordGroupCallback.onGetAllPasswordGroup(groups));
    }

    public void close() {
        mRealm.close();
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

    private String encrypt(String password) {
        String result = null;
        try {
            result = AES.encrypt(password, encryptKey);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            if (result == null) {
                result = password;
            }
        }

        return result;
    }

    private String decrypt(String data) {
        String result = null;
        try {
            result = AES.decrypt(data, encryptKey);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }  finally {
            if (result == null) {
                result = data;
            }
        }
        return result;
    }

    private synchronized void callNewPassword(Password password) {
        for(OnPasswordChangeListener listener : getOnPasswordListeners()) {
            listener.onNewPassword(password);
        }
    }

    private synchronized void callUpdatePassword(Password password) {
        for(OnPasswordChangeListener listener : getOnPasswordListeners()) {
            listener.onUpdatePassword(password);
        }
    }

    private synchronized void callDeletePassword(String id) {
        for(OnPasswordChangeListener listener : getOnPasswordListeners()) {
            listener.onDeletePassword(id);
        }
    }

    private synchronized void callNewPasswordGroup(PasswordGroup group) {
        for(OnPasswordGroupChangeListener listener : getOnPasswordGroupListeners()) {
            listener.onNewPasswordGroup(group);
        }
    }

    private synchronized void callUpdatePasswordGroup(String oldGroupName, String newGroupName) {
        for(OnPasswordGroupChangeListener listener : getOnPasswordGroupListeners()) {
            listener.onUpdateGroupName(oldGroupName, newGroupName);
        }
    }

    private synchronized void callDeletePasswordGroup(String groupName) {
        for(OnPasswordGroupChangeListener listener : getOnPasswordGroupListeners()) {
            listener.onDeletePasswordGroup(groupName);
        }
    }

    public void onDestroy() {
        mRealm.close();
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

}
