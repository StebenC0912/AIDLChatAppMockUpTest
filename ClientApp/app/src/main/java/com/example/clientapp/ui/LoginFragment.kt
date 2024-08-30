package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.clientapp.MainViewModel
import com.example.clientapp.R
import com.example.clientapp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val screenWidth by lazy {
        resources.displayMetrics.widthPixels.toFloat()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setInitialPositions()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateViewsIn()
        viewModel.bindService()
        
        binding.registerButton.setOnClickListener {
            animateViewsOutAndNavigate()
        }
        binding.loginButton.setOnClickListener {
            handleLogin()
        }
    }
    
    private fun setInitialPositions() {
        binding.registerButton.translationX = screenWidth
        binding.loginTextView.translationX = -screenWidth
        binding.enterYourEmailAndPasswordTextView.translationX = -screenWidth
    }
    
    private fun animateViewsIn() {
        binding.registerButton.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
        
        binding.loginTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
        
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
    }
    
    private fun animateViewsOutAndNavigate() {
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .start()
        
        binding.loginTextView.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .start()
        
        binding.rememberMeLayout.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .start()
        
        binding.registerButton.animate()
            .translationX(screenWidth)
            .setDuration(500)
            .withEndAction {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            .start()
        
    }
    
    private fun handleLogin() {
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        
        if (username.isNotBlank() && password.isNotBlank()) {
            val user = viewModel.login(username, password)
            if (user != null) {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
