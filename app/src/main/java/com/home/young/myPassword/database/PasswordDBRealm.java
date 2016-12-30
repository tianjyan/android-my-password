package com.home.young.myPassword.database;

import android.content.Context;
import android.text.TextUtils;

import com.home.young.myPassword.model.PasswordRealm;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

//TODO: 添加版本的代码（RealmMigration must be provided）
public class PasswordDBRealm {

    private Realm realm;
    private String encryptKey;

    public PasswordDBRealm(Context context) {
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }


    private Realm getRealm(Context context) {
        Realm.init(context);
        return Realm.getDefaultInstance();
    }

    public String insertOrUpdatePasswordRealm(PasswordRealm passwordRealm) {
        if (TextUtils.isEmpty(passwordRealm.getId())) {
            passwordRealm.setId(UUID.randomUUID().toString());
        }
        realm.beginTransaction();
        realm.insertOrUpdate(passwordRealm);
        realm.commitTransaction();
        return passwordRealm.getId();
    }

    public void deletePasswordRealm(PasswordRealm passwordRealm) {
        realm.beginTransaction();
        RealmResults<PasswordRealm> realmResults = realm.where(PasswordRealm.class).equalTo("id", passwordRealm.getId()).findAll();
        if (realmResults.size() > 0) {
            realmResults.remove(1);
        }
        realm.commitTransaction();
    }


    public RealmResults<PasswordRealm> getAllPasswordRealm(Context context) {
        Realm realm = getRealm(context);
        final RealmResults<PasswordRealm> passwordRealms = realm.where(PasswordRealm.class).findAll();
        return passwordRealms;
    }

    public void Close() {
        realm.close();
    }
}
