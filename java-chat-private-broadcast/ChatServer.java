package el;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer extends JFrame {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ArrayList<String> messageHistory = new ArrayList<>();
    private JTextArea console;
    private JLabel statusLabel, statsLabel;
    private int totalMessages = 0;
    private volatile boolean running = true;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ChatServer() {
        setupUI();
        startServer();
    }

    private void setupUI() {
        setTitle("Chat Server - Port " + PORT);
        setSize(700, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel header = new JPanel();
        header.setBackground(new java.awt.Color(33, 150, 243));
        statusLabel = new JLabel("Server Running");
        statusLabel.setForeground(java.awt.Color.WHITE);
        statusLabel.setFont(new java.awt.Font("Arial", 1, 14));
        header.add(statusLabel);

        // Console
        console = new JTextArea();
        console.setEditable(false);
        console.setFont(new java.awt.Font("Consolas", 0, 12));
        console.setBackground(new java.awt.Color(245, 245, 245));

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(new java.awt.Color(33, 150, 243));
        statsLabel = new JLabel("Messages: 0 | Clients: 0");
        statsLabel.setForeground(java.awt.Color.WHITE);
        statsLabel.setFont(new java.awt.Font("Arial", 0, 12));
        JButton stopBtn = new JButton("Stop");
        stopBtn.addActionListener(e -> System.exit(0));
        footer.add(statsLabel);
        footer.add(stopBtn);

        add(header, "North");
        add(new JScrollPane(console), "Center");
        add(footer, "South");
        setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                log("[OK] Server started on port " + PORT);
                while (running) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleClient(socket)).start();
                }
            } catch (IOException e) {
                if (running) log("[ERROR] " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                socket.close();
                return;
            }

            username = username.trim();

            // Check if username exists
            if (clients.containsKey(username)) {
                out.println("ERROR:Username already exists");
                socket.close();
                return;
            }

            ClientHandler handler = new ClientHandler(socket, username, in, out);
            clients.put(username, handler);
            log("[OK] " + username + " connected");
            broadcastUsers();
            updateStats();
            handler.run();
        } catch (IOException e) {
            log("[ERROR] Connection error");
        }
    }

    // Broadcast to all users
    public void broadcast(String msg, String sender) {
        synchronized (messageHistory) {
            String time = LocalTime.now().format(formatter);
            String full = "[" + time + "] " + sender + ": " + msg;
            log(full);
            messageHistory.add(full);
            totalMessages++;
            clients.values().forEach(c -> c.send(full));
            updateStats();
        }
    }

    // Send private message to specific user
    public void sendPrivate(String msg, String sender, String recipient) {
        synchronized (messageHistory) {
            String time = LocalTime.now().format(formatter);
            String full = "[" + time + "] " + sender + " to " + recipient + ": " + msg;
            log(full + " (private)");
            totalMessages++;

            // Send to recipient
            ClientHandler recipientHandler = clients.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.send("PRIVATE:" + time + ":" + sender + ":" + msg);
            }

            // Send confirmation to sender
            ClientHandler senderHandler = clients.get(sender);
            if (senderHandler != null) {
                senderHandler.send("SENT:" + time + ":" + recipient + ":" + msg);
            }

            updateStats();
        }
    }

    public void broadcastUsers() {
        synchronized (clients) {
            Set<String> users = clients.keySet();
            String userList = String.join(",", users);
            clients.values().forEach(c -> c.sendUsers(new ArrayList<>(users)));
            updateStats();
        }
    }

    public void removeClient(String username) {
        if (clients.remove(username) != null) {
            log("[EXIT] " + username + " disconnected");
            broadcastUsers();
            updateStats();
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            console.append(msg + "\n");
            console.setCaretPosition(console.getDocument().getLength());
        });
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> 
            statsLabel.setText("Messages: " + totalMessages + " | Clients: " + clients.size())
        );
    }

    class ClientHandler extends Thread {
        private Socket socket;
        private String username;
        private BufferedReader in;
        private PrintWriter out;
        private volatile boolean active = true;

        public ClientHandler(Socket s, String u, BufferedReader i, PrintWriter o) {
            socket = s; username = u; in = i; out = o;
        }

        public void run() {
            try {
                String msg;
                while (active && (msg = in.readLine()) != null) {
                    if (msg.startsWith("EXIT")) break;

                    // Check if it's a private message
                    if (msg.startsWith("PRIVATE:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length == 3) {
                            String recipient = parts[1];
                            String message = parts[2];
                            sendPrivate(message, username, recipient);
                        }
                    } else {
                        broadcast(msg, username);
                    }
                }
            } catch (IOException e) {
            } finally {
                disconnect();
            }
        }

        public void send(String msg) { out.println(msg); }

        public void sendUsers(ArrayList<String> users) {
            out.println("USERS:" + users.size() + ":" + String.join(",", users));
        }

        private void disconnect() {
            active = false;
            try { socket.close(); in.close(); out.close(); } catch (IOException e) {}
            removeClient(username);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatServer::new);
    }
}