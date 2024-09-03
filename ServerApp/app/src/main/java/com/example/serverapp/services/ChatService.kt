package com.example.serverapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.IConversationCallback
import com.example.serverapp.IMessageCallback
import com.example.serverapp.data.database.ServerDatabase
import com.example.serverapp.data.repositories.ServerRepository
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.Message
import com.example.serverapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatService : Service() {
    private lateinit var serverRepository: ServerRepository
    private val callbacks = RemoteCallbackList<IConversationCallback>()
    private val messageCallbacks = RemoteCallbackList<IMessageCallback>()
    private val conversationUpdatesFlow = MutableSharedFlow<List<Conversation>>(replay = 1)
    
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
        observeConversationUpdates()
    }
    
    private fun observeConversationUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            conversationUpdatesFlow.collect { conversations ->
                notifyConversationUpdate(conversations)
            }
        }
    }
    
    private fun emitConversationUpdate(conversations: List<Conversation>) {
        CoroutineScope(Dispatchers.IO).launch {
            conversationUpdatesFlow.emit(conversations)
        }
    }
    
    private fun notifyConversationUpdate(conversations: List<Conversation>) {
        val count = callbacks.beginBroadcast()
        Log.d("ChatService", "Notifying $count clients of new conversation updates.")
        for (i in 0 until count) {
            try {
                Log.d("ChatService", "Sending conversation update to client $i: $conversations")
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
                    
                    val updatedConversations =
                        serverRepository.getAllConversationsForUser(newUserId).first()
                    emitConversationUpdate(updatedConversations)
                    
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
        
        override fun sendMessage(message: Message?) {
            runBlocking {
                if (message != null) {
                    // Add the new message to the database
                    serverRepository.addMessage(message)
                    
                    // Notify all registered callbacks that a new message was received
                    val count = messageCallbacks.beginBroadcast()
                    for (i in 0 until count) {
                        try {
                            messageCallbacks.getBroadcastItem(i).onMessageReceived(message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    messageCallbacks.finishBroadcast()
                    
                    // Retrieve the list of conversations for the sender
                    val conversations =
                        serverRepository.getAllConversationsForUser(message.senderId).firstOrNull()
                    
                    // Find the conversation associated with the message's chatId (assuming chatId is equivalent to conversationId)
                    val currentConversation =
                        conversations?.firstOrNull { it.conversationId == message.chatId }
                    
                    if (currentConversation != null) {
                        // Update the conversation with the latest message content and timestamp
                        val updatedConversation = currentConversation.copy(
                            lastMessageContent = message.content,
                            lastMessageTimestamp = message.timestamp
                        )
                        serverRepository.updateConversation(updatedConversation)
                        
                        // Emit the updated conversation list to notify all clients
                        val updatedConversations =
                            serverRepository.getAllConversationsForUser(message.senderId).first()
                        emitConversationUpdate(updatedConversations)
                    } else {
                        Log.e("ChatService", "Conversation with id ${message.chatId} not found.")
                    }
                }
            }
        }
        
        override fun registerMessageCallback(callback: IMessageCallback?) {
            if (callback != null) {
                messageCallbacks.register(callback)
            }
        }
        
        override fun unregisterMessageCallback(callback: IMessageCallback?) {
            if (callback != null) {
                messageCallbacks.unregister(callback)
            }
        }
        
        override fun getMessagesForConversation(conversationId: Int): MutableList<Message> {
            return runBlocking {
                serverRepository.getMessagesForConversation(conversationId).first().toMutableList()
            }
        }
    }
    
}