package com.example.serverapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.IConversationCallback
import com.example.serverapp.data.database.ServerDatabase
import com.example.serverapp.data.repositories.ServerRepository
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatService : Service() {
    private lateinit var serverRepository: ServerRepository
    private val callbacks = RemoteCallbackList<IConversationCallback>()
    
    override fun onCreate() {
        super.onCreate()
        val userDao = ServerDatabase.getInstance(this).userDao()
        val messageDao = ServerDatabase.getInstance(this).messageDao()
        val conversationDao = ServerDatabase.getInstance(this).conversationDao()
        
        serverRepository = ServerRepository(userDao, conversationDao, messageDao)
        CoroutineScope(Dispatchers.IO).launch {
            val users = serverRepository.getAllUsers().first().toMutableList()
            Log.d("test", "onCreate: $users")
        }
        observeConversationsForAllUsers()
    }
    
    private fun observeConversationsForAllUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = serverRepository.getAllUsers().first()
            users.forEach { user ->
                observeConversationsForUser(user.id)
            }
        }
    }
    
    private fun observeConversationsForUser(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            serverRepository.getAllConversationsForUser(userId).collect { conversations ->
                notifyConversationUpdate(conversations)
            }
        }
    }
    
    private fun notifyConversationUpdate(conversations: List<Conversation>) {
        val count = callbacks.beginBroadcast()
        for (i in 0 until count) {
            try {
                callbacks.getBroadcastItem(i).onConversationsUpdated(conversations)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        callbacks.finishBroadcast()
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
                    val usernameExists = serverRepository.getUserByUsername(user.username)
                    if (usernameExists) {
                        return@runBlocking 2
                    }
                    val newUserId = serverRepository.addUser(user)
                    serverRepository.createConversationsForNewUser(newUserId)
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
        
        override fun getAllConversationsForUser(userId: Int): MutableList<Conversation> {
            return runBlocking {
                serverRepository.getAllConversationsForUser(userId).first().toMutableList()
            }
        }
        
        override fun registerConversationCallback(callback: IConversationCallback?) {
            if (callback != null) {
                callbacks.register(callback)
            }
        }
        
        override fun unregisterConversationCallback(callback: IConversationCallback?) {
            if (callback != null) {
                callbacks.unregister(callback)
            }
        }
        
        override fun getUserById(userId: Int): User {
            return serverRepository.getUserById(userId)
            
        }
    }
    
}