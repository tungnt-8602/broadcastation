package com.example.broadcastation.common.utility

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.broadcastation.R
import java.lang.Error

const val ID_REQUEST_KEY = "requestId"
const val NAME_REQUEST_KEY = "requestName"
const val DESC_REQUEST_KEY = "requestDes"
const val ICON_REQUEST_KEY = "requestIcon"
const val EDIT_REQUEST_KEY = "requestEdit"
const val ID_ARG = "id"
const val NAME_ARG = "name"
const val DESC_ARG = "des"
const val ICON_ARG = "icon"
const val EDIT_ARG = "edit"
const val EDIT_TITLE = "Cập nhật điều khiển"

const val MES_ADD_SUCCESS = "Add successfully"
const val MES_UPDATE_SUCCESS = "Update successfully"
const val EMPTY = "Nothing to show"
const val ERROR = "Error found is : "

const val TAG_HOME_FRAGMENT = "home"
const val TAG_ADD_FRAGMENT = "add"
const val TAG_UPDATE_FRAGMENT = "update"
const val BASE_URL = "https://reqres.in/api/"

@SuppressLint("HardwareIds")
fun Context.getUUID(): String {
    return Settings.Secure.getString(
        contentResolver, Settings.Secure.ANDROID_ID
    )
}

fun screenNavigate(fragmentManager: FragmentManager?, containerView: Int, aimFragment: Fragment, tag: String? = null){
    fragmentManager?.commit {
        setCustomAnimations(
            R.anim.fade_in,
            R.anim.slide_out,
            R.anim.slide_in,
            R.anim.fade_out
        )
        replace(
            containerView,
            aimFragment,
            tag
        )
        addToBackStack(null)
        setReorderingAllowed(true)
    }
}

