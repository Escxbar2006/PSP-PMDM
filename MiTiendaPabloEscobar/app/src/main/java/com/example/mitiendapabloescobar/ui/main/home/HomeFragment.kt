package com.example.mitiendapabloescobar.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.mitiendapabloescobar.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.webView.apply {
            // Permitimos que la web se cargue dentro del WebView
            webViewClient = WebViewClient()
            
            // Configuraciones básicas para una web moderna
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            

            loadUrl("file:///android_asset/index.html")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
