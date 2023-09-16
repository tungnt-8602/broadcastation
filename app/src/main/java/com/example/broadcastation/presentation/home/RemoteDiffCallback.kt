package com.example.broadcastation.presentation.home

import androidx.recyclerview.widget.DiffUtil
import com.example.broadcastation.entity.Remote

class RemoteDiffCallback(
    private val oldList: MutableList<Remote>,
    private val newList: MutableList<Remote>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        newList[newItemPosition].name == oldList[oldItemPosition].name

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        newList[newItemPosition] == oldList[oldItemPosition]
}