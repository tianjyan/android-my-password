package com.home.young.myPassword;

import android.app.Application;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.home.young.myPassword.application.IOHelper;
import com.home.young.myPassword.application.JsonHelper;
import com.home.young.myPassword.application.PwdGen;
import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.Password;
import com.home.young.myPassword.model.PasswordRealm;

import io.realm.RealmResults;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @SmallTest
    public void testPwdGen(){
        String password = PwdGen.generatePassword(16, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.PROHIBITED);
        assertNotNull(password);
    }

    @SmallTest
    public void testToJsonObject() throws JSONException{
        Password password = new Password();
        password.setGroupName("testGroup");
        password.setId(1);
        password.setNote("test Note");
        password.setPassword("testPassword");
        password.setPublish(123);
        password.setTitle("test Title");
        password.setUserName("test UserName");

        String jsonStr = JsonHelper.toJSON(password);
        assertNotNull(jsonStr);

        Password newPassword = JsonHelper.parseObject(jsonStr,Password.class);
        assertNotNull(newPassword);
    }

    @SmallTest
    public void testToJsonArray() throws JSONException{
        ArrayList<Password> passwords = new ArrayList<>();

        Password password1 = new Password();
        password1.setGroupName("testGroup");
        password1.setId(1);
        password1.setNote("test Note");
        password1.setPassword("testPassword");
        password1.setPublish(123);
        password1.setTitle("test Title");
        password1.setUserName("test UserName");

        Password password2 = new Password();
        password2.setGroupName("testGroup");
        password2.setId(1);
        password2.setNote("test Note");
        password2.setPassword("testPassword");
        password2.setPublish(123);
        password2.setTitle("test Title");
        password2.setUserName("test UserName");

        passwords.add(password1);
        passwords.add(password2);

        String jsonStr = JsonHelper.toJSON(passwords);
        assertNotNull(jsonStr);

        Password[] newPasswords = JsonHelper.parseArray(jsonStr,Password.class);
        assertNotNull(newPasswords);
    }

    @SmallTest
    public void testReadFile() throws IOException {
        IOHelper.writeSDFile(Environment.getExternalStorageDirectory().getPath()+ "/Download/MyPasswordBackup.json","123");
        String str = IOHelper.readSDFile(Environment.getExternalStorageDirectory().getPath()+ "/Download/MyPasswordBackup.json");
        Assert.assertNotNull(str);
        IOHelper.deleteSDFile(Environment.getExternalStorageDirectory().getPath()+ "/Download/MyPasswordBackup.json");
    }

    @SmallTest
    public void testInsertPasswordRealm() {
        PasswordDBRealm passwordDBRealm = new PasswordDBRealm(getContext());
        PasswordRealm passwordRealm = new PasswordRealm();
        passwordRealm.setTitle("Test Title");
        passwordRealm.setUserName("Test UserName");
        String id = passwordDBRealm.insertOrUpdatePasswordRealm(passwordRealm);

        RealmResults<PasswordRealm> passwordRealms = passwordDBRealm.getAllPasswordRealm(getContext());

        Iterator<PasswordRealm> iterator = passwordRealms.iterator();

        boolean result = false;

        while (iterator.hasNext()) {
            PasswordRealm item = iterator.next();
            if (item.getId().equals(id)) {
                result = true;
                break;
            }
        }

        Assert.assertEquals(result, true);
    }
}