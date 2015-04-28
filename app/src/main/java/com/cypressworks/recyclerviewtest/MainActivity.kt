package com.cypressworks.recyclerviewtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.SortedList
import android.support.v7.util.getUnderlyingArray
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
import java.util.Arrays
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

        for (i in 1..100) {
            add()
        }
    }

    fun add() {
        var date = random.nextInt(3600).toLong()

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


