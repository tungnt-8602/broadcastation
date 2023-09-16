package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.databinding.ItemRemoteBinding
import com.example.broadcastation.entity.Remote

class ItemRemoteAdapter : RecyclerView.Adapter<ItemRemoteAdapter.ViewHolder>(){
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var data: MutableList<Remote> = mutableListOf()

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
        }
    }

    /* **********************************************************************
    * Function
    ********************************************************************** */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data : MutableList<Remote>) {
        this.data.clear()
        this.data = data
        notifyDataSetChanged()
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    class ViewHolder(val binding: ItemRemoteBinding) : RecyclerView.ViewHolder(binding.root)
}