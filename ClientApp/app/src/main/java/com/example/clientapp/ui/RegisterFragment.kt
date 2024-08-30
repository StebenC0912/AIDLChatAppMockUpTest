package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.clientapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val screenWidth by lazy {
        resources.displayMetrics.widthPixels.toFloat()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setInitialPositions()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateViewsIn()
        binding.loginButton.setOnClickListener {
            animateViewsOut()
        }
    }
    
    private fun setInitialPositions() {
        binding.loginButton.translationX = -screenWidth
        binding.registerTextView.translationX = screenWidth
        binding.enterYourEmailAndPasswordTextView.translationX =
            screenWidth
    }
    
    private fun animateViewsIn() {
        binding.loginButton.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
        
        binding.registerTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()                // Navigate back to the login fragment
        
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
    }
    
    private fun animateViewsOut() {
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(screenWidth)
            .setDuration(500)
            .start()
        
        binding.registerTextView.animate()
            .translationX(screenWidth)
            .setDuration(500)
            .start()
        
        binding.loginButton.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .withEndAction {
                requireActivity().onBackPressed()
            }
            .start()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
