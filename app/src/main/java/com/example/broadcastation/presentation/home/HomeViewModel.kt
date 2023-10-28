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
    val undoDelete = R.string.undo_delete
    val deleteConfirmTitle = R.string.delete_confirm_title
    val deleteConfirmMessage = R.string.delete_confirm_message
    val yesConfirm = R.string.yes_confirm
    val noConfirm = R.string.no_confirm

    val noticeCustom = R.string.custom_noti
    val noticeNormal = R.string.normal_noti
    val noticeGrid = R.string.grid_noti
    val noticeCategory = R.string.category_noti
    val noticeBroadcastType = R.string.broadcast_noti


    val menuFilter = R.menu.filter_menu

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
        Grid,
        Category,
        Broadcast
    }
}