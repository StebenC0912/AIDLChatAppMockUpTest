# AIDL Chat App Mockup

This project is a mock-up of a client-server chat application on Android. It demonstrates how to perform Inter-Process Communication (IPC) using **Android Interface Definition Language (AIDL)**.

The project is split into two distinct Android applications that run in separate processes:
- **ClientApp** (The Frontend UI)
- **ServerApp** (The Backend Service & Database)

## Features

- **Real-time Chatting**: Send and receive messages in real-time between clients.
- **Inter-Process Communication (IPC)**: Powered entirely by Android AIDL to handle the communication between the UI and the background database service.
- **Local Persistence**: Messages and conversations are stored locally in a Room database managed by the `ServerApp`.
- **Soft Deletion & Hard Deletion**: 
  - Messages can be deleted by either the sender or receiver (soft delete).
  - If both users delete a message, it is permanently removed from the database (hard delete).
- **Reactive UI**: Built with Kotlin Coroutines and `StateFlow` to provide a reactive, lifecycle-aware user interface.

## Tech Stack

- **Kotlin**
- **Android Interface Definition Language (AIDL)**
- **AndroidX & Material Design**
- **Room Database** for local data persistence (`ServerApp`)
- **Coroutines & Kotlin Flow** for asynchronous programming and reactive streams.
- **Dagger Hilt** for Dependency Injection.
- **Glide** for image loading/processing.

## Project Structure

### 1. ClientApp
The user-facing application where the interactions happen. 
- Contains the UI Fragments (`ChatsFragment`, `ConversationFragment`, etc.).
- Relies on a `MainViewModel` that binds to the `ChatService` using a `ServiceConnection`.
- Does **not** have direct access to the database; every action is delegated to the `ServerApp` via AIDL methods like `chatService?.sendMessage(...)`.
- Receives real-time updates through `IMessageCallback` and `IConversationCallback`.

### 2. ServerApp
The headless backend part of the project.
- Hosts a background Android Bound Service (`ChatService`) that implements the AIDL contract (`ChatServiceInterface.Stub`).
- Manages the single source of truth—a local SQLite database via Room (`ServerDatabase`).
- Uses `RemoteCallbackList` to notify all connected clients simultaneously when database changes occur (like an incoming message or a deleted chat).

## How it Works

1. **Binding**: The `ClientApp` binds to the `ChatService` from the `ServerApp` using a `ServiceConnection`.
2. **AIDL Communication**: The `ClientApp` invokes remote AIDL methods defined in `ChatServiceInterface.aidl`. For example, calling `sendMessage(message)` sends the message bundle to the `ServerApp`.
3. **Database Operations**: The `ServerApp` processes the request natively on its background process using `ServerRepository` and saves it to Room.
4. **Broadcasting Updates**: Upon saving, the `ServerApp` broadcasts the new data state to any connected clients using `RemoteCallbackList`. The `ClientApp` receives the callback and updates its local `StateFlow`, triggering a UI refresh.

## Installation & Setup

1. Open the project in **Android Studio**.
2. This project consists of two separate modules (`ClientApp` and `ServerApp`). 
3. You must install **both** apps on your Android emulator or physical device for the IPC communication to work properly.
4. Run the **ServerApp** first so the `ChatService` is available to accept connections.
5. Run the **ClientApp** and interact with the UI. You can run multiple instances of the ClientApp on different emulators to simulate chatting between different users!
