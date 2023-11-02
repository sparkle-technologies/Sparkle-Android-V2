package com.hyphenate.easeui.ui.dialog

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

object ThreadUtil {
    private const val TAG = "ThreadUtil"

    val handle by lazy { Handler(Looper.getMainLooper()) }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread == Thread.currentThread()
    }

    fun assertInMainThread(inOrNot: Boolean = true) {
        val condition = if (inOrNot) {
            !isMainThread()
        } else {
            isMainThread()
        }
        if (condition) {
            val msg = "cannot be invoked in thread ${Thread.currentThread().id}"
            Log.e(TAG, msg)
        }
    }

    fun runOnMainThread(runnable: Runnable) {
        handle.post(runnable)
    }

    fun post(r: Runnable) = postDelayed(0, r)

    fun postDelayed(delayed: Long, r: Runnable) = handle.postDelayed(r, delayed)

    fun newFixedThreadPool(name: String, threads: Int): ExecutorService {
        return Executors.newFixedThreadPool(
            threads,
            XThreadFactory.getDefaultThreadFactory(name)
        )
    }

    private class XThreadFactory private constructor(name: String) : ThreadFactory {
        private val mThreadGroup: ThreadGroup
        private val mThreadNumber: AtomicInteger = AtomicInteger(1)
        private val mNamePrefix: String

        init {
            val s = System.getSecurityManager()
            mThreadGroup = if (s != null) s.threadGroup else Thread.currentThread().threadGroup!!
            mNamePrefix = "$name#"
        }

        override fun newThread(r: Runnable?): Thread {
            val t = Thread(mThreadGroup, r, mNamePrefix + mThreadNumber.getAndIncrement(), 0)

            // no daemon
            if (t.isDaemon) t.isDaemon = false

            // normal priority
            if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
            return t
        }

        companion object {
            /**
             * 获取线程工程供创建线程池使用
             *
             * 根据编程规范，首先不应使用[java.util.concurrent.Executors]创建线程池
             * 其次[Executors.defaultThreadFactory]使用数字命名线程，难于定位问题，强制使用本方法
             */
            fun getDefaultThreadFactory(name: String): ThreadFactory {
                return XThreadFactory(name)
            }
        }
    }
}

