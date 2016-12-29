package com.home.young.myPassword.database;

import android.content.Context;

import com.home.young.myPassword.model.PasswordRealm;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

//TODO: 添加版本的代码（RealmMigration must be provided）
public class PasswordDBRealm {

    private Realm getRealm(Context context) {
        Realm.init(context);
        return Realm.getDefaultInstance();
    }

    public String insertPasswordRealm(Context context, PasswordRealm passwordRealm) {
        passwordRealm.setId(UUID.randomUUID().toString());
        Realm realm = getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(passwordRealm);
        realm.commitTransaction();
        return passwordRealm.getId();
    }

    public RealmResults<PasswordRealm> getAllPasswordRealm(Context context) {
        Realm realm = getRealm(context);
        final RealmResults<PasswordRealm> passwordRealms = realm.where(PasswordRealm.class).findAll();
        return passwordRealms;
    }
}
