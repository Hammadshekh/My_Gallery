package com.example.selector.utils

object ValueOf {
    fun toString(o: Any): String {
        var value = ""
        try {
            value = o.toString()
        } catch (e: Exception) {
        }
        return value
    }

    fun toDouble(o: Any?, defaultValue: Int = 0): Double {
        if (o == null) {
            return defaultValue.toDouble()
        }
        val value: Double = try {
            o.toString().trim { it <= ' ' }.toDouble()
        } catch (e: Exception) {
            defaultValue.toDouble()
        }
        return value
    }

    fun toLong(o: Any?, defaultValue: Long = 0): Long {
        if (o == null) {
            return defaultValue
        }
        var value: Long = 0
        value = try {
            val s = o.toString().trim { it <= ' ' }
            if (s.contains(".")) {
                s.substring(0, s.lastIndexOf(".")).toLong()
            } else {
                s.toLong()
            }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    fun toFloat(o: Any?, defaultValue: Long = 0): Float {
        if (o == null) {
            return defaultValue.toFloat()
        }
        var value = 0f
        value = try {
            val s = o.toString().trim { it <= ' ' }
            s.toFloat()
        } catch (e: Exception) {
            defaultValue.toFloat()
        }
        return value
    }

    fun toInt(o: Any?, defaultValue: Int = 0): Int {
        if (o == null) {
            return defaultValue
        }
        val value: Int = try {
            val s = o.toString().trim { it <= ' ' }
            if (s.contains(".")) {
                s.substring(0, s.lastIndexOf(".")).toInt()
            } else {
                s.toInt()
            }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    fun toBoolean(o: Any?, defaultValue: Boolean = false): Boolean {
        if (o == null) {
            return false
        }
        val value: Boolean = try {
            val s = o.toString().trim { it <= ' ' }
            "false" != s.trim { it <= ' ' }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    fun <T> to(o: T, defaultValue: T): T {
        if (o == null) {
            return defaultValue
        }
        return o
    }
}
