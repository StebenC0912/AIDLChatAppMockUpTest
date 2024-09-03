// ChatServiceInterface.aidl
package com.example.serverapp;

import com.example.serverapp.models.User;
import com.example.serverapp.models.Conversation;
import com.example.serverapp.models.Message;
import com.example.serverapp.IConversationCallback;
import com.example.serverapp.IMessageCallback;
interface ChatServiceInterface {
    List<User> getUsers();
    int addUser(in User user);
    User login(String username, String password);
    List<Conversation> getAllConversationsForUser(int userId);
    void registerConversationCallback(IConversationCallback callback);
    void unregisterConversationCallback(IConversationCallback callback);
    User getUserById(int userId);

     void sendMessage(in Message message);
     void registerMessageCallback(IMessageCallback callback);
     void unregisterMessageCallback(IMessageCallback callback);

     List<Message> getMessagesForConversation(int conversationId);

    void deleteConversation(int conversationId, int userId);
}