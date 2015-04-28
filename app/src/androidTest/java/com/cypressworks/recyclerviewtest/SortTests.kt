package com.cypressworks.recyclerviewtest

import android.support.v7.util.SortedList
import junit.framework.TestCase
import java.util.Random
import kotlin.properties.Delegates

abstract class AbstractSortTest : TestCase() {
    val random = Random()
    var callback: MyCallback by Delegates.notNull()


    override fun setUp() {
        val adapter = MyAdapter()
        callback = createCallback(adapter)
        val list = SortedList(javaClass<Data>(), callback)

        adapter.list = list
        callback.list = list

        for (i in 1..10000) {
            var date = System.currentTimeMillis()
            date -= date - (date % (3600 * 1000))
            date = 10

            val name = (0..5)
                    .map { "${('A' + random.nextInt(25)).toChar()}" }
                    .fold("") { l, r -> l + r }

            list.add(Data(name, date))
        }
    }

    private fun createCallback(adapter: MyAdapter)
            = MyCallback(adapter, compareBy { it.name }, createSortDelegate())

    abstract fun createSortDelegate(): MyCallback.() -> Unit

    fun testCompareDate() {
        callback.comparator = compareByDescending<Data> { it.date }
    }


    fun testCompareDateName() {
        callback.comparator = compareByDescending<Data> { it.date }
                .thenBy { it.name }
    }

    fun testCompareDateIdentity() {
        callback.comparator = compareByDescending<Data> { it.date }
                .thenBy { System.identityHashCode(it) }
    }
}

class SortByCopyTest : AbstractSortTest() {

    override fun createSortDelegate(): MyCallback.() -> Unit = { resortByCopy() }
}


class SortUnderlyingTest : AbstractSortTest() {

    override fun createSortDelegate(): MyCallback.() -> Unit = { resortUnderlying() }
}