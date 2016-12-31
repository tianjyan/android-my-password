package com.home.young.myPassword.database;

import android.content.Context;
import android.text.TextUtils;

import com.home.young.myPassword.application.AES;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.PasswordGroup;
import com.home.young.myPassword.model.PasswordRealm;
import com.home.young.myPassword.service.MainBinder;
import com.home.young.myPassword.service.OnGetAllPasswordCallback;
import com.home.young.myPassword.service.OnGetPasswordCallback;
import com.home.young.myPassword.service.OnPasswordChangeListener;
import com.home.young.myPassword.service.OnPasswordGroupChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

//TODO: 添加版本的代码（RealmMigration must be provided）
public class PasswordDBRealm {

    private Realm realm;
    private String encryptKey;
    private MainBinder mainBinder;

    public PasswordDBRealm(Context context, String encryptKey, MainBinder mainBinder) {
        Realm.init(context);
        Realm.setDefaultConfiguration(getConfiguration());
        this.realm = Realm.getDefaultInstance();
        this.encryptKey = encryptKey;
        this.mainBinder = mainBinder;
    }

    private RealmConfiguration getConfiguration() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("mypasswordrealm.realm").deleteRealmIfMigrationNeeded().build();
        return realmConfiguration;
    }

    private Realm getRealm(Context context) {
        Realm.init(context);
        return Realm.getDefaultInstance();
    }

    public void addPassword(final Password password) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                password.setId(UUID.randomUUID().toString());
                password.setPassword(encrypt(password.getPassword()));
                password.setPayPassword(encrypt(password.getPayPassword()));
                //当导入密码时，需要插入分组
                PasswordGroup group = new PasswordGroup();
                group.setGroupName(password.getGroupName());
                realm.insertOrUpdate(group);

                realm.insertOrUpdate(password);
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                callNewPassword(password);
            }
        });
    }

    public void updatePassword(final Password password) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                password.setPassword(encrypt(password.getPassword()));
                password.setPayPassword(encrypt(password.getPayPassword()));
                realm.insertOrUpdate(password);
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                callUpdatePassword(password);
            }
        });
    }

    public void deletePassword(final Password password) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Password> realmResults = realm.where(Password.class).equalTo("id", password.getId()).findAll();
                if (realmResults.size() > 0) {
                    realmResults.deleteAllFromRealm();
                }
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                callDeletePassword(password);
            }
        });
    }

    public void getPassword(final String id, final OnGetPasswordCallback onGetPasswordCallback) {
        final Password password = new Password();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
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
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                onGetPasswordCallback.onGetPassword(password);
            }
        });
    }

    public void getAllPasswordByGroupName(final String groupName, final OnGetAllPasswordCallback onGetAllPasswordCallback) {
        final List<Password> passwords = new ArrayList<>();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Password> results = realm.where(Password.class).equalTo("groupname", groupName).findAll();
                Iterator<Password> iterator = results.iterator();
                while (iterator.hasNext()) {
                    Password item = iterator.next();
                    Password password = new Password();
                    password.setId(item.getId());
                    password.setGroupName(item.getGroupName());
                    password.setUserName(item.getUserName());
                    password.setTitle(item.getTitle());
                    password.setNote(item.getNote());
                    password.setPassword(decrypt(item.getPassword()));
                    password.setPayPassword(decrypt(item.getPayPassword()));
                    password.setPublish(item.getPublish());
                    passwords.add(item);
                }
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                onGetAllPasswordCallback.onGetAllPassword(groupName, passwords);
            }
        });
    }

    public void addPasswordGroup(final PasswordGroup passwordGroup) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(passwordGroup);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callNewPasswordGroup(passwordGroup);
            }
        });
    }

    public void updatePasswordGroup(final String oldGroupName, final String newGroupName) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PasswordGroup newGroup = realm.where(PasswordGroup.class).equalTo("groupname", newGroupName).findFirst();
                PasswordGroup oldGroup = realm.where(PasswordGroup.class).equalTo("groupname", oldGroupName).findFirst();
                if(newGroup == null) {
                    oldGroup.setGroupName(newGroupName);
                } else {
                    // 新的分组已经存在 直接删除旧的分组
                    oldGroup.deleteFromRealm();
                }

                PasswordGroup group = realm.where(PasswordGroup.class).equalTo("groupname", oldGroupName).findFirst();
                group.setGroupName(newGroupName);

                RealmResults<Password> passwords = realm.where(Password.class).equalTo("groupname", oldGroupName).findAll();
                Iterator<Password> iterator = passwords.iterator();
                while (iterator.hasNext()) {
                    Password password = iterator.next();
                    password.setGroupName(newGroupName);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callUpdatePasswordGroup(oldGroupName, newGroupName);
            }
        });
    }

    public void deletePasswordGroup(final String groupName) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PasswordGroup.class).equalTo("groupname", groupName).findFirst().deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callDeletePasswordGroup(groupName);
            }
        });
    }

    public void Close() {
        realm.close();
    }

    private String encrypt(String password){
        String result;
        try{
            result = AES.encrypt(password, encryptKey);
        }
        catch (Exception e){
            e.printStackTrace();
            result = password;
        }
        return result;
    }

    private String decrypt(String data) {
        String result;
        try {
            result = AES.decrypt(data, encryptKey);
        } catch (Exception e) {
            e.printStackTrace();
            result = data;
        }
        return result;
    }

    private synchronized void callNewPassword(Password password) {
        for(OnPasswordChangeListener listener : mainBinder.getOnPasswordListeners()) {
            listener.onNewPassword(password);
        }
    }

    private synchronized void callUpdatePassword(Password password) {
        for(OnPasswordChangeListener listener : mainBinder.getOnPasswordListeners()) {
            listener.onUpdatePassword(password);
        }
    }

    private synchronized void callDeletePassword(Password password) {
        for(OnPasswordChangeListener listener : mainBinder.getOnPasswordListeners()) {
            listener.onDeletePassword(password);
        }
    }

    private synchronized void callNewPasswordGroup(PasswordGroup group) {
        for(OnPasswordGroupChangeListener listener : mainBinder.getOnPasswordGroupListeners()) {
            listener.onNewPasswordGroup(group);
        }
    }

    private synchronized void callUpdatePasswordGroup(String oldGroupName, String newGroupName) {
        for(OnPasswordGroupChangeListener listener : mainBinder.getOnPasswordGroupListeners()) {
            listener.onUpdateGroupName(oldGroupName, newGroupName);
        }
    }

    private synchronized void callDeletePasswordGroup(String groupName) {
        for(OnPasswordGroupChangeListener listener : mainBinder.getOnPasswordGroupListeners()) {
            listener.onDeletePasswordGroup(groupName);
        }
    }
}
