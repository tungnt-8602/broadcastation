package com.example.broadcastation.entity.config

import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter

data class BluetoothConfig(val deviceName: String, val content: String) :
    Config(ItemRemoteCustomAdapter.Type.BLUETOOTH)