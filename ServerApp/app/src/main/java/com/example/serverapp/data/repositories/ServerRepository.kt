package com.example.serverapp.data.repositories

import com.example.serverapp.data.dao.ConversationDao
import com.example.serverapp.data.dao.MessageDao
import com.example.serverapp.data.dao.UserDao
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.Message
import com.example.serverapp.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ServerRepository(
    private val userDao: UserDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) {
    fun getAllUsers() = userDao.getAllUsers()
    
    fun login(username: String, password: String) = userDao.login(username, password)
    suspend fun addUser(user: User): Int {
        val newUserId = userDao.addUser(user).toInt()
        return newUserId
    }
    
    suspend fun createConversationsForNewUser(newUserId: Int) {
        withContext(Dispatchers.IO) {
            val allUsers = userDao.getAllUsers().first()
            allUsers.forEach { existingUser ->
                if (existingUser.id != newUserId) {
                    val conversation = Conversation(
                        user1Id = newUserId, user2Id = existingUser.id
                    )
                    conversationDao.addConversation(conversation)
                }
            }
        }
    }
    
    suspend fun getUserByUsername(username: String): Boolean {
        return userDao.getUserByUsername(username)
    }
    
    fun getAllConversationsForUser(userId: Int): Flow<List<Conversation>> {
        return conversationDao.getAllConversationsForUser(userId)
    }
    
    fun getMessagesForConversation(conversationId: Int): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId)
    }
    
    fun getUserById(userId: Int): User {
        return userDao.getUserById(userId)
    }
    
    fun addMessage(message: Message) {
        messageDao.addNewMessage(message)
    }
    
    fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation)
    }
    
    fun updateMessage(copy: Message) {
        messageDao.updateMessage(copy)
    }
    
    fun deleteMessage(messageId: Int) {
        messageDao.deleteMessage(messageId)
    }
}

