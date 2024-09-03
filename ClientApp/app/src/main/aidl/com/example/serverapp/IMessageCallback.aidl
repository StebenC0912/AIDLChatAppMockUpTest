package com.example.serverapp;

import com.example.serverapp.models.Message;

interface IMessageCallback {
    void onMessageReceived(in Message message);
}