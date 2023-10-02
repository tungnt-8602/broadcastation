package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
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
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable


class HomeFragment :
    BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun instance(callback: Callback): HomeFragment {
            val homeFragment = HomeFragment()
            val args = Bundle()
            args.putSerializable("callback", callback)
            homeFragment.arguments = args
            return homeFragment
        }
    }

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = arguments?.getSerializable("callback", Callback::class.java)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager

        logger.i("Empty/list display")
        val remoteList = callback?.getAllRemote()
        if (remoteList?.isEmpty() == true || remoteList?.size == 0) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Recycler view")
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = callback?.let {
            ItemRemoteAdapter(callback = object : ItemRemoteAdapter.Callback {
                override fun shareBluetooth(remote: Remote, callback: Callback) {
                    var isTurnedOn =
                        activity?.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled
                    if (isTurnedOn == true) {
                        viewModel.shareBluetooth(remote, callback)
                        return
                    } else {
                        callback.grantBluetoothPermission(remote, callback)
                    }
                }

                override fun postHttp(remote: Remote) {
                    callback?.postHttp(remote)
                    callback?.getMessageBroadcast()?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.add).show()
                    }
                }

                override fun publishMqtt(remote: Remote) {
                    callback?.publishMqtt(remote)
                    callback?.saveMessageBroadcast(remote.name)
                    callback?.getMessageBroadcast()?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.add).show()
                    }
                }


            }, it)
        }
        if (remoteList != null) {
            adapter?.setData(remoteList)
        }
        binding.remoteList.adapter = adapter

        logger.i("Item navigate: Update remote")
        adapter?.setOnItemTouchListener {
            callback?.saveMessageAction(TAG_UPDATE_FRAGMENT)
            setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to it.id))
            callback?.let { callback -> AddFragment.instance(callback) }?.let { fragment ->
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.UP,
                    R.id.mainContainer,
                    fragment
                )
            }
        }

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            callback?.saveMessageAction(TAG_ADD_FRAGMENT)
            callback?.let { callback -> AddFragment.instance(callback) }?.let { fragment ->
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.UP,
                    R.id.mainContainer,
                    fragment,
                    TAG_ADD_FRAGMENT
                )
            }
        }

        logger.i("Message after type")
        val message = callback?.updateNotice()
        if (message != "") {
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add)
                    .show()
            }
        }
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
    abstract class Callback : AddFragment.Callback, Serializable {
        abstract fun grantBluetoothPermission(remote: Remote, callback: Callback)
        abstract fun shareBluetooth(remote: Remote, callback: Callback)
        abstract fun postHttp(remote: Remote)
        abstract fun publishMqtt(remote: Remote)

        abstract fun updateNotice(): String
        abstract fun saveMessageAction(message: String)
        abstract fun getMessageAction(): String
        abstract fun getMessageBroadcast(): String
        abstract fun saveMessageBroadcast(message: String)
        abstract fun startAdvertise(advertise: String, message: String)
        abstract fun stopAdvertise()
    }
}