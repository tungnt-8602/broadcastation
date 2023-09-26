package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.DESC_ARG
import com.example.broadcastation.common.utility.DESC_REQUEST_KEY
import com.example.broadcastation.common.utility.EDIT_ARG
import com.example.broadcastation.common.utility.EDIT_REQUEST_KEY
import com.example.broadcastation.common.utility.EDIT_TITLE
import com.example.broadcastation.common.utility.ICON_ARG
import com.example.broadcastation.common.utility.ICON_REQUEST_KEY
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.NAME_ARG
import com.example.broadcastation.common.utility.NAME_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext


class HomeFragment(val callback: Callback) : BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager

        logger.i("Empty/list display")
        val remoteList = callback.getAllRemote()
        if(remoteList.isNullOrEmpty() || remoteList.size == 0) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        }else{
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Recycler view")
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = ItemRemoteAdapter(callback = object: ItemRemoteAdapter.Callback{
            override fun shareBluetooth(remote: Remote, view: View) {
                callback.grantBluetoothPermission()
                viewModel.shareBluetooth(remote, view)
            }

            override fun postHttp(remote: Remote, view: View) {
                viewModel.postHttp(remote, view)
            }

            override fun publishMqtt(remote: Remote, view: View) {
                viewModel.publishMqtt(remote, view)
            }


        }, binding.idLoadingPB)
        adapter.setData(remoteList as MutableList<Remote>)
        binding.remoteList.adapter = adapter

        logger.i("Item navigate: Update remote")
        adapter.setOnItemTouchListener {
            viewModel.editRemote("update")
            setFragmentResult(EDIT_REQUEST_KEY, bundleOf(EDIT_ARG to EDIT_TITLE))
            setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to it.id))
            screenNavigate(fragmentManager, R.id.mainContainer, AddFragment(callback))
        }

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            viewModel.editRemote("add")
            screenNavigate(fragmentManager, R.id.mainContainer, AddFragment(callback), TAG_ADD_FRAGMENT)
        }

        logger.i("Message after type")
        callback.updateNotice(viewLifecycleOwner, binding.add)
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
    abstract class Callback : AddFragment.Callback {
         abstract fun getAllRemote() : ArrayList<Remote>
         abstract fun updateNotice(owner: LifecycleOwner, view: View)
         abstract fun grantBluetoothPermission()
    }
}