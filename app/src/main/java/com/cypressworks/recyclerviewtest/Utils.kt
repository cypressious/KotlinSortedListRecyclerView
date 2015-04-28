package com.cypressworks.recyclerviewtest

import android.support.v7.util.SortedList
import android.util.Log
import java.util.ArrayList
import kotlin.properties.ReadWriteProperty

val <T>SortedList<T>.indices: IntRange
    get() = 0..size() - 1

fun <T>SortedList<T>.batched(f: (SortedList<T>) -> Unit) {
    beginBatchedUpdates()
    f(this)
    endBatchedUpdates()
}

fun <T> withListener(initial: T, callback: () -> Unit): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
    var value: T = initial

    override fun get(thisRef: Any, desc: PropertyMetadata) = value

    override fun set(thisRef: Any, desc: PropertyMetadata, value: T) {
        this.value = value
        callback()
    }
}

fun Any.log(msg: Any?) {
    Log.d(javaClass.getSimpleName(), msg?.toString() ?: "null")
}