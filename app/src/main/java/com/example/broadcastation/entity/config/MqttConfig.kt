package com.example.broadcastation.entity.config

import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter

data class MqttConfig(
    val user: String,
    val password: String,
    val domain: String,
    val port: String?,
    val channel: String,
    val content: String
) : Config(ItemRemoteCustomAdapter.Type.MQTT)