package com.example.serverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.serverapp.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM user WHERE username = :username AND password = :password")
    fun login(username: String, password: String): User
    
    @Insert
    suspend fun addUser(user: User): Long
    
    @Query("DELETE FROM user")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) > 0 FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): Boolean
    
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Int): User
}