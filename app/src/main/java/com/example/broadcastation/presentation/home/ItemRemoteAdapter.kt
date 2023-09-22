package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.databinding.ItemRemoteBinding
import com.example.broadcastation.entity.Remote
import com.google.android.material.snackbar.Snackbar

class ItemRemoteAdapter(private var data: MutableList<Remote>, private var viewModel: HomeViewModel, private var load: View) : RecyclerView.Adapter<ItemRemoteAdapter.ViewHolder>(){
    /* **********************************************************************
     * Variable
     ********************************************************************** */

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
        when (data[position].action) {
            1 -> {
                holder.binding.broadcast.setOnClickListener {
                    viewModel.shareBluetooth(data[position], load)
                }
            }
            2 -> {
                holder.binding.broadcast.setOnClickListener {
                    viewModel.postHttp(data[position], load)
                }
            }
            3 -> {
                holder.binding.broadcast.setOnClickListener {
                    viewModel.publishMqtt(data[position], load)
                }
            }
        }
    }

    /* **********************************************************************
    * Function
    ********************************************************************** */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data : MutableList<Remote>) {
        this.data = data
        notifyDataSetChanged()
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    class ViewHolder(val binding: ItemRemoteBinding) : RecyclerView.ViewHolder(binding.root)
}