package com.example.serverapp;

import com.example.serverapp.models.Conversation;

interface IConversationCallback {
    void onConversationsUpdated(in List<Conversation> conversations);
}
