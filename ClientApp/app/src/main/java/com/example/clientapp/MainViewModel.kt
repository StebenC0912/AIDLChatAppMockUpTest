package com.example.clientapp

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    
    private val _isBound = MutableStateFlow(false)
    val isBound = _isBound.asStateFlow()
    
    private var chatService: ChatServiceInterface? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("ClientApp", "Service Connected")
            chatService = ChatServiceInterface.Stub.asInterface(service)
            _isBound.value = true
        }
        
        override fun onServiceDisconnected(name: ComponentName) {
            chatService = null
            _isBound.value = false
        }
    }
    
    fun bindService() {
        if (!_isBound.value) {
            val intent = Intent().apply {
                setClassName("com.example.serverapp", "com.example.serverapp.services.ChatService")
            }
            getApplication<Application>().bindService(
                intent, serviceConnection, Context.BIND_AUTO_CREATE
            )
        }
    }
    
    fun unbindService() {
        if (_isBound.value) {
            getApplication<Application>().unbindService(serviceConnection)
            _isBound.value = false
        }
    }
    
    fun login(username: String, password: String): User? {
        return if (_isBound.value) {
            try {
                chatService?.login(username, password)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    
    fun registerUser(name: String, username: String, password: String, profileImageUri: Uri) {
        if (_isBound.value) {
            try {
                val user = User(0, name, username, password, profileImageUri)
                val status = chatService?.addUser(user)
                when (status) {
                    1 -> {
                        Log.d("ClientApp", "User added successfully")
                        Toast.makeText(
                            getApplication(),
                            "User added successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    
                    2 -> {
                        Log.d("ClientApp", "Failed to add user")
                        Toast.makeText(
                            getApplication(),
                            "Failed to add user because of duplicate username",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    else -> {
                        Log.d("ClientApp", "Failed to add user")
                        Toast.makeText(getApplication(), "Failed to add user", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
    }
}
