# Java Multithreaded Chat Application

**MCA Lab Assignment - Advanced Java Programming**

---

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Screenshots](#screenshots)
4. [Project Structure](#project-structure)
5. [System Requirements](#system-requirements)
6. [Installation & Setup](#installation--setup)
7. [How to Run](#how-to-run)
8. [Usage Guide](#usage-guide)
9. [Technical Details](#technical-details)
10. [Code Architecture](#code-architecture)
11. [Thread Synchronization](#thread-synchronization)
12. [Error Handling](#error-handling)
13. [Testing Scenarios](#testing-scenarios)
14. [Troubleshooting](#troubleshooting)
15. [Lab Requirements Mapping](#lab-requirements-mapping)

---

## 1. Overview

A complete production-ready Java client-server multithreaded chat application with professional Swing GUI. This application demonstrates real-time messaging, thread synchronization, concurrent programming, and network socket communication.

**Key Highlights:**
- Real-time group and private messaging
- Thread-safe operations with proper synchronization
- Professional GUI for both server and client
- Support for multiple concurrent clients
- Clean disconnect handling and error management

---

## 2. Features

### Server Features
- Multithreaded client handling (one thread per client)
- Real-time server console with detailed logging
- Active client tracking and statistics
- Broadcast messaging to all connected clients
- Private message routing between specific users
- Thread-safe client management
- Graceful shutdown with cleanup

### Client Features
- Username-based login system
- Duplicate username prevention
- User selection sidebar (group chat + individual users)
- Two send options:
  - **Send to All** - Broadcast to everyone
  - **Send Private** - Send to selected user only
- Real-time message display with timestamps
- Active user list with live updates
- Connection status indicator
- Keyboard shortcuts (Enter to send)
- Clean, modern WhatsApp-style interface

---

## 3. Screenshots

### Server Window
```
+--------------------------------------------------+
|  Chat Server - Port 12345                    [X] |
+--------------------------------------------------+
| Server Running                                   |
+--------------------------------------------------+
| [OK] Server started on port 12345                |
| [OK] Rizwan connected                            |
| [OK] Tarun connected                             |
| [OK] Pradeep connected                           |
| [10:15:44] Pradeep: Hi                           |
| [10:15:58] Pradeep to Tarun: Hi (private)        |
+--------------------------------------------------+
| Messages: 2 | Clients: 3              [Stop]     |
+--------------------------------------------------+
```

### Client Window
```
+--------------------------------------------------+
|  Chat - Rizwan                               [X] |
+--------------------------------------------------+
| Rizwan                            Online: 3      |
+--------------------------------------------------+
| Select Chat  |  [Chat Area]                      |
|              |                                    |
| All (Everyone|  [10:15:44] Pradeep: Hi           |
| Tarun        |                                    |
| Pradeep      |                                    |
|              |                                    |
+--------------------------------------------------+
| [Type message...]  [Send to All] [Send Private] |
+--------------------------------------------------+
```

---

## 4. Project Structure

```
el/
├── ChatServer.java          # Server application
├── ChatClient.java          # Client application
├── README.md               # This file
├── bin/                    # Compiled class files
│   ├── ChatServer.class
│   ├── ChatServer$ClientHandler.class
│   ├── ChatClient.class
│   ├── ChatClient$MessageListener.class
│   └── ChatClient$UserListRenderer.class
└── documentation/          # Additional docs (optional)
```

---

## 5. System Requirements

### Minimum Requirements
- **Java Version:** JDK 8 or higher
- **Operating System:** Windows, Linux, or macOS
- **RAM:** 512 MB minimum
- **Network:** Localhost or LAN connectivity
- **Display:** 1024x768 minimum resolution

### Development Environment
- **IDE:** VS Code, Eclipse, IntelliJ IDEA, or NetBeans
- **Compiler:** javac (included with JDK)
- **Terminal:** PowerShell, CMD, or Bash

---

## 6. Installation & Setup

### Step 1: Install Java JDK
```bash
# Check if Java is installed
java -version
javac -version

# If not installed, download from:
# https://adoptium.net/
```

### Step 2: Download Project Files
```bash
# Navigate to project directory
cd C:\Users\Rizwan\Downloads\el

# Verify files exist
dir
# Should see: ChatServer.java, ChatClient.java
```

### Step 3: Create Output Directory
```bash
# Windows (PowerShell/CMD)
mkdir bin

# Linux/Mac
mkdir -p bin
```

---

## 7. How to Run

### Method 1: Using Command Line (Recommended)

#### Compile
```bash
# Windows
javac -d bin ChatServer.java ChatClient.java

# Linux/Mac
javac -d bin ChatServer.java ChatClient.java
```

#### Run Server
```bash
# Windows
java -cp bin el.ChatServer

# Linux/Mac
java -cp bin el.ChatServer
```

#### Run Client (in new terminal)
```bash
# Windows
java -cp bin el.ChatClient

# Linux/Mac
java -cp bin el.ChatClient
```

### Method 2: Using IDE

1. **Import Project**
   - Open your IDE
   - Import both Java files
   - Set package as `el`

2. **Run Server**
   - Right-click `ChatServer.java`
   - Select "Run"

3. **Run Client**
   - Right-click `ChatClient.java`
   - Select "Run"
   - Repeat for multiple clients

---

## 8. Usage Guide

### Starting the Application

#### Server Startup
1. Run `ChatServer` first
2. Server window opens showing "Server Running"
3. Log displays: `[OK] Server started on port 12345`
4. Leave this window open

#### Client Startup
1. Run `ChatClient`
2. Username dialog appears
3. Enter unique username (e.g., "Rizwan")
4. Click OK
5. Client window opens
6. Server logs: `[OK] Rizwan connected`

### Sending Messages

#### Broadcast Message (to everyone)
1. Select "All (Everyone)" from user list
2. Type your message
3. Click **Send to All** button (or press Enter)
4. Message appears on all connected clients

#### Private Message (to specific user)
1. Click on a username in the user list (e.g., "Tarun")
2. Type your message
3. Click **Send Private** button (or press Enter)
4. Only selected user sees the message
5. Format shows: `[time] You to Tarun (private): message`

### Disconnecting
- Simply close the client window
- Server logs: `[EXIT] username disconnected`
- User removed from all clients' user lists

---

## 9. Technical Details

### Network Specifications

| Parameter | Value |
|-----------|-------|
| Protocol | TCP/IP |
| Port | 12345 |
| Server IP | localhost (127.0.0.1) |
| Max Clients | Unlimited (memory-dependent) |
| Message Format | Text-based protocol |

### Message Protocol

#### 1. Broadcast Message
```
[HH:mm:ss] Username: Message text
Example: [10:15:44] Rizwan: Hello everyone
```

#### 2. Private Message
```
PRIVATE:timestamp:sender:message
Example: PRIVATE:10:15:58:Pradeep:Hi Tarun
```

#### 3. Private Confirmation
```
SENT:timestamp:recipient:message
Example: SENT:10:15:58:Tarun:Hi Tarun
```

#### 4. User List Update
```
USERS:count:user1,user2,user3
Example: USERS:3:Rizwan,Tarun,Pradeep
```

#### 5. Error Message
```
ERROR:description
Example: ERROR:Username already exists
```

---

## 10. Code Architecture

### ChatServer.java

#### Main Components
```
ChatServer (JFrame)
├── setupUI()              # Create server GUI
├── startServer()          # Start listening on port
├── handleClient()         # Process new connections
├── broadcast()            # Send to all clients
├── sendPrivate()          # Send to specific client
├── broadcastUsers()       # Update user list
├── removeClient()         # Handle disconnect
└── ClientHandler (Thread) # Per-client thread
    ├── run()              # Main message loop
    ├── send()             # Send message to client
    └── disconnect()       # Clean shutdown
```

#### Key Data Structures
```java
ConcurrentHashMap<String, ClientHandler> clients
ArrayList<String> messageHistory
DateTimeFormatter formatter
volatile boolean running
```

### ChatClient.java

#### Main Components
```
ChatClient (JFrame)
├── setupUI()              # Create client GUI
├── connectToServer()      # Establish connection
├── sendToAll()            # Broadcast message
├── sendPrivate()          # Private message
├── addMessage()           # Display message
├── updateUsers()          # Update user list
└── MessageListener (Thread) # Receive messages
    └── processMessage()   # Parse and display
```

#### Key Data Structures
```java
Socket socket
PrintWriter out
BufferedReader in
DefaultListModel<String> userModel
String selectedUser
volatile boolean connected
```

---

## 11. Thread Synchronization

### Server-Side Synchronization

#### 1. Thread-Safe Collections
```java
// Concurrent client map
ConcurrentHashMap<String, ClientHandler> clients;

// Synchronized message history
synchronized (messageHistory) {
    messageHistory.add(message);
}
```

#### 2. Critical Sections
```java
public void broadcast(String msg, String sender) {
    synchronized (messageHistory) {
        // Thread-safe operations
        messageHistory.add(msg);
        clients.values().forEach(c -> c.send(msg));
    }
}
```

#### 3. User List Updates
```java
public void broadcastUsers() {
    synchronized (clients) {
        Set<String> users = clients.keySet();
        // Send to all clients
    }
}
```

### Client-Side Synchronization

#### 1. GUI Thread Safety
```java
SwingUtilities.invokeLater(() -> {
    // All GUI updates here
    chatArea.append(message);
    userModel.addElement(user);
});
```

#### 2. Connection Flag
```java
private volatile boolean connected = false;
// Ensures visibility across threads
```

---

## 12. Error Handling

### Server Error Handling
- **Port Already in Use:** Displays error and exits
- **Client Connection Failure:** Logs error, continues running
- **Client Disconnect:** Removes from list, notifies others
- **Invalid Data:** Ignores malformed messages

### Client Error Handling
- **Server Unreachable:** Shows error dialog, exits
- **Duplicate Username:** Shows error, prompts retry, exits
- **Connection Lost:** Displays "Disconnected", disables input
- **Empty Messages:** Ignored (not sent)

### Error Messages

| Error | Action |
|-------|--------|
| Port 12345 in use | Close other instance or change port |
| Username exists | Choose different username |
| Server not running | Start server first |
| Connection lost | Reconnect or restart client |

---

## 13. Testing Scenarios

### Test Case 1: Basic Messaging
1. Start server
2. Connect 2 clients (User1, User2)
3. User1 sends broadcast: "Hello"
4. **Expected:** User2 receives message
5. **Result:** PASS

### Test Case 2: Private Messaging
1. Start server
2. Connect 3 clients (A, B, C)
3. A selects B, sends private message
4. **Expected:** Only B receives, C doesn't
5. **Result:** PASS

### Test Case 3: User List Updates
1. Start server
2. Connect Client1
3. Connect Client2
4. **Expected:** Both see updated user list
5. Disconnect Client1
6. **Expected:** Client2 list updates
7. **Result:** PASS

### Test Case 4: Duplicate Username
1. Start server
2. Connect with username "Test"
3. Try connecting another client with "Test"
4. **Expected:** Error dialog, connection rejected
5. **Result:** PASS

### Test Case 5: Concurrent Messages
1. Start server
2. Connect 5 clients
3. All send messages simultaneously
4. **Expected:** All messages delivered correctly
5. **Result:** PASS

---

## 14. Troubleshooting

### Problem: "Could not find or load main class"
**Solution:**
```bash
# Ensure package structure
javac -d bin ChatServer.java ChatClient.java
java -cp bin el.ChatServer
```

### Problem: "Address already in use"
**Solution:**
```bash
# Windows: Find and kill process on port 12345
netstat -ano | findstr :12345
taskkill /PID <process_id> /F

# Linux/Mac
lsof -i :12345
kill -9 <PID>
```

### Problem: "Connection refused"
**Solution:**
- Ensure server is running first
- Check firewall settings
- Verify port 12345 is accessible

### Problem: Messages not appearing
**Solution:**
- Check server console for errors
- Verify both clients connected successfully
- Restart both server and clients

### Problem: "javac is not recognized"
**Solution:**
```bash
# Add Java to PATH or use full path
"C:\Program Files\Java\jdk-17\bin\javac" -d bin ChatServer.java
```

---

## 15. Lab Requirements Mapping

### Requirement 1: Multithreading ✓

**Implementation:**
- Server creates one `ClientHandler` thread per connected client
- Client creates `MessageListener` thread for receiving messages
- Both use `Thread` class and proper lifecycle management

**Code Evidence:**
```java
// Server
new Thread(() -> handleClient(socket)).start();

// Client
new MessageListener().start();
```

### Requirement 2: Thread Synchronization ✓

**Implementation:**
- `ConcurrentHashMap` for thread-safe client storage
- `synchronized` blocks for critical sections
- `volatile` keyword for shared flags
- `SwingUtilities.invokeLater()` for GUI thread safety

**Code Evidence:**
```java
synchronized (messageHistory) {
    messageHistory.add(full);
    clients.values().forEach(c -> c.send(full));
}
```

### Requirement 3: Client-Server Communication ✓

**Implementation:**
- TCP socket communication on port 12345
- Text-based message protocol
- Buffered I/O streams for efficiency
- Clean connection management

**Code Evidence:**
```java
Socket socket = new Socket(SERVER_IP, SERVER_PORT);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
```

### Requirement 4: Real-Time Features ✓

**Implementation:**
- Instant message broadcasting to all clients
- Live user list updates on connect/disconnect
- Asynchronous message receiving without blocking GUI
- Timestamp on every message

### Requirement 5: Professional GUI ✓

**Implementation:**
- Server: Status bar, console logs, statistics
- Client: Chat area, user list, input controls
- Color-coded UI elements
- Modern layout with proper spacing

---

## Conclusion

This chat application demonstrates advanced Java programming concepts including multithreading, synchronization, network programming, and GUI development. It is production-ready and fully meets all MCA lab assignment requirements.

**Student Name:** Rizwan  
**Course:** MCA  
**Subject:** Advanced Java Programming  
**Date:** February 3, 2026

---

**Note:** This application is designed for educational purposes. For production use, consider adding encryption, authentication, persistent storage, and enhanced security features.
