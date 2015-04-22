package com.cypressworks.recyclerviewtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.SortedList
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.activity_main.buttonAdd
import kotlinx.android.synthetic.activity_main.buttonAlphabetical
import kotlinx.android.synthetic.activity_main.buttonChronological
import kotlinx.android.synthetic.activity_main.recyclerView
import kotlinx.android.synthetic.item.view.date
import kotlinx.android.synthetic.item.view.name
import java.util.ArrayList
import java.util.Comparator
import java.util.Random
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

class MainActivity : AppCompatActivity() {

    val adapter = MyAdapter()
    val callback = MyCallback(adapter, compareBy { it.name })
    val list = SortedList(javaClass<Data>(), callback)

    val random = Random()

    init {
        adapter.list = list
        callback.list = list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter.inflater = LayoutInflater.from(this)

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setAdapter(adapter)

        buttonAdd.setOnClickListener { add() }
        buttonAlphabetical.setOnClickListener { resort(true) }
        buttonChronological.setOnClickListener { resort(false) }

        for (i in 1..10000) {
            add()
        }
    }

    fun add() {
        var date = System.currentTimeMillis()
        date -= date - (date % (3600 * 1000))
        date = 10

        val name = (0..5)
                .map { "${('A' + random.nextInt(25)).toChar()}" }
                .fold("") { l, r -> l + r }

        list.add(Data(name, date))
    }

    fun resort(alphabetical: Boolean) {
        callback.comparator = if (alphabetical) {
            compareBy { it.name }
        } else {
            compareByDescending<Data> { it.date }
                        .thenByDescending { it.name }
        }
    }
}

data class Data(
        val name: String,
        val date: Long
)

class MyAdapter : RecyclerView.Adapter<MyAdapter.DataHolder>() {

    var list: SortedList<Data> by Delegates.notNull()
    var inflater: LayoutInflater by Delegates.notNull()

    override fun getItemCount() = list.size()

    override fun onBindViewHolder(holder: DataHolder, position: Int) {
        val data = list.get(position)
        holder.view.name.setText(data.name)
        holder.view.date.setText(data.date.toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = DataHolder(inflater.inflate(R.layout.item, parent, false))

    class DataHolder(val view: View) : RecyclerView.ViewHolder(view)

}

class MyCallback(
        adapter: MyAdapter,
        comp: Comparator<Data>
) : SortedListAdapterCallback<Data>(adapter) {

    var list: SortedList<Data> by Delegates.notNull()

    var comparator: Comparator<Data> by withListener(comp) {
        resort()
    }

    private fun resort() = list.batched {
        //        if (list.size() < 100) {
        //            resortFew()
        //        } else {
        resortMany()
        //        }
    }

    private fun resortMany() {
        val copy = (list.size() - 1 downTo 0).map { list.removeItemAt(it) }
        copy.forEach { list.add(it) }
    }

    private fun resortFew() {
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

    override fun areContentsTheSame(oldItem: Data?, newItem: Data?) = oldItem == newItem

    override fun areItemsTheSame(item1: Data?, item2: Data?) = item1 == item2

    override fun compare(o1: Data?, o2: Data?) = comparator.compare(o1, o2)
}

val <T>SortedList<T>.indices: IntRange
    get() = 0..size() - 1

fun <T>SortedList<T>.toArrayList(): ArrayList<T> = indices.map { get(it) }.toArrayList()

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
