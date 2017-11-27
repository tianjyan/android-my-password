package org.tianjyan.pwd.model

import android.os.Bundle

class AsyncResult<Data> {
    var result: Int = 0
    var data: Data? = null
    val bundle = Bundle()
}