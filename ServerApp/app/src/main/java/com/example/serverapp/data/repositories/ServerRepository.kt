package com.example.serverapp.data.repositories

import com.example.serverapp.data.dao.UserDao
import com.example.serverapp.models.User

class ServerRepository(
    private val userDao: UserDao,
) {
    fun getAllUsers() = userDao.getAllUsers()
    
    fun login(username: String, password: String) = userDao.login(username, password)
    fun addUser(user: User) {
        userDao.addUser(user)
    }
    
    fun getUserByUsername(username: String): Boolean {
        return userDao.getUserByUsername(username)
    }
}