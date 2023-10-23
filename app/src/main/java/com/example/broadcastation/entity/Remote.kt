package com.example.broadcastation.entity

import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter

data class Remote(
    val id: Int,
    var name: String,
    var describe: String,
    var category: String,
    var type: ItemRemoteCustomAdapter.Type,
    var icon: Int,
    var config: String
)
