package com.example.broadcastation.entity.config

import com.example.broadcastation.presentation.home.ItemRemoteAdapter

data class BluetoothConfig(val deviceName: String, val content: String) :
    Config(ItemRemoteAdapter.Type.BLUETOOTH)