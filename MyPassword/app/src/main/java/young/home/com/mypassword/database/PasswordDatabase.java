package young.home.com.mypassword.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import young.home.com.mypassword.application.App;
import young.home.com.mypassword.model.SettingKey;

/**
 * Created by YOUNG on 2016/4/3.
 */
public class PasswordDatabase extends SQLiteOpenHelper {

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
}
