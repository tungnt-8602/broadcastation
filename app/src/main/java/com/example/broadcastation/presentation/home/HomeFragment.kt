package com.example.broadcastation.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.add.AddFragment


open class HomeFragment : BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager : FragmentManager? = null
    private var transaction : FragmentTransaction? = null
    var remoteList = mutableListOf<Remote>()

    companion object{
        val instance = HomeFragment()
    }

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isAdded){
            return
        }
        fragmentManager = activity?.supportFragmentManager
        transaction = fragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.slide_in,  // enter
            R.anim.fade_out,  // exit
            R.anim.fade_in,   // popEnter
            R.anim.slide_out  // popExit
        )

        if(remoteList.isEmpty()){
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        }else{
            binding.remoteList.visibility = View.VISIBLE
            binding.empty.visibility = View.GONE
        }

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            transaction?.replace(R.id.mainContainer, AddFragment(), null)?.commit()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
}