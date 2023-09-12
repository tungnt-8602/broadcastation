package com.example.broadcastation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

typealias Inflate<VB> = (LayoutInflater, ViewGroup?, Boolean) -> VB

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment(){
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!
    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /* **********************************************************************
     * Function - Abstract
     ********************************************************************** */

    /* **********************************************************************
     * Function
     ********************************************************************** */
}