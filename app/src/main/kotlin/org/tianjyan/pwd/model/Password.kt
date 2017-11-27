package org.tianjyan.pwd.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Password : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var publish: Long = 0
    var title: String? = null
    var userName: String? = null
    var password: String? = null
    var payPassword: String? = null
    var note: String? = null
    var groupName: String? = null
}