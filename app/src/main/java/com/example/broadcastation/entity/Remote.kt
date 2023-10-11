package com.example.broadcastation.entity

import com.example.broadcastation.presentation.home.ItemRemoteAdapter

data class Remote(val id: Int, var name: String, var describe: String, var category: String, var type: ItemRemoteAdapter.Type, var icon: Int, var config: String)
