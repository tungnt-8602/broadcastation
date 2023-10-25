package com.example.broadcastation.presentation.home.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.databinding.ItemCategoryBinding
import com.example.broadcastation.databinding.ItemRemoteLinearBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.HttpConfig
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("NAME_SHADOWING")
class ItemRemoteCategoryAdapter(
    var callback: ItemRemoteCustomAdapter.Callback,
    private var homeCallback: HomeFragment.Callback
) :
    RecyclerView.Adapter<ItemRemoteCategoryAdapter.ViewHolder>() {

    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var data = listOf<Data>()
    private var onItemTouchListener: ((Remote) -> Unit)? = null
    private var map = mutableMapOf<MutableList<Any>, MutableList<Remote>>()
    private var showTitle: Boolean = true
    val logger = Logger.instance

    /* **********************************************************************
    * Life Cycle
    ********************************************************************** */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            1 -> CategoryViewHolder(
                ItemCategoryBinding.inflate(inflater, parent, false)
            )

            else -> RemoteViewHolder(
                ItemRemoteLinearBinding.inflate(inflater, parent, false)
            )

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].type == Type.Category) {
            1
        } else
            2
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            1 -> {
                val categoryViewHolder = holder as CategoryViewHolder
                categoryViewHolder.bindView(position)
                categoryViewHolder.category?.isVisible = showTitle
            }

            2 -> {
                val emojiViewHolder = holder as RemoteViewHolder
                emojiViewHolder.bindView(position)
            }
        }
    }

    /* **********************************************************************
    * Function
    ********************************************************************** */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableMap<MutableList<Any>, MutableList<Remote>>) {
        this.map = data
        val newData = mutableListOf<Data>()
        for ((key, value) in map) {
            newData.add(Data(Type.Category, key))
            for (remote in value) {
                newData.add(
                    Data(
                        Type.Remote,
                        mutableListOf(
                            remote.id,
                            remote.name,
                            remote.describe,
                            remote.category,
                            remote.type,
                            remote.icon,
                            remote.config
                        )
                    )
                )
            }
        }
        logger.i("ttt $newData")
        this.data = newData
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOnItemTouchListener(listener: ((Remote) -> Unit)?) {
        this.onItemTouchListener = listener
        notifyDataSetChanged()
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindView(index: Int)
    }

    inner class CategoryViewHolder(binding: ItemCategoryBinding) : ViewHolder(binding.root) {
        private val binding: ItemCategoryBinding

        init {
            this.binding = binding
        }

        var category: TextView? = binding.remoteCategory

        override fun bindView(index: Int) {
            val categoryName = data[index].content[0]
            category?.text = categoryName.toString()
        }
    }

    inner class RemoteViewHolder(binding: ItemRemoteLinearBinding) : ViewHolder(binding.root) {
        private val binding: ItemRemoteLinearBinding

        init {
            this.binding = binding
        }

        override fun bindView(index: Int) {
            logger.i("tt")
            val item = data[index].content
            var type: ItemRemoteCustomAdapter.Type = ItemRemoteCustomAdapter.Type.BLUETOOTH
            when (item[4].toString()) {
                "Bluetooth" -> type = ItemRemoteCustomAdapter.Type.BLUETOOTH
                "Http" -> type = ItemRemoteCustomAdapter.Type.HTTP
                "Mqtt" -> type = ItemRemoteCustomAdapter.Type.MQTT
            }
            val remoteItem = Remote(
                item[0].toString().toInt(),
                item[1].toString(),
                item[2].toString(),
                item[3].toString(),
                type,
                item[5].toString().toInt(),
                item[6].toString()
            )
            binding.remoteName.text = remoteItem.name
            binding.remoteIcon.setImageResource(remoteItem.icon)
            binding.remoteContent.text = remoteItem.describe

            binding.broadcast.setOnClickListener {
                when (type) {
                    ItemRemoteCustomAdapter.Type.BLUETOOTH -> {
                        callback.shareBluetooth(remoteItem, homeCallback)
                    }

                    ItemRemoteCustomAdapter.Type.HTTP -> {
                        val type = object : TypeToken<HttpConfig>() {}.type
                        val gson = Gson()
                        val config = gson.fromJson(remoteItem.config, type) as HttpConfig
                        if (config.method == ItemRemoteCustomAdapter.HttpMethod.POST) {
                            callback.postHttp(remoteItem)
                        } else {
                            callback.getHttp(remoteItem)
                        }
                    }

                    ItemRemoteCustomAdapter.Type.MQTT -> {
                        callback.publishMqtt(remoteItem)
                    }
                }
            }

            binding.root.setOnClickListener {
                onItemTouchListener?.let { touch -> touch(remoteItem) }
            }
        }
    }

    enum class Type {
        Category, Remote
    }

    data class Data(val type: Type, val content: MutableList<Any>)

}