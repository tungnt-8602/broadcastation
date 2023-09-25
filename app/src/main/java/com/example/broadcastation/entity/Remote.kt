package com.example.broadcastation.entity

import com.example.broadcastation.presentation.home.ItemRemoteAdapter

data class Remote(val id: Int, val name: String, val describe: String, val type: ItemRemoteAdapter.Type, val icon: Int)
