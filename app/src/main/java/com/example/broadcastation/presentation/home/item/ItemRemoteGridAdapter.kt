package com.example.broadcastation.presentation.home.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.databinding.ItemRemoteGridBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.HttpConfig
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ItemRemoteGridAdapter(var callback: ItemRemoteCustomAdapter.Callback, private var homeCallback: HomeFragment.Callback) :
    RecyclerView.Adapter<ItemRemoteGridAdapter.ViewHolder>() {

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
            ItemRemoteGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            remoteType.setImageResource(item.icon)
            remoteDescribe.text = item.describe
        }
        holder.binding.remoteBroadcast.setOnClickListener {
            when (data[position].type) {
                ItemRemoteCustomAdapter.Type.BLUETOOTH -> {
                    callback.shareBluetooth(data[position], homeCallback)
                }

                ItemRemoteCustomAdapter.Type.HTTP -> {
                    val type = object : TypeToken<HttpConfig>() {}.type
                    val gson = Gson()
                    val config = gson.fromJson(data[position].config, type) as HttpConfig
                    if (config.method == ItemRemoteCustomAdapter.HttpMethod.POST) {
                        callback.postHttp(data[position])
                    } else {
                        callback.getHttp(data[position])
                    }
                }

                ItemRemoteCustomAdapter.Type.MQTT -> {
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
    class ViewHolder(val binding: ItemRemoteGridBinding) : RecyclerView.ViewHolder(binding.root)
}