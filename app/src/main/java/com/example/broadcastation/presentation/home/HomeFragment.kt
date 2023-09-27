package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.google.android.material.snackbar.Snackbar


class HomeFragment(val callback: Callback) :
    BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
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
        if (remoteList.isEmpty() || remoteList.size == 0) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Recycler view")
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = ItemRemoteAdapter(callback = object : ItemRemoteAdapter.Callback {
            override fun shareBluetooth(remote: Remote) {
                callback.grantBluetoothPermission()
                callback.shareBluetooth(remote)
                callback.saveMessageAction(remote.name)
                Snackbar.make(binding.root, callback.getMessageAction(), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add).show()
            }

            override fun postHttp(remote: Remote) {
                callback.postHttp(remote)
                Snackbar.make(binding.root, callback.getMessageBroadcast(), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add).show()
            }

            override fun publishMqtt(remote: Remote) {
                callback.publishMqtt(remote)
                callback.saveMessageBroadcast(remote.name)
                Snackbar.make(binding.root, callback.getMessageBroadcast(), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add).show()
            }


        })
        adapter.setData(remoteList)
        binding.remoteList.adapter = adapter

        logger.i("Item navigate: Update remote")
        adapter.setOnItemTouchListener {
            viewModel.actionRemote("update")
            setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to it.id))
            screenNavigate(
                fragmentManager,
                MainActivity.Navigate.UP,
                R.id.mainContainer,
                AddFragment(callback)
            )
        }

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            viewModel.actionRemote("add")
            screenNavigate(
                fragmentManager,
                MainActivity.Navigate.UP,
                R.id.mainContainer,
                AddFragment(callback),
                TAG_ADD_FRAGMENT
            )
        }

        logger.i("Message after type")
        val message = callback.updateNotice()
        if (message != "") {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).setAnchorView(binding.add)
                .show()
        }
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
    abstract class Callback : AddFragment.Callback {
        abstract fun updateNotice(): String
        abstract fun grantBluetoothPermission()
        abstract fun shareBluetooth(remote: Remote)
        abstract fun postHttp(remote: Remote)
        abstract fun publishMqtt(remote: Remote)
        abstract fun saveMessageAction(message: String)
        abstract fun getMessageAction(): String
        abstract fun getMessageBroadcast(): String
        abstract fun saveMessageBroadcast(message: String)
    }
}