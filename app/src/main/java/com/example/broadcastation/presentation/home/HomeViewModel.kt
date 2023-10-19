package com.example.broadcastation.presentation.home

import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel


class HomeViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    val notice = MutableLiveData<String>()
    val undo = R.string.undo
    val deleteRemote = R.string.delete_remote

    val noticeGetHttp = R.string.get_http_notice
    val noticePostHttp = R.string.post_http_notice

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun noticeBroadcast(newNotice: String){
        notice.value = newNotice
    }

    fun getSortType() = local.getSortType()

    fun saveSortType(type: SortType){
        local.saveSortType(type)
    }

    /* **********************************************************************
    * Class
    ********************************************************************** */
    enum class SortType {
        Normal,
        Category,
        Custom
    }
}