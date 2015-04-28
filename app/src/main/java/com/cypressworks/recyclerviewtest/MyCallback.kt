package com.cypressworks.recyclerviewtest

import android.support.v7.util.SortedList
import android.support.v7.util.getUnderlyingArray
import android.support.v7.widget.util.SortedListAdapterCallback
import java.util.Arrays
import java.util.Comparator
import kotlin.properties.Delegates

class MyCallback(
        private val adapter: MyAdapter,
        comp: Comparator<Data>,
        private val resortDelegate: MyCallback.() -> Unit = { resortUnderlying() }
) : SortedListAdapterCallback<Data>(adapter) {

    var list: SortedList<Data> by Delegates.notNull()

    var comparator: Comparator<Data> by withListener(comp) {

        resortDelegate()

    }

    fun resortByCopy() {
        list.batched {
            val copy = (list.size() - 1 downTo 0).map { list.removeItemAt(it) }
            copy.forEach { list.add(it) }
        }
    }

    /**
     * Results in endless loop if items are not unique!
     */
    fun resortUnique() {
        list.batched {
            val indices = list.indices.toArrayList()

            while (!indices.isEmpty()) {
                val i = indices.first()
                val item = list.get(i)

                list.recalculatePositionOfItemAt(i)

                val newIndex = list.indexOf(item)

                [suppress("USELESS_CAST_STATIC_ASSERT_IS_FINE")]
                indices.remove(newIndex as Any) //cast to disambiguate remove()
            }
        }
    }

    fun resortUnderlying() {
        Arrays.sort(list.getUnderlyingArray(), comparator)

        adapter.notifyItemRangeRemoved(0, list.size())
        adapter.notifyItemRangeInserted(0, list.size())
    }

    override fun areContentsTheSame(oldItem: Data?, newItem: Data?) = oldItem == newItem

    override fun areItemsTheSame(item1: Data?, item2: Data?) = item1 == item2

    override fun compare(o1: Data?, o2: Data?) = comparator.compare(o1, o2)
}