package com.example.broadcastation.presentation.add

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.broadcastation.R
import com.example.broadcastation.common.BaseFragment
import com.example.broadcastation.databinding.AddFragmentBinding

class AddFragment :
BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private lateinit var fragmentManager : FragmentManager
    private lateinit var transaction : FragmentTransaction

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isAdded){
            return
        }
        fragmentManager = childFragmentManager
        transaction = fragmentManager.beginTransaction()
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