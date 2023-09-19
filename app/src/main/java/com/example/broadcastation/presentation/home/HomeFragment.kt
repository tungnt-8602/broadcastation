package com.example.broadcastation.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
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


 class HomeFragment : BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private var transaction: FragmentTransaction? = null

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
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
        logger.i("1 ${shareViewModel.remoteLiveList.value}")

        val adapter = ItemRemoteAdapter()
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        shareViewModel.remoteLiveList.observe(viewLifecycleOwner) { remotes ->
            logger.i("list: $remotes")
            adapter.setData(remotes)
        }
        binding.remoteList.adapter = adapter


        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
//            shareViewModel.addRemote(Remote("Data", "", 1, R.drawable.ic_local_fill))
            transaction?.replace(R.id.mainContainer, AddFragment.newInstance(), null)?.commit()
        }
    }


     /* **********************************************************************
      * Function
      ********************************************************************** */
     override fun onDetach() {
         super.onDetach()
         logger.d("onDetach")
     }

     override fun onResume() {
         super.onResume()
         logger.d("onResume")
     }

    /* **********************************************************************
     * Class
     ********************************************************************** */
}