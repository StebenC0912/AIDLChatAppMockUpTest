package com.example.serverapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.serverapp.data.dao.UserDao
import com.example.serverapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class], version = 1)
abstract class ServerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    
    companion object {
        private const val DATABASE_NAME = "server_database"
        
        @Volatile
        private var instance: ServerDatabase? = null
        
        fun getInstance(context: Context): ServerDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ServerDatabase::class.java,
                        DATABASE_NAME
                    )
                        .addCallback(roomDatabaseCallback)
                        .build()
                }
            }
            return instance!!
        }
        
        // Define a RoomDatabase.Callback to populate the database with sample data on creation
        private val roomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                instance?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.userDao())
                    }
                }
            }
        }
        
        // Insert sample data into the database
        suspend fun populateDatabase(userDao: UserDao) {
            // Clear the existing data (if any)
            userDao.deleteAll()
            
            // Create sample users
            val user1 = User(0, "John Doe", "johndoe", "password123", "image_url_1")
            val user2 = User(0, "Jane Smith", "janesmith", "password456", "image_url_2")
            val user3 = User(0, "Alice Johnson", "alicej", "password789", "image_url_3")
            
            // Insert sample users into the database
            userDao.addUser(user1)
            userDao.addUser(user2)
            userDao.addUser(user3)
        }
    }
}
