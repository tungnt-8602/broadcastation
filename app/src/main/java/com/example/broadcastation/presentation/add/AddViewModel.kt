package com.example.broadcastation.presentation.add

import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel

class AddViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    val uuid = MutableLiveData<String>()
    val notice = MutableLiveData<String>()

    val listRemote = R.array.remote_menu
    val listCategoryRemote = R.array.remote_category


    /* **********************************************************************
     * Function
     ********************************************************************** */

    fun noticeVerify(newNotice: String){
        notice.value = newNotice
    }


    /* **********************************************************************
     * Class
     ********************************************************************** */
}