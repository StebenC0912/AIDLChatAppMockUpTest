package com.example.serverapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.data.database.ServerDatabase
import com.example.serverapp.data.repositories.ServerRepository
import com.example.serverapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatService : Service() {
    private lateinit var serverRepository: ServerRepository
    
    override fun onCreate() {
        super.onCreate()
        val userDao = ServerDatabase.getInstance(this).userDao()
        serverRepository = ServerRepository(userDao)
        CoroutineScope(Dispatchers.IO).launch {
            val users = serverRepository.getAllUsers().first().toMutableList()
            Log.d("test", "onCreate: $users")
        }
        
    }
    
    override fun onBind(p0: Intent?): IBinder {
        return MyBinder()
    }
    
    inner class MyBinder : ChatServiceInterface.Stub() {
        override fun getUsers(): MutableList<User> {
            return runBlocking {
                serverRepository.getAllUsers().first().toMutableList()
            }
        }
        
        override fun addUser(user: User): Int {
            return runBlocking {
                try {
                    serverRepository.addUser(user)
                    1
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
        }
        
        override fun login(username: String?, password: String?): User {
            return runBlocking {
                serverRepository.login(username!!, password!!)
            }
        }
        
    }
}