package com.example.broadcastation.presentation.add

import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel

class AddViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    val notice = MutableLiveData<String>()

    val listRemote = R.array.remote_menu
    val listCategoryRemote = R.array.remote_category

    val dropdownItem = R.layout.dropdown_item

    val menuCategory = R.menu.category_menu
    val menuBroadcast = R.menu.broadcast_menu
    val menuHttpMethod = R.menu.http_method_menu

    val updateTitle = R.string.update_title
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