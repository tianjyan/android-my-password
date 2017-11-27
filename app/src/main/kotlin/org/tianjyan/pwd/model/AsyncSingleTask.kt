package org.tianjyan.pwd.model

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

abstract class AsyncSingleTask<D> {

    private var mAsyncResult: AsyncResult<D>? = null
    private var mIsRunned = false
    private var mDelay = 0
    private val mainThreadRunable = Runnable { runOnUIThread(mAsyncResult) }
    private val backgroundRunable = Runnable {
        mAsyncResult = doInBackground(AsyncResult())
        handler.postDelayed(mainThreadRunable, mDelay.toLong())
    }

    fun setDelay(delay: Int) {
        this.mDelay = delay
    }

    @Synchronized
    fun execute() {
        if (mIsRunned)
            throw RuntimeException("该任务已经运行过，不能再次调用")

        mIsRunned = true
        executorService.execute(backgroundRunable)
    }

    protected abstract fun doInBackground(asyncResult: AsyncResult<D>): AsyncResult<D>

    protected abstract fun runOnUIThread(asyncResult: AsyncResult<D>?)

    companion object {
        private val executorService = Executors.newSingleThreadExecutor()
        private val handler = Handler(Looper.getMainLooper())
    }
}