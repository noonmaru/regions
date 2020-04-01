package com.github.noonmaru.regions.util

import java.lang.ref.SoftReference
import kotlin.reflect.KProperty


fun <T> softCache(loader: () -> T): CacheDelegate<T> {
    return SoftCacheDelegate(loader)
}

interface CacheDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }

    fun get(): T

    fun clear()
}

class SoftCacheDelegate<T>(private val loader: () -> T) : CacheDelegate<T> {

    private var ref: SoftReference<T>? = null

    override fun get(): T {
        var value = ref?.get()

        if (value == null) {
            value = loader()
            ref = SoftReference(value)
        }

        return value!!
    }

    override fun clear() {
        ref = null
    }
}

