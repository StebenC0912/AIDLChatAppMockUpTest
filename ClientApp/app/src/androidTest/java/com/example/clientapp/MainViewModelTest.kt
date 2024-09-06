package com.example.clientapp

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.example.clientapp.utils.ImageConverter
import com.example.serverapp.ChatServiceInterface
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.Message
import com.example.serverapp.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MainViewModelTest {
    
    @Mock
    lateinit var mockChatService: ChatServiceInterface
    
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: MainViewModel
    private val testScope = TestScope(testDispatcher)
    
    
    @Mock
    lateinit var mockImageConverter: ImageConverter
    
    @Mock
    lateinit var mockFutureTarget: FutureTarget<Bitmap>
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Get the real application context
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Clear SharedPreferences before each test to ensure no data is carried over
        val sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
        
        // Initialize ViewModel with the real context
        viewModel = MainViewModel(context as Application)
        
        Dispatchers.setMain(testDispatcher)
        
        // Set the chatService mock
        viewModel.chatService = mockChatService
    }
    
    
    @Test
    fun `test successful login`() = testScope.runTest {
        // Set the service as bound
        viewModel._isBound.value = true
        
        // Mock user login
        val mockUser =
            User(id = 1, name = "Test User", username = "test", password = "pass", image = "image")
        Mockito.`when`(mockChatService.login("test", "pass")).thenReturn(mockUser)
        
        // Perform login
        val result = viewModel.login("test", "pass")
        
        val sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        
        assertEquals(1, sharedPreferences.getInt("User Id", -1))
        assertEquals("Test User", sharedPreferences.getString("User Name", ""))
        assertEquals("image", sharedPreferences.getString("User Image", ""))
        assertEquals("test", sharedPreferences.getString("User Username", ""))
        
        // Verify that fetchAllConversations was called
        Mockito.verify(mockChatService).getAllConversationsForUser(mockUser.id)
        
        // Assert that the login returned the correct user
        assertEquals(mockUser, result)
    }
    
    @Test
    fun `test failed login returns null`() = testScope.runTest {
        // Mock failed login
        Mockito.`when`(mockChatService.login("wrong", "wrongpass")).thenReturn(null)
        
        // Perform login
        val result = viewModel.login("wrong", "wrongpass")
        
        // Assert that no user was returned
        assertNull(result)
        
        // Since login failed, no SharedPreferences should be updated,
        // so you can verify that no conversations were fetched
        Mockito.verify(mockChatService, Mockito.never())
            .getAllConversationsForUser(Mockito.anyInt())
    }
    
    
    @Test
    fun `test sendMessage`() = testScope.runTest {
        // Set the service as bound
        viewModel._isBound.value = true
        
        // Mock user ID and current conversation ID
        Mockito.`when`(viewModel.getUserId()).thenReturn(1)
        Mockito.`when`(viewModel.getCurrentConversationId()).thenReturn(100)
        Mockito.`when`(viewModel.getReceiverId()).thenReturn(2)
        
        // Send message
        viewModel.sendMessage("Hello World")
        
        // Verify that chatService's sendMessage was called with the correct parameters
        val messageCaptor = ArgumentCaptor.forClass(Message::class.java)
        Mockito.verify(mockChatService).sendMessage(messageCaptor.capture())
        
        val sentMessage = messageCaptor.value
        assertEquals(1, sentMessage.senderId)
        assertEquals(100, sentMessage.chatId)
        assertEquals("Hello World", sentMessage.content)
    }
    
    @Test
    fun `test register user`() = testScope.runTest {
        // Set service as bound
        viewModel._isBound.value = true
        
        // Create a real Bitmap for testing
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        // Mock Uri and image processing
        val mockUri = Mockito.mock(Uri::class.java)
        val imageString = "image_base64_string"
        Mockito.`when`(mockImageConverter.bitmapToString(bitmap)).thenReturn(imageString)
        Mockito.`when`(
            Glide.with(Mockito.any(Context::class.java)).asBitmap().load(mockUri).submit()
        ).thenReturn(mockFutureTarget)
        Mockito.`when`(mockFutureTarget.get()).thenReturn(bitmap)
        
        // Mock ChatService response
        Mockito.`when`(mockChatService.addUser(Mockito.any())).thenReturn(1)
        
        // Call registerUser
        viewModel.registerUser("John Doe", "johndoe", "password", mockUri)
        
        // Verify the user creation
        val userCaptor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(mockChatService).addUser(userCaptor.capture())
        
        val registeredUser = userCaptor.value
        assertEquals("John Doe", registeredUser.name)
        assertEquals("johndoe", registeredUser.username)
        assertEquals("password", registeredUser.password)
        assertEquals(imageString, registeredUser.image)
    }
    
    @Test
    fun `test delete conversation`() = testScope.runTest {
        // Set the service as bound
        viewModel._isBound.value = true
        
        // Create a mock conversation
        val conversation = Conversation(conversationId = 101, user1Id = 1, user2Id = 2)
        
        // Mock getUserId() to return 1
        Mockito.`when`(viewModel.getUserId()).thenReturn(1)
        
        // Call deleteConversation
        viewModel.deleteConversation(conversation)
        
        // Verify that deleteConversation was called with the correct parameters
        Mockito.verify(mockChatService).deleteConversation(101, 1)
    }
    
    @Test
    fun `test logout clears shared preferences`() = testScope.runTest {
        // Perform logout
        viewModel.logout()
        
        // Verify that SharedPreferences were cleared
        val sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        
        // Check that SharedPreferences are empty after logout
        assertEquals(
            -1,
            sharedPreferences.getInt("User Id", -1)
        ) // Default value -1 means it's cleared
        assertEquals("", sharedPreferences.getString("User Name", ""))
        assertEquals("", sharedPreferences.getString("User Image", ""))
        assertEquals("", sharedPreferences.getString("User Username", ""))
    }
    
    
    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the dispatcher after the test
    }
}

