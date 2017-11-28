package org.tianjyan.pwd.database

import android.content.Context
import android.os.Binder
import android.text.TextUtils
import android.util.Log
import com.home.young.myPassword.application.AES
import com.home.young.myPassword.model.AsyncResult
import com.home.young.myPassword.model.AsyncSingleTask
import com.home.young.myPassword.model.Password
import com.home.young.myPassword.model.PasswordGroup
import com.home.young.myPassword.service.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.util.*

//TODO: 添加版本的代码（RealmMigration must be provided）
class PasswordDBRealm(context: Context, private val encryptKey: String) : Binder() {

    private val mRealm: Realm

    private val onPasswordListeners = ArrayList<OnPasswordChangeListener>()
    private val onPasswordGroupListeners = ArrayList<OnPasswordGroupChangeListener>()

    private val configuration: RealmConfiguration
        get() = RealmConfiguration.Builder()
                .name("mypasswordrealm.realm").deleteRealmIfMigrationNeeded().build()

    fun getOnPasswordListeners(): List<OnPasswordChangeListener> {
        return onPasswordListeners
    }

    fun getOnPasswordGroupListeners(): List<OnPasswordGroupChangeListener> {
        return onPasswordGroupListeners
    }

    init {
        Realm.init(context)
        Realm.setDefaultConfiguration(configuration)
        this.mRealm = Realm.getDefaultInstance()
    }

    fun registOnPasswordGroupListener(onPasswordGroupListener: OnPasswordGroupChangeListener) {
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                onPasswordGroupListeners.add(onPasswordGroupListener)
            }
        }.execute()
    }

    fun unregistOnPasswordGroupListener(onPasswordGroupListener: OnPasswordGroupChangeListener) {
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                onPasswordGroupListeners.remove(onPasswordGroupListener)
            }
        }.execute()
    }

    fun registOnPasswordListener(onPasswordListener: OnPasswordChangeListener) {
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                onPasswordListeners.add(onPasswordListener)
            }
        }.execute()
    }

    fun unregistOnPasswordListener(onPasswordListener: OnPasswordChangeListener) {
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                onPasswordListeners.remove(onPasswordListener)
            }
        }.execute()
    }

    fun addPassword(password: Password) {
        mRealm.executeTransactionAsync({ realm ->
            password.id = UUID.randomUUID().toString()
            password.password = encrypt(password.password)
            password.payPassword = encrypt(password.payPassword)
            //当导入密码时，需要插入分组
            val group = PasswordGroup()
            group.groupName = password.groupName
            realm.insertOrUpdate(group)

            realm.insertOrUpdate(password)
        }) { ->
            password.password = decrypt(password.password)
            password.payPassword = decrypt(password.payPassword)
            callNewPassword(password)
        }
    }

    fun updatePassword(password: Password) {
        mRealm.executeTransactionAsync({ realm ->
            password.password = encrypt(password.password)
            password.payPassword = encrypt(password.payPassword)
            realm.insertOrUpdate(password)
        }) { ->
            password.password = decrypt(password.password)
            password.payPassword = decrypt(password.payPassword)
            callUpdatePassword(password)
        }
    }

    fun deletePassword(id: String) {
        mRealm.executeTransactionAsync({ realm ->
            val realmResults = realm.where(Password::class.java).equalTo("id", id).findAll()
            if (realmResults.size > 0) {
                realmResults.deleteAllFromRealm()
            }
        }) { -> callDeletePassword(id) }
    }

    fun getPassword(id: String, onGetPasswordCallback: OnGetPasswordCallback) {
        val password = Password()
        mRealm.executeTransactionAsync({ realm ->
            val result = realm.where(Password::class.java).equalTo("id", id).findFirst()
            if (result != null) {
                password.id = result!!.getId()
                password.groupName = result!!.getGroupName()
                password.userName = result!!.getUserName()
                password.title = result!!.getTitle()
                password.note = result!!.getNote()
                password.password = decrypt(result!!.getPassword())
                password.payPassword = decrypt(result!!.getPayPassword())
                password.publish = result!!.getPublish()
            }
        }) { -> onGetPasswordCallback.onGetPassword(password) }
    }

    fun getAllPasswordByGroupName(groupName: String,
                                  onGetAllPasswordCallback: OnGetAllPasswordCallback) {
        val passwords = ArrayList<Password>()
        mRealm.executeTransactionAsync({ realm ->
            val results: RealmResults<Password>
            if (TextUtils.isEmpty(groupName)) {
                results = realm.where(Password::class.java).findAll()
            } else {
                results = realm.where(Password::class.java).equalTo(GROUPNAME, groupName).findAll()
            }

            for (result in results) {
                val password = realm.copyFromRealm(result)
                password.setPassword(decrypt(password.getPassword()))
                password.setPayPassword(decrypt(password.getPayPassword()))
                passwords.add(password)
            }
        }) { -> onGetAllPasswordCallback.onGetAllPassword(groupName, passwords) }
    }

    fun addPasswordGroup(passwordGroup: PasswordGroup) {
        mRealm.executeTransactionAsync({ realm -> realm.insertOrUpdate(passwordGroup) },  { -> callNewPasswordGroup(passwordGroup) })
    }

    fun updatePasswordGroup(oldGroupName: String, newGroupName: String) {
        mRealm.executeTransactionAsync({ realm ->
            val newGroup = realm.where(PasswordGroup::class.java).equalTo(GROUPNAME, newGroupName).findFirst()
            val oldGroup = realm.where(PasswordGroup::class.java).equalTo(GROUPNAME, oldGroupName).findFirst()

            //对象创建后，主键不能修改，先删除旧的分组
            oldGroup!!.deleteFromRealm()
            if (newGroup == null) {
                val group = PasswordGroup()
                group.groupName = newGroupName
                realm.insertOrUpdate(group)
            }

            val passwords = realm.where(Password::class.java).equalTo(GROUPNAME, oldGroupName).findAll()
            for (password in passwords) {
                password.setGroupName(newGroupName)
            }
        }) { ->callUpdatePasswordGroup(oldGroupName, newGroupName) }
    }

    /**
     * 删除密码分组，包括密码分住下的所有密码都会被删除
     *
     * @param groupName 分组名
     */
    fun deletePasswordGroup(groupName: String) {
        mRealm.executeTransactionAsync({ realm ->
            realm.where(Password::class.java).equalTo(GROUPNAME, groupName).findAll().deleteAllFromRealm()
            realm.where(PasswordGroup::class.java).equalTo(GROUPNAME, groupName).findFirst()!!.deleteFromRealm()
        }) { -> callDeletePasswordGroup(groupName) }
    }

    fun getAllPasswordGroup(onGetAllPasswordGroupCallback: OnGetAllPasswordGroupCallback) {
        val groups = ArrayList<PasswordGroup>()
        mRealm.executeTransactionAsync({ realm ->
            val results = realm.where(PasswordGroup::class.java).findAll()
            for (result in results) {
                val group = realm.copyFromRealm(result)
                groups.add(group)
            }

            if (groups.size == 0) {
                val group = PasswordGroup()
                group.groupName = "默认"
                realm.insertOrUpdate(group)
                groups.add(group)
            }

        }) { -> onGetAllPasswordGroupCallback.onGetAllPasswordGroup(groups) }
    }

    fun close() {
        mRealm.close()
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                super.runOnUIThread(asyncResult)
                onPasswordListeners.clear()
                onPasswordGroupListeners.clear()
            }
        }.execute()
    }

    private fun encrypt(password: String): String {
        var result: String? = null
        try {
            result = AES.encrypt(password, encryptKey)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        } finally {
            if (result == null) {
                result = password
            }
        }

        return result
    }

    private fun decrypt(data: String): String {
        var result: String? = null
        try {
            result = AES.decrypt(data, encryptKey)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        } finally {
            if (result == null) {
                result = data
            }
        }
        return result
    }

    @Synchronized private fun callNewPassword(password: Password) {
        for (listener in getOnPasswordListeners()) {
            listener.onNewPassword(password)
        }
    }

    @Synchronized private fun callUpdatePassword(password: Password) {
        for (listener in getOnPasswordListeners()) {
            listener.onUpdatePassword(password)
        }
    }

    @Synchronized private fun callDeletePassword(id: String) {
        for (listener in getOnPasswordListeners()) {
            listener.onDeletePassword(id)
        }
    }

    @Synchronized private fun callNewPasswordGroup(group: PasswordGroup) {
        for (listener in getOnPasswordGroupListeners()) {
            listener.onNewPasswordGroup(group)
        }
    }

    @Synchronized private fun callUpdatePasswordGroup(oldGroupName: String, newGroupName: String) {
        for (listener in getOnPasswordGroupListeners()) {
            listener.onUpdateGroupName(oldGroupName, newGroupName)
        }
    }

    @Synchronized private fun callDeletePasswordGroup(groupName: String) {
        for (listener in getOnPasswordGroupListeners()) {
            listener.onDeletePasswordGroup(groupName)
        }
    }

    fun onDestroy() {
        mRealm.close()
        object : AsyncSingleTask<Void>() {
            override fun doInBackground(asyncResult: AsyncResult<Void>): AsyncResult<Void> {
                return asyncResult
            }

            override fun runOnUIThread(asyncResult: AsyncResult<Void>) {
                super.runOnUIThread(asyncResult)
                onPasswordListeners.clear()
                onPasswordGroupListeners.clear()
            }
        }.execute()
    }

    companion object {

        private val GROUPNAME = "groupName"
        private val TAG = "PasswordDBRealm"
    }

}