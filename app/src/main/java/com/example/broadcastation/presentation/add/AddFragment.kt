package com.example.broadcastation.presentation.add

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_HOME_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.ItemRemoteAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddFragment(private val callback: HomeFragment.Callback) :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private val addViewModel: AddViewModel by viewModels()
    private var typeBroadcast: ItemRemoteAdapter.Type = ItemRemoteAdapter.Type.BLUETOOTH
    private lateinit var remoteUpdate: Remote

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager

        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            callback.saveMessage("")
            screenNavigate(
                fragmentManager,
                MainActivity.Navigate.DOWN,
                R.id.mainContainer,
                fragmentManager?.findFragmentByTag(
                    TAG_HOME_FRAGMENT
                ) ?: HomeFragment(callback)
            )
        }

        logger.i("Handle dropdown menu ")
        val listRemote = resources.getStringArray(R.array.remote_menu)
        val listCategoryRemote = resources.getStringArray(R.array.remote_category).toMutableList()
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, listCategoryRemote)
        val optionAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, listRemote)

        binding.categoryNameText.setAdapter(categoryAdapter)
        binding.categoryNameText.setOnItemClickListener { _, _, i, _ ->
            if (i == 0) {
                val builder = AlertDialog.Builder(requireContext())
                val inflater = layoutInflater
                builder.setTitle(resources.getString(R.string.add_category_title))
                val dialogLayout = inflater.inflate(R.layout.layout_add_category, null)
                val newCategory =
                    dialogLayout.findViewById<TextInputEditText>(R.id.add_category_text)
                builder.setView(dialogLayout)
                builder.setPositiveButton("OK") { _, _ ->
                    if (newCategory.text.isNullOrEmpty()) {
                        Snackbar.make(
                            binding.root,
                            resources.getString(R.string.add_category_fail),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        listCategoryRemote.add(newCategory.text.toString())
                        categoryAdapter.notifyDataSetChanged()
                            Snackbar.make(
                                binding.root,
                                "${newCategory.text}: ${resources.getString(R.string.add_category_success)}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                    }
                }
                builder.show()
            }
        }

        logger.i("Handle option of remote by typeBroadcast")
        binding.optionNameText.doAfterTextChanged {
            when (it.toString()) {
                listRemote[0] -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteAdapter.Type.BLUETOOTH
                }

                listRemote[1] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteAdapter.Type.HTTP
                }

                listRemote[2] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                    typeBroadcast = ItemRemoteAdapter.Type.MQTT
                }
            }
        }
        binding.optionNameText.setAdapter(optionAdapter)

        logger.i("Receive data from Home")
        setFragmentResultListener(ID_REQUEST_KEY) { _, bundle ->
            val id = bundle.getInt(ID_ARG)
            logger.i("Set title when update")
            binding.addFragment.text = resources.getString(R.string.update_title)
            logger.i("Find remote by id")
            remoteUpdate = callback.findRemoteById(id)
            logger.i("Set value of remote item to update form")
            binding.remoteNameText.setText(remoteUpdate.name)
            binding.remoteDescriptionText.setText(remoteUpdate.describe)
            when (remoteUpdate.type) {
                ItemRemoteAdapter.Type.BLUETOOTH -> {
                    binding.optionNameText.setText(listRemote[0])
                }

                ItemRemoteAdapter.Type.HTTP -> {
                    binding.optionNameText.setText(listRemote[1])
                }

                ItemRemoteAdapter.Type.MQTT -> {
                    binding.optionNameText.setText(listRemote[2])
                }
            }
        }

        logger.i("Navigate to home")
        binding.saveRemote.setOnClickListener {
            saveInputAsRemote(
                binding.remoteNameText.text.toString(),
                binding.remoteDescriptionText.text.toString(),
                typeBroadcast
            )
            screenNavigate(
                fragmentManager,
                MainActivity.Navigate.DOWN,
                R.id.mainContainer,
                fragmentManager?.findFragmentByTag(TAG_HOME_FRAGMENT) ?: HomeFragment(callback)
            )

        }
    }


    /* ***********************************************************************
     * Function
     ********************************************************************** */
    private fun saveInputAsRemote(name: String, describe: String, type: ItemRemoteAdapter.Type) {
        val icon = when (type) {
            ItemRemoteAdapter.Type.HTTP -> {
                R.drawable.ic_http
            }

            ItemRemoteAdapter.Type.BLUETOOTH -> {
                R.drawable.ic_local
            }

            ItemRemoteAdapter.Type.MQTT -> {
                R.drawable.ic_mqtt
            }
        }
        if (callback.getActionRemote() == TAG_ADD_FRAGMENT) {
            callback.saveMessage(resources.getString(R.string.mes_add_success))
//            viewModel.notice.value = resources.getString(R.string.mes_add_success)
            var lastId = 1
            val remoteArray = callback.getAllRemote()
            if (remoteArray.isNotEmpty()) {
                lastId += remoteArray.last().id
            }
            callback.addRemote(Remote(lastId, name, describe, type, icon))
        } else if (callback.getActionRemote() == TAG_UPDATE_FRAGMENT) {
            callback.saveMessage(resources.getString(R.string.mes_update_success))
//            viewModel.notice.value = resources.getString(R.string.mes_update_success)
            val oldRemoteList = callback.getAllRemote()
            oldRemoteList.forEach {
                if (it.id == remoteUpdate.id) {
                    it.name = name
                    it.describe = describe
                    it.type = type
                    it.icon = icon
                }
            }
            callback.updateRemote(oldRemoteList)
        }
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */

    interface Callback {
        fun addRemote(remote: Remote)
        fun findRemoteById(id: Int): Remote
        fun updateRemote(remotes: MutableList<Remote>)
        fun getAllRemote(): MutableList<Remote>
        fun getActionRemote(): String
        fun saveMessage(message: String)
    }
}