package com.example.broadcastation.entity.config

import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter

data class HttpConfig(
    val url: String,
    val method: ItemRemoteCustomAdapter.HttpMethod,
    val content: String
) : Config(ItemRemoteCustomAdapter.Type.HTTP)