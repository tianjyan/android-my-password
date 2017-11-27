package org.tianjyan.pwd.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PasswordGroup : RealmObject() {
    @PrimaryKey
    var groupName: String? = null

    override fun toString(): String {
        return "PasswordGroup [groupName=$groupName]"
    }
}