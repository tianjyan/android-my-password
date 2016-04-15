package young.home.com.mypassword.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import young.home.com.mypassword.application.AES;
import young.home.com.mypassword.application.App;
import young.home.com.mypassword.model.Password;
import young.home.com.mypassword.model.PasswordGroup;
import young.home.com.mypassword.model.SettingKey;

/**
 * Created by YOUNG on 2016/4/3.
 */
public class PasswordDatabase extends SQLiteOpenHelper {

    private String encryptKey;
    private static  final  int version = 1;
    private Context context;

    public PasswordDatabase(Context context) {
        super(context, "password", null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createPasswordTable(db);
        createGroupTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void setEncryptKey(String key){
        this.encryptKey = key;
    }

    public long insertPassword(Password password){
        long id = -1;
        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("publish", password.getPublish());
            contentValues.put("title", password.getTitle());
            contentValues.put("user_name",password.getUserName());
            contentValues.put("password", encrypt(password.getPassword()));
            contentValues.put("note", password.getNote());
            contentValues.put("group_name",password.getGroupName());
            id = sqLiteDatabase.insert("password",null,contentValues);
        } catch (Exception e){
            e.printStackTrace();
        }

        return  id;
    }

    public List<Password> getAllPassword(){
        List<Password> passwords = new ArrayList<Password>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sqLiteDatabase.query("password", null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                Password password = null;
                password = mapPassword(cursor);

                passwords.add(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return passwords;
    }

    public List<Password> getAllPasswordByGroupName(String groupName) {
        List<Password> passwords = new ArrayList<Password>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sqLiteDatabase.query("password", null, "group_name = ?", new String[]{groupName}, null, null,
                    null);

            while (cursor.moveToNext()) {
                Password password = null;
                password = mapPassword(cursor);

                passwords.add(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return passwords;
    }

    public int deletePassword(int id){
        int result = -1;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        result = sqLiteDatabase.delete("password", "id = ?", new String[]{String.valueOf(id)});
        return result;
    }

    public Password getPassword(int id) {
        Password password = null;

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password", null, "id = ?", new String[]{String.valueOf(id)}, null, null,
                    null);

            if (cursor.moveToNext()) {
                password = mapPassword(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return password;
    }

    public int updatePassword(Password password) {
        int result = 0;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            if (password.getPublish() != 0)
                contentValues.put("create_date", password.getPublish());
            if (password.getTitle() != null)
                contentValues.put("title", password.getTitle());
            if (password.getUserName() != null)
                contentValues.put("user_name", password.getUserName());
            if (password.getPassword() != null)
                contentValues.put("password", encrypt(password.getPassword()));
            if (password.getNote() != null)
                contentValues.put("note", password.getNote());

            if (password.getGroupName() != null)
                contentValues.put("group_name", password.getGroupName());

            result = sqLiteDatabase.update("password", contentValues, "id = ?",
                    new String[]{String.valueOf(password.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void addPasswordGroup(PasswordGroup passwordGroup) {
        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", passwordGroup.getGroupName());
            sqLiteDatabase.insert("password_group", null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int deletePasswordGroup(String passwordGroupName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        int count;
        count = sqLiteDatabase.delete("password_group", "name = ?", new String[]{passwordGroupName});
        if (count > 0) {
            sqLiteDatabase.delete("password", "group_name = ?", new String[]{passwordGroupName});
        }
        return count;
    }

    public void updatePasswordGroupName(String oldGroupName, String newGroupName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor rawQuery = null;
        try {
            rawQuery = sqLiteDatabase.rawQuery("select count(name) from password_group where name = ?",
                    new String[]{newGroupName});
            if (rawQuery != null && rawQuery.moveToNext() && rawQuery.getInt(0) == 1) {
                // 新的分组已经存在 直接删除旧的分组
                sqLiteDatabase.delete("password_group", "name = ?", new String[]{oldGroupName});
            } else {
                // 新的分组不存在， 更新旧的分组名称
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", newGroupName);
                sqLiteDatabase.update("password_group", contentValues, "name = ?", new String[]{oldGroupName});
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put("group_name", newGroupName);
            sqLiteDatabase.update("password", contentValues, "group_name = ?", new String[]{oldGroupName});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rawQuery != null)
                rawQuery.close();
        }
    }

    public List<PasswordGroup> getAllPasswordGroup() {
        List<PasswordGroup> passwordGroups = new ArrayList<PasswordGroup>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password_group", null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                PasswordGroup passwordGroup = new PasswordGroup();
                passwordGroup.setGroupName(cursor.getString(cursor.getColumnIndex("name")));
                passwordGroups.add(passwordGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return passwordGroups;
    }

    private void createPasswordTable(SQLiteDatabase db){
        String sql = "create table password(id integer primary key autoincrement, publish integer, title text, "
                + "user_name text, password text, url text, note text, group_name text default '"
                + getDefaultGroupName() + "')";
        db.execSQL(sql);
    }

    private  void createGroupTable(SQLiteDatabase db){
        String sql;
        sql = "create table password_group(name text primary key)";
        db.execSQL(sql);

        sql = "insert into password_group(name) values('" + getDefaultGroupName() + "')";
        db.execSQL(sql);
        getApp().putSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, getDefaultGroupName());
    }

    private String getDefaultGroupName() {
        return "个人";
    }

    private App getApp() {
        return (App) context.getApplicationContext();
    }

    private String encrypt(String password){
        String result = "";
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
        String result = "";
        try {
            result = AES.decrypt(data, encryptKey);
        } catch (Exception e) {
            e.printStackTrace();
            result = data;
        }
        return result;
    }

    private Password mapPassword(Cursor cursor) {
        Password password = new Password();
        password.setId(cursor.getInt(cursor.getColumnIndex("id")));
        password.setPublish(cursor.getLong(cursor.getColumnIndex("publish")));
        password.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        password.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
        password.setPassword(decrypt(cursor.getString(cursor.getColumnIndex("password"))));
        password.setNote(cursor.getString(cursor.getColumnIndex("note")));
        password.setGroupName(cursor.getString(cursor.getColumnIndex("group_name")));
        return password;
    }
}
