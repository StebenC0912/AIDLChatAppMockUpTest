package com.example.clientapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
        setLoginButtonState(false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateViewsIn()
        viewModel.bindService()
        
        binding.registerButton.setOnClickListener {
            animateViewsOutAndNavigate()
        }
        binding.usernameEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.loginButton.setOnClickListener {
            handleLogin()
        }
    }
    
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            setLoginButtonState(username.isNotEmpty() && password.isNotEmpty())
        }
        
        override fun afterTextChanged(s: Editable?) {}
    }
    
    private fun setLoginButtonState(isEnabled: Boolean) {
        binding.loginButton.isEnabled = isEnabled
        val backgroundRes = if (isEnabled) {
            R.drawable.circular_gradient_button_active
        } else {
            R.drawable.circular_gradient_button_not_active
        }
        binding.loginButton.background = ContextCompat.getDrawable(requireContext(), backgroundRes)
    }
    
    private fun setInitialPositions() {
        binding.registerButton.translationX = screenWidth
        binding.loginTextView.translationX = -screenWidth
        binding.enterYourEmailAndPasswordTextView.translationX = -screenWidth
        binding.rememberMeLayout.translationX = -screenWidth
    }
    
    private fun animateViewsIn() {
        binding.registerButton.animate().translationX(0f).setDuration(500).start()
        
        binding.loginTextView.animate().translationX(0f).setDuration(500).start()
        
        binding.enterYourEmailAndPasswordTextView.animate().translationX(0f).setDuration(500)
            .start()
        
        binding.rememberMeLayout.animate().translationX(0f).setDuration(500).start()
    }
    
    private fun animateViewsOutAndNavigate() {
        binding.enterYourEmailAndPasswordTextView.animate().translationX(-screenWidth)
            .setDuration(500).start()
        
        binding.loginTextView.animate().translationX(-screenWidth).setDuration(500).start()
        
        binding.rememberMeLayout.animate().translationX(-screenWidth).setDuration(500).start()
        
        binding.registerButton.animate().translationX(screenWidth).setDuration(500).withEndAction {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }.start()
        
    }
    
    private fun handleLogin() {
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        
        if (username.isNotBlank() && password.isNotBlank()) {
            val user = viewModel.login(username, password)
            if (user != null) {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                saveLoginInfoToSharedPreferences(user.id)
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }
    
    private fun saveLoginInfoToSharedPreferences(userId: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("login_prefs", 0)
        val editor = sharedPreferences.edit()
        editor.putInt("User Id", userId)
        editor.apply()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
