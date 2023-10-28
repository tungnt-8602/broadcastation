package com.example.broadcastation.presentation.home.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.databinding.ItemRemoteLinearBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.HttpConfig
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections

@SuppressLint("NotifyDataSetChanged")
class ItemRemoteCustomAdapter(var callback: Callback, private var homeCallback: HomeFragment.Callback) :
    RecyclerView.Adapter<ItemRemoteCustomAdapter.ViewHolder>(),
    ItemMoveCustomCallback.ItemTouchHelperContract {
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
            ItemRemoteLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(data, i, i + 1)
                reOrderRemote(i, i+1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(data, i, i - 1)
                reOrderRemote(i, i-1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
//        myViewHolder?.itemView?.setBackgroundResource(R.color.scc_100)
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
//        myViewHolder?.itemView?.setBackgroundColor(Color.WHITE)
        notifyDataSetChanged()
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

    private fun reOrderRemote(fromPosition: Int, toPosition: Int){
        val remoteList = homeCallback.getAllRemote()
        Collections.swap(remoteList, fromPosition, toPosition)
        homeCallback.updateRemote(remoteList)
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    enum class Type { BLUETOOTH, HTTP, MQTT }
    enum class HttpMethod { GET, POST }
    class ViewHolder(val binding: ItemRemoteLinearBinding) : RecyclerView.ViewHolder(binding.root)
    interface Callback {
        fun shareBluetooth(remote: Remote, callback: HomeFragment.Callback)
        fun postHttp(remote: Remote)
        fun getHttp(remote: Remote)
        fun publishMqtt(remote: Remote)
    }
}