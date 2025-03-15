package com.adrianl.financetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adrianl.financetracker.databinding.FragmentManageTxBinding

abstract class BaseManageTxFragment : Fragment() {
    protected lateinit var binding: FragmentManageTxBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageTxBinding.inflate(inflater, container, false)
        return binding.root
    }
}