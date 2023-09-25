package com.example.broadcastation.common.utility

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import android.text.Layout
import android.view.View

const val NAME_REQUEST_KEY = "requestName"
const val DESC_REQUEST_KEY = "requestDes"
const val ICON_REQUEST_KEY = "requestIcon"
const val EDIT_REQUEST_KEY = "requestEdit"
const val NAME_ARG = "name"
const val DESC_ARG = "des"
const val ICON_ARG = "icon"
const val EDIT_ARG = "edit"
const val EDIT_TITLE = "Cập nhật điều khiển"

const val MES_ADD_SUCCESS = "Add successfully"
const val MES_UPDATE_SUCCESS = "Update successfully"

const val TAG_HOME_FRAGMENT = "home"
const val TAG_ADD_FRAGMENT = "add"

@SuppressLint("HardwareIds")
fun Context.getUUID(): String {
    return Settings.Secure.getString(
        contentResolver, Settings.Secure.ANDROID_ID
    )
}

