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
    
    val _isBound = MutableStateFlow(false)
    var chatService: ChatServiceInterface? = null
    
    private val _conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationsFlow = _conversationsFlow.asStateFlow()
    
    private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    val messagesFlow = _messagesFlow.asStateFlow()
    
    private var _deleteMode = false
    val deleteMode: Boolean get() = _deleteMode
    
    private val _selectedMessages = mutableListOf<Message>()
    val selectedMessages: List<Message> get() = _selectedMessages
    
    private val conversationCallback = object : IConversationCallback.Stub() {
        override fun onConversationsUpdated(conversations: List<Conversation>) {
            fetchAllConversations()
        }
    }
    
    private val messageCallback = object : IMessageCallback.Stub() {
        override fun onMessageReceived(message: Message) {
            viewModelScope.launch {
                if (message.chatId == getCurrentConversationId()) {
                    _messagesFlow.value += message
                }
            }
        }
        
        override fun onMessageDeleted(messageId: Int) {
            viewModelScope.launch {
                _messagesFlow.value = _messagesFlow.value.filter { it.messageId != messageId }
                Log.d("ClientApp", "Message with ID $messageId has been deleted.")
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
            fetchAllConversations()
        }
        
        override fun onServiceDisconnected(name: ComponentName) {
            try {
                chatService?.unregisterConversationCallback(conversationCallback)
                chatService?.unregisterMessageCallback(messageCallback)
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
    
    fun fetchAllConversations() {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    _conversationsFlow.value =
                        chatService?.getAllConversationsForUser(getUserId()) ?: emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun fetchMessagesForConversation(conversationId: Int) {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    _messagesFlow.value =
                        chatService?.getMessagesForConversation(conversationId) ?: emptyList()
                    saveCurrentConversationId(conversationId)
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
                    saveUserId(user.id)
                    saveCurrentUserInformation(user)
                    fetchAllConversations()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    
    private fun saveCurrentUserInformation(user: User) {
        val sharedPreferences = getSharedPreferences()
        sharedPreferences.edit().apply {
            putString("User Name", user.name)
            putString("User Image", user.image)
            putString("User Username", user.username)
        }.apply()
    }
    
    fun getCurrentUser(): User {
        val sharedPreferences = getSharedPreferences()
        val name = sharedPreferences.getString("User Name", "") ?: ""
        val image = sharedPreferences.getString("User Image", "") ?: ""
        val username = sharedPreferences.getString("User Username", "") ?: ""
        return User(0, name, username, "", image)
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
                    handleRegistrationStatus(status)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    val message = Message(
                        messageId = 0,
                        chatId = getCurrentConversationId(),
                        senderId = getUserId(),
                        receiverId = getReceiverId(),
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
    
    fun saveCurrentConversation(conversation: Conversation) {
        fetchMessagesForConversation(conversation.conversationId)
        val receiver = getUserById(conversation.user1Id, conversation.user2Id)
        saveReceiverInfo(receiver)
    }
    
    fun getUserById(user1: Int, user2: Int): User {
        val queryUser = if (getUserId() == user1) user2 else user1
        return chatService?.getUserById(queryUser) ?: User(0, "", "", "", "")
    }
    
    private fun handleRegistrationStatus(status: Int?) {
        when (status) {
            1 -> {
                Log.d("ClientApp", "User added successfully")
                showToast("User added successfully")
            }
            
            2 -> {
                Log.d("ClientApp", "Failed to add user due to duplicate username")
                showToast("Failed to add user due to duplicate username")
            }
            
            else -> {
                Log.d("ClientApp", "Failed to add user")
                showToast("Failed to add user")
            }
        }
    }
    
    private fun saveUserId(userId: Int) {
        getSharedPreferences().edit().putInt("User Id", userId).apply()
    }
    
    fun getUserId(): Int {
        return getSharedPreferences().getInt("User Id", -1)
    }
    
    private fun saveCurrentConversationId(conversationId: Int) {
        getSharedPreferences().edit().putInt("Conversation Id", conversationId).apply()
    }
    
    fun getCurrentConversationId(): Int {
        return getSharedPreferences().getInt("Conversation Id", -1)
    }
    
    private fun saveReceiverInfo(receiver: User) {
        getSharedPreferences().edit().apply {
            putInt("receiverId", receiver.id)
            putString("Receiver Name", receiver.name)
            putString("Receiver Image", receiver.image)
        }.apply()
    }
    
    fun getReceiverId(): Int {
        return getSharedPreferences().getInt("receiverId", -1)
    }
    
    fun getReceiverName(): String {
        return getSharedPreferences().getString("Receiver Name", "") ?: ""
    }
    
    fun getReceiverImage(): String {
        return getSharedPreferences().getString("Receiver Image", "") ?: ""
    }
    
    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
    
    fun getSharedPreferences() =
        getApplication<Application>().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    
    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    chatService?.deleteConversation(conversation.conversationId, getUserId())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun getVisibleLastMessage(conversation: Conversation): Message {
        if (_isBound.value) {
            try {
                val userId = getUserId()
                val message = chatService?.getMessageById(conversation.conversationId, userId)
                Log.d(
                    "last message", "getVisibleLastMessage: ${message} , current user id: $userId"
                )
                return message ?: Message(0, 0, 0, 0, "", 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return Message(0, 0, 0, 0, "", 0)
    }
    
    fun toggleDeleteMode() {
        _deleteMode = !_deleteMode
    }
    
    fun addMessageToSelection(message: Message) {
        _selectedMessages.add(message)
    }
    
    fun removeMessageFromSelection(message: Message) {
        _selectedMessages.remove(message)
    }
    
    fun clearSelectedMessages() {
        _selectedMessages.clear()
    }
    
    fun deleteSelectedMessages() {
        viewModelScope.launch {
            if (_isBound.value) {
                try {
                    val selectId = _selectedMessages.map { it.messageId }.toIntArray()
                    chatService?.deleteMessages(selectId, getUserId())
                    _selectedMessages.clear()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun logout() {
        getSharedPreferences().edit().clear().apply()
    }
}

