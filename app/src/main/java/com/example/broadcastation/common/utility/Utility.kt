package com.example.broadcastation.common.utility

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

@SuppressLint("HardwareIds")
fun Context.getUUID(): String {
    return Settings.Secure.getString(
        contentResolver, Settings.Secure.ANDROID_ID
    )
}
