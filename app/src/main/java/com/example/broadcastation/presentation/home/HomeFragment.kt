package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.DRAG_DIRS
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.BluetoothConfig
import com.example.broadcastation.entity.config.Config
import com.example.broadcastation.entity.config.MqttConfig
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.example.broadcastation.presentation.home.item.ItemMoveCustomCallback
import com.example.broadcastation.presentation.home.item.ItemRemoteBroadcastAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteCategoryAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteGridAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class HomeFragment(val callback: Callback) :
    BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    lateinit var type: Type
    val gson = Gson()
    private lateinit var config: Config
    private lateinit var categoryData: MutableMap<MutableList<Any>, MutableList<Remote>>
    private lateinit var broadcastData: MutableMap<MutableList<Any>, MutableList<Remote>>
    private lateinit var remoteList: MutableList<Remote>
    private lateinit var itemCallBack : ItemRemoteCustomAdapter.Callback
    private lateinit var gridAdapter : ItemRemoteGridAdapter
    private lateinit var categoryAdapter : ItemRemoteCategoryAdapter
    private lateinit var broadcastAdapter : ItemRemoteBroadcastAdapter
    private lateinit var customAdapter : ItemRemoteCustomAdapter
    private lateinit var itemCustomCallback: ItemTouchHelper.Callback
    private lateinit var touchCustomHelper: ItemTouchHelper

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = parentFragmentManager

        logger.i("Empty/list display")
        remoteList = callback.getAllRemote()
        if (remoteList.isEmpty()) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Common item callback for all adapter")
        itemCallBack = object : ItemRemoteCustomAdapter.Callback {
            override fun shareBluetooth(remote: Remote, callback: Callback) {
                val isTurnedOn =
                    activity?.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled
                try {
                    if (isTurnedOn == true) {
                        callback.shareBluetooth(remote, callback)
                        type = object : TypeToken<BluetoothConfig>() {}.type
                        config = gson.fromJson(remote.config, type)
                        callback.saveMessageBroadcast((config as BluetoothConfig).content)
                        homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
                    } else {
                        callback.grantBluetoothPermission(remote, callback, binding.add)
                    }
                } catch (e: Exception) {
                    logger.w(e.message ?: "Share bluetooth")
                }
            }

            override fun postHttp(remote: Remote) {
                callback.postHttp(remote)
                homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
            }

            override fun getHttp(remote: Remote) {
                callback.getHttp(remote)
                homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
            }

            override fun publishMqtt(remote: Remote) {
                try {
                    type = object : TypeToken<MqttConfig>() {}.type
                    config = gson.fromJson(remote.config, type)
                    callback.publishMqtt(remote, callback, requireContext())
                    callback.saveMessageBroadcast((config as MqttConfig).content)
                    homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
                } catch (e: Exception) {
                    logger.w(e.message ?: "Publish Mqtt")
                }
            }
        }

        categoryData = remoteToDataList(callback.getAllRemote(), HomeViewModel.SortType.Category)
        broadcastData = remoteToDataList(callback.getAllRemote(), HomeViewModel.SortType.Broadcast)

//        val adapter = ItemRemoteNormalAdapter(callback = itemCallBack, callback)
        gridAdapter = ItemRemoteGridAdapter(callback = itemCallBack, callback)
        categoryAdapter = ItemRemoteCategoryAdapter(callback = itemCallBack, callback)
        broadcastAdapter = ItemRemoteBroadcastAdapter(callback = itemCallBack, callback)
        customAdapter = ItemRemoteCustomAdapter(callback = itemCallBack, callback)
        itemCustomCallback = ItemMoveCustomCallback(customAdapter)
        touchCustomHelper = ItemTouchHelper(itemCustomCallback)
        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(DRAG_DIRS, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedRemote: Remote = remoteList[position]
                remoteList.removeAt(position)
                this@HomeFragment.callback.updateRemote(remoteList)
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(resources.getString(homeViewModel.deleteConfirmTitle))
                builder.setMessage(resources.getString(homeViewModel.deleteConfirmMessage))
                builder.setPositiveButton(resources.getString(homeViewModel.yesConfirm)) { _,_ ->
                    notifyDataChangeAllAdapter(remoteList)
                    Snackbar.make(
                        binding.root,
                        "${resources.getString(homeViewModel.deleteRemote)} ${deletedRemote.name}",
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding.add)
                        .setAction(homeViewModel.undo) {
                            remoteList.add(position, deletedRemote)
                            this@HomeFragment.callback.updateRemote(remoteList)
                            notifyDataChangeAllAdapter(remoteList)
                        }.show()
                }
                builder.setNegativeButton(resources.getString(homeViewModel.noConfirm)){_,_ ->
                    remoteList.add(position, deletedRemote)
                    this@HomeFragment.callback.updateRemote(remoteList)
                    notifyDataChangeAllAdapter(remoteList)
                    Snackbar.make(
                        binding.root,
                        resources.getString(homeViewModel.undoDelete),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding.add)
                        .show()
                }
                builder.show()
            }
        })

        bindAdapter(homeViewModel.getSortType().toString())

//        when(homeViewModel.getSortType()){
//            HomeViewModel.SortType.Normal-> {
//                customAdapter.setData(remoteList)
//                binding.remoteList.adapter = customAdapter
//                touchCustomHelper.attachToRecyclerView(binding.remoteList)
//                homeViewModel.saveSortType(HomeViewModel.SortType.Normal)
//                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
//            }
//            HomeViewModel.SortType.Grid -> {
//                gridAdapter.setData(remoteList)
//                binding.remoteList.adapter = gridAdapter
//                binding.remoteList.layoutManager = GridLayoutManager(requireContext(), 3)
//                touchCustomHelper.attachToRecyclerView(null)
//                homeViewModel.saveSortType(HomeViewModel.SortType.Grid)
//                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeGrid))
//            }
//            HomeViewModel.SortType.Category -> {
//                categoryAdapter.setData(categoryData)
//                binding.remoteList.adapter = categoryAdapter
//                touchCustomHelper.attachToRecyclerView(null)
//                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
//                homeViewModel.saveSortType(HomeViewModel.SortType.Category)
//                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCategory))
//            }
//            HomeViewModel.SortType.Broadcast -> {
//                broadcastAdapter.setData(broadcastData)
//                binding.remoteList.adapter = broadcastAdapter
//                touchCustomHelper.attachToRecyclerView(null)
//                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
//                homeViewModel.saveSortType(HomeViewModel.SortType.Broadcast)
//                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeBroadcastType))
//            }
//        }

        binding.sortRemote.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.apply {
                menuInflater.inflate(homeViewModel.menuFilter, popup.menu)
                setForceShowIcon(true)
                setOnMenuItemClickListener { menuItem: MenuItem ->
                    bindAdapter(menuItem.title.toString())
//                    when (menuItem.title) {
//                        resources.getString(R.string.normal_filter)-> {
//                            customAdapter.setData(remoteList)
//                            binding.remoteList.adapter = customAdapter
//                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
//                            touchCustomHelper.attachToRecyclerView(binding.remoteList)
//                            homeViewModel.saveSortType(HomeViewModel.SortType.Normal)
//                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
//                        }
//                        resources.getString(R.string.grid_filter) -> {
//                            gridAdapter.setData(remoteList)
//                            binding.remoteList.adapter = gridAdapter
//                            binding.remoteList.layoutManager = GridLayoutManager(requireContext(), 3)
//                            touchCustomHelper.attachToRecyclerView(null)
//                            homeViewModel.saveSortType(HomeViewModel.SortType.Grid)
//                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
//                        }
//                        resources.getString(R.string.category_filter) -> {
//                            categoryAdapter.setData(categoryData)
//                            binding.remoteList.adapter = categoryAdapter
//                            touchCustomHelper.attachToRecyclerView(null)
//                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
//                            homeViewModel.saveSortType(HomeViewModel.SortType.Category)
//                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCategory))
//                        }
//                        resources.getString(R.string.broadcast_filter) -> {
//                            broadcastAdapter.setData(broadcastData)
//                            binding.remoteList.adapter = broadcastAdapter
//                            touchCustomHelper.attachToRecyclerView(null)
//                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
//                            homeViewModel.saveSortType(HomeViewModel.SortType.Broadcast)
//                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeBroadcastType))
//                        }
//                    }
                    true
                }
                show()
            }
        }

        touchHelper.attachToRecyclerView(binding.remoteList)

        customAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        gridAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        categoryAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        broadcastAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            callback.saveMessageAction(TAG_ADD_FRAGMENT)
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
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.add)
                .show()
        }
        homeViewModel.notice.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.add).show()
        }
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    private fun navigateUpdate(callback: Callback, remote: Remote){
        callback.saveMessageAction(TAG_UPDATE_FRAGMENT)
        setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to remote.id))
        screenNavigate(
            fragmentManager,
            MainActivity.Navigate.UP,
            R.id.mainContainer,
            AddFragment(callback)
        )
    }

    private fun remoteToDataList(remoteList : MutableList<Remote>, filter: HomeViewModel.SortType) : MutableMap<MutableList<Any>, MutableList<Remote>>{
        val mapData = mutableMapOf<MutableList<Any>, MutableList<Remote>>()
        for(remote in remoteList){
            val key : MutableList<Any> = mutableListOf()
            if(filter == HomeViewModel.SortType.Category){
                key.add(remote.category)
            }else if(filter == HomeViewModel.SortType.Broadcast){
                key.add(remote.type.toString())
            }
            if (!mapData.containsKey(key)) {
                mapData[key] = mutableListOf()
                mapData[key]?.add(remote)
            }else{
                mapData[key]?.add(remote)
            }
        }
        return mapData
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataChangeAllAdapter(remoteList: MutableList<Remote>){
        customAdapter.notifyDataSetChanged()
        gridAdapter.notifyDataSetChanged()
        categoryData = remoteToDataList(remoteList, HomeViewModel.SortType.Category)
        broadcastData = remoteToDataList(remoteList, HomeViewModel.SortType.Broadcast)
        categoryAdapter.notifyDataSetChanged()
        broadcastAdapter.notifyDataSetChanged()
    }

    private fun bindAdapter(sortType : String){
        when(sortType){
            resources.getString(R.string.normal_filter)-> {
                customAdapter.setData(remoteList)
                binding.remoteList.adapter = customAdapter
                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                touchCustomHelper.attachToRecyclerView(binding.remoteList)
                homeViewModel.saveSortType(HomeViewModel.SortType.Normal)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
            }
            resources.getString(R.string.grid_filter) -> {
                gridAdapter.setData(remoteList)
                binding.remoteList.adapter = gridAdapter
                binding.remoteList.layoutManager = GridLayoutManager(requireContext(), 3)
                touchCustomHelper.attachToRecyclerView(null)
                homeViewModel.saveSortType(HomeViewModel.SortType.Grid)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
            }
            resources.getString(R.string.category_filter) -> {
                categoryAdapter.setData(categoryData)
                binding.remoteList.adapter = categoryAdapter
                touchCustomHelper.attachToRecyclerView(null)
                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                homeViewModel.saveSortType(HomeViewModel.SortType.Category)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCategory))
            }
            resources.getString(R.string.broadcast_filter) -> {
                broadcastAdapter.setData(broadcastData)
                binding.remoteList.adapter = broadcastAdapter
                touchCustomHelper.attachToRecyclerView(null)
                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                homeViewModel.saveSortType(HomeViewModel.SortType.Broadcast)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeBroadcastType))
            }
        }
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    interface Callback : AddFragment.Callback {
        fun grantBluetoothPermission(remote: Remote, callback: Callback, view: View)
        fun shareBluetooth(remote: Remote, callback: Callback)
        fun postHttp(remote: Remote)
        fun getHttp(remote: Remote)
        fun publishMqtt(remote: Remote, callback: Callback, context: Context)

        fun updateNotice(): String
        fun saveMessageAction(message: String)
        fun getMessageAction(): String
        fun getMessageBroadcast(): String
        fun saveMessageBroadcast(message: String)
        fun startAdvertise(advertise: String, message: String)
        fun stopAdvertise()
    }
}