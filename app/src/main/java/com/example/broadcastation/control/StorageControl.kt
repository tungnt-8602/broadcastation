package com.example.broadcastation.control

import com.example.broadcastation.common.base.BaseControl

class StorageControl : BaseControl() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    var advertisingName = ""
    var domainMqtt = "tcp://mqtt.bctoyz.com:1883"
    val userName = "Android"
    val passWord = "android123"
    var channel = "event1/all"
    var port = ""
    var content = ""

    companion object {
        var instance = StorageControl()
    }

    var deviceId = ""
}