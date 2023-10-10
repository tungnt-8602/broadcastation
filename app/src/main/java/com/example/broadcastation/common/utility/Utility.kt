package com.example.broadcastation.common.utility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.broadcastation.R
import com.example.broadcastation.presentation.MainActivity

const val ID_REQUEST_KEY = "requestId"
const val ID_ARG = "id"
const val EMPTY = "Không có gì"
const val ERROR = "Lỗi: "

const val USER_NAME= "tungnt"
const val PASSWORD = "8602"

const val GET_METHOD = "GET"
const val POST_METHOD = "POST"

const val TAG_HOME_FRAGMENT = "home"
const val TAG_ADD_FRAGMENT = "add"
const val TAG_UPDATE_FRAGMENT = "update"
const val POST_URL = "https://reqres.in/api/"
const val GET_URL = "https://api.chucknorris.io/"
const val GET_SUCCESS = "Lấy dữ liệu thành công từ"

const val FIRST_STACK = 1
const val DELAY_TIME_TO_QUIT : Long = 2000

@SuppressLint("HardwareIds")
fun Context.getUUID(): String {
    return Settings.Secure.getString(
        contentResolver, Settings.Secure.ANDROID_ID
    )
}

fun screenNavigate(fragmentManager: FragmentManager?, navDirection: MainActivity.Navigate, containerView: Int, aimFragment: Fragment, tag: String? = null){
    fragmentManager?.commit {
        if (navDirection == MainActivity.Navigate.DOWN) {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
        }else{
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
        }
        replace(
            containerView,
            aimFragment,
            tag
        )
        addToBackStack(null)
        setReorderingAllowed(true)
    }
}

fun getDeviceName(): String {
    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else {
        capitalize(manufacturer) + model
    }
}

private fun capitalize(s: String?): String {
    if (s.isNullOrEmpty()) {
        return ""
    }
    val first = s[0]
    return if (Character.isUpperCase(first)) {
        s
    } else {
        first.uppercaseChar().toString() + s.substring(1)
    }
}

fun showMenu(v: View, @MenuRes menuRes: Int, context: Context) {
    val popup = PopupMenu(context, v)
    popup.menuInflater.inflate(menuRes, popup.menu)
    popup.setOnMenuItemClickListener { menuItem: MenuItem ->
        (v as TextView).text = menuItem.title.toString()
        true
    }
    popup.show()
}

