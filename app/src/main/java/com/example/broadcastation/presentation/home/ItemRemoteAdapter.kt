package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.databinding.ItemRemoteBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.HttpConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ItemRemoteAdapter(var callback: Callback, private var homeCallback: HomeFragment.Callback) :
    RecyclerView.Adapter<ItemRemoteAdapter.ViewHolder>() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var data: MutableList<Remote> = mutableListOf()
    private var onItemTouchListener: ((Remote) -> Unit)? = null

    /* **********************************************************************
    * Life Cycle
    ********************************************************************** */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRemoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return try {
            data.size
        } catch (e: Exception) {
            0
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        with(holder.binding) {
            remoteName.text = item.name
            remoteIcon.setImageResource(item.icon)
            remoteContent.text = item.describe
        }
        holder.binding.broadcast.setOnClickListener {
            when (data[position].type) {
                Type.BLUETOOTH -> {
                    callback.shareBluetooth(data[position], homeCallback)
                }

                Type.HTTP -> {
                    val type = object : TypeToken<HttpConfig>() {}.type
                    val gson = Gson()
                    val config = gson.fromJson(data[position].config, type) as HttpConfig
                    if (config.method == HttpMethod.POST) {
                        callback.postHttp(data[position])
                    } else {
                        callback.getHttp(data[position])
                    }
                }

                Type.MQTT -> {
                    callback.publishMqtt(data[position])
                }
            }
        }

        holder.binding.root.setOnClickListener {
            onItemTouchListener?.let { touch -> touch(data[position]) }
        }
    }

    /* **********************************************************************
    * Function
    ********************************************************************** */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<Remote>) {
        this.data = data
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOnItemTouchListener(listener: (Remote) -> Unit) {
        this.onItemTouchListener = listener
        notifyDataSetChanged()
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    enum class Type { BLUETOOTH, HTTP, MQTT }
    enum class HttpMethod { GET, POST }
    class ViewHolder(val binding: ItemRemoteBinding) : RecyclerView.ViewHolder(binding.root)
    interface Callback {
        fun shareBluetooth(remote: Remote, callback: HomeFragment.Callback)
        fun postHttp(remote: Remote)
        fun getHttp(remote: Remote)
        fun publishMqtt(remote: Remote)
    }
}