package com.prianshuparashar.newstime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.databinding.FragmentLanguageBinding
import com.prianshuparashar.newstime.di.module.FragmentModule

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun injectDependencies() {
        (requireActivity().application as NewsApplication)
            .applicationComponent
            .getFragmentComponentBuilder()
            .fragmentModule(FragmentModule(this))
            .build()
            .inject(this)
    }
}