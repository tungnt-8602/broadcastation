package com.example.broadcastation.entity.config

import com.example.broadcastation.presentation.home.ItemRemoteAdapter

data class HttpConfig(
    val url: String,
    val method: ItemRemoteAdapter.HttpMethod,
    val content: String
) : Config(ItemRemoteAdapter.Type.HTTP)