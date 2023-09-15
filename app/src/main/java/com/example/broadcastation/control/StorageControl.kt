package com.example.broadcastation.control

import com.example.broadcastation.common.base.BaseControl

class StorageControl : BaseControl() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    companion object {
        var instance = StorageControl()
    }
    var deviceId = ""
}