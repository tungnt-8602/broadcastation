package com.example.broadcastation.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.add.AddFragment
import com.example.broadcastation.presentation.add.AddViewModel


open class HomeFragment : BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private var transaction: FragmentTransaction? = null
    private val viewModel: HomeViewModel by viewModels()

    companion object {
        val instance = HomeFragment()
    }

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager
        transaction = fragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.slide_in,  // enter
            R.anim.fade_out,  // exit
            R.anim.fade_in,   // popEnter
            R.anim.slide_out  // popExit
        )
        logger.i("1 ${viewModel.remoteList.value}")


        viewModel.addRemote(Remote("Bluetooth", "", 1))

        val adapter = ItemRemoteAdapter()
        viewModel.remoteList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.empty.visibility = View.VISIBLE
                binding.remoteList.visibility = View.GONE
            } else {
                binding.remoteList.visibility = View.VISIBLE
                binding.empty.visibility = View.GONE
            }
            logger.i("list: ${viewModel.remoteList.value}")
            adapter.setData(it)
        }
        binding.remoteList.adapter = adapter
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            transaction?.replace(R.id.mainContainer, AddFragment(), null)?.addToBackStack(null)
                ?.commit()
        }
    }


    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
}