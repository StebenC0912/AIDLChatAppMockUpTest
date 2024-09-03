package com.example.clientapp

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.clientapp.utils.ImageConverter
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.IConversationCallback
import com.example.serverapp.IMessageCallback
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.Message
import com.example.serverapp.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    
    private val _isBound = MutableStateFlow(false)
    private var chatService: ChatServiceInterface? = null
    
    private val _conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationsFlow = _conversationsFlow.asStateFlow()
    private val conversationCallback = object : IConversationCallback.Stub() {
        override fun onConversationsUpdated(conversations: List<Conversation>) {
            val userId = getUserIdFromSharedPreferences()
            if (userId != -1) {
                fetchAllConversations(userId)
            }
        }
    }
    
    private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    val messagesFlow = _messagesFlow.asStateFlow()
    
    private val messageCallback = object : IMessageCallback.Stub() {
        override fun onMessageReceived(message: Message) {
            viewModelScope.launch {
                val currentConversationId = getCurrentConversationIdFromPreferences()
                if (message.chatId == currentConversationId) {
                    val updatedMessages = _messagesFlow.value.toMutableList()
                    updatedMessages.add(message)
                    _messagesFlow.value = updatedMessages
                }
            }
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("ClientApp", "Service Connected")
            chatService = ChatServiceInterface.Stub.asInterface(service)
            _isBound.value = true
            chatService?.registerConversationCallback(conversationCallback)
            chatService?.registerMessageCallback(messageCallback)
            val userId = getUserIdFromSharedPreferences()
            if (userId != -1) {
                fetchAllConversations(userId)
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName) {
            try {
                chatService?.unregisterConversationCallback(conversationCallback)
                chatService?.unregisterMessageCallback(messageCallback)
                Log.d("Test", "onServiceDisconnected: Oke")
            } catch (e: DeadObjectException) {
                Log.w(
                    "ClientApp", "Service already disconnected. Unable to unregister callback.", e
                )
            } catch (e: Exception) {
                Log.e("ClientApp", "Error while unregistering callback", e)
            } finally {
                chatService = null
                _isBound.value = false
            }
        }
    }
    
    private fun fetchAllConversations(userId: Int) {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    val initialConversations = chatService?.getAllConversationsForUser(userId)
                    _conversationsFlow.value = initialConversations ?: emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
                chatService?.login(username, password)?.also { user ->
                    saveUserIdToSharedPreferences(user.id)
                    fetchAllConversations(user.id)
                }
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
            viewModelScope.launch {
                try {
                    val imageString = withContext(Dispatchers.IO) {
                        val bitmap =
                            Glide.with(getApplication<Application>().applicationContext).asBitmap()
                                .load(profileImageUri).submit().get()
                        ImageConverter().bitmapToString(bitmap)
                    }
                    val user = User(0, name = name, username, password, imageString)
                    val status = chatService?.addUser(user)
                    when (status) {
                        1 -> {
                            Log.d("ClientApp", "User added successfully")
                            Toast.makeText(
                                getApplication(), "User added successfully", Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                getApplication(), "Failed to add user", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
    }
    
    fun getUserById(user1: Int, user2: Int): User {
        val currentUser = getUserIdFromSharedPreferences()
        val queryUser = if (currentUser == user1) user2 else user1
        return chatService?.getUserById(queryUser) ?: User(0, "", "", "", "")
    }
    
    private fun saveUserIdToSharedPreferences(userId: Int) {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("User Id", userId).apply()
    }
    
    fun getUserIdFromSharedPreferences(): Int {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("User Id", -1)
    }
    
    private fun fetchMessagesForConversation(conversationId: Int) {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    val messages = chatService?.getMessagesForConversation(conversationId)
                    _messagesFlow.value = messages ?: emptyList()
                    saveCurrentConversationIdToPreferences(conversationId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun sendMessage(content: String) {
        val chatId = getCurrentConversationIdFromPreferences()
        val senderId = getUserIdFromSharedPreferences()
        val receiverId = getReceiverId()
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    val message = Message(
                        messageId = 0,
                        chatId = chatId,
                        senderId = senderId,
                        receiverId = receiverId,
                        content = content,
                        timestamp = System.currentTimeMillis()
                    )
                    chatService?.sendMessage(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun saveCurrentConversationIdToPreferences(conversationId: Int) {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("Conversation Id", conversationId).apply()
    }
    
    private fun getCurrentConversationIdFromPreferences(): Int {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("Conversation Id", -1)
    }
    
    fun saveCurrentConversation(conversation: Conversation) {
        val conversationId = conversation.conversationId
        fetchMessagesForConversation(conversationId)
        val receiver = getUserById(conversation.user1Id, conversation.user2Id)
        val receiverName = receiver.name
        val receiverImage = receiver.image
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("receiverId", receiver.id).apply()
        sharedPreferences.edit().putString("Receiver Name", receiverName).apply()
        sharedPreferences.edit().putString("Receiver Image", receiverImage).apply()
    }
    
    fun getReceiverId(): Int {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("receiverId", -1)
    }
    
    fun getReceiverName(): String {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("Receiver Name", "") ?: ""
    }
    
    fun getReceiverImage(): String {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("Receiver Image", "") ?: ""
    }
}
