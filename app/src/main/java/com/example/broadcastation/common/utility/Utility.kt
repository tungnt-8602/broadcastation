package com.example.broadcastation.common.utility

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.broadcastation.R
import com.example.broadcastation.presentation.MainActivity

const val ID_REQUEST_KEY = "requestId"
const val ID_ARG = "id"
const val EMPTY = "Không có gì"
const val ERROR = "Lỗi: "

const val TAG_HOME_FRAGMENT = "home"
const val TAG_ADD_FRAGMENT = "add"
const val TAG_UPDATE_FRAGMENT = "update"
const val BASE_URL = "https://reqres.in/api/"

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

