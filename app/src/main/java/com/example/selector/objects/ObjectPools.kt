package com.example.selector.objects

import java.util.*

class ObjectPools {
    interface Pool<T> {
        /**
         * 获取对象
         */
        fun acquire(): T

        /**
         * 释放对象
         */
        fun release(obj: T): Boolean

        /**
         * 销毁对象池
         */
        fun destroy()
    }

    open class SimpleObjectPool<T> : Pool<T> {
        private val mPool: LinkedList<T> = LinkedList()
        override fun acquire(): T {
            return mPool.poll()
        }

        override fun release(obj: T): Boolean {
            return if (isInPool(obj)) {
                false
            } else mPool.add(obj)
        }

        override fun destroy() {
            mPool.clear()
        }

        private fun isInPool(obj: T): Boolean {
            return mPool.contains(obj)
        }

    }

    class SynchronizedPool<T> : SimpleObjectPool<T>() {
        private val mLock = Any()
        override fun acquire(): T {
            synchronized(mLock) { return super.acquire() }
        }

        override fun release(obj: T): Boolean {
            synchronized(mLock) { return super.release(obj) }
        }

        override fun destroy() {
            synchronized(mLock) { super.destroy() }
        }
    }
}
