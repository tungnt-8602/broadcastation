package com.example.broadcastation.entity.http

import com.example.broadcastation.entity.Mqtt
import com.example.broadcastation.presentation.home.ItemRemoteAdapter
import java.util.UUID

data class Http (val url: String, val method: ItemRemoteAdapter.HttpMethod, val content: String)