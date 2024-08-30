// ChatServiceInterface.aidl
package com.example.serverapp;

import com.example.serverapp.models.User;
interface ChatServiceInterface {
    List<User> getUsers();
    int addUser(in User user);
}