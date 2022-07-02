package com.example.compress.io

import java.util.*

internal abstract class BaseKeyPool<T : PoolAble?> {
    private val keyPool: Queue<T> = createQueue(MAX_SIZE)
    fun get(): T? {
        var result = keyPool.poll()
        if (result == null) {
            result = create()
        }
        return result
    }

    fun offer(key: T) {
        if (keyPool.size < MAX_SIZE) {
            keyPool.offer(key)
        }
    }

    abstract fun create(): T

    companion object {
        private const val MAX_SIZE = 20
        fun <T> createQueue(size: Int): Queue<T> {
            return ArrayDeque(size)
        }
    }
}
