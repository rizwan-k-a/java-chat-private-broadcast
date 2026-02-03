package el;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatClient extends JFrame {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private JTextArea chatArea;
    private JTextField msgInput;
    private JLabel statusLabel, usersLabel;
    private JList<String> userList;
    private DefaultListModel<String> userModel;
    private String selectedUser = "All";
    private volatile boolean connected = false;

    public ChatClient() {
        setupUI();
        connectToServer();
    }

    private void setupUI() {
        setTitle("Chat Client");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 140, 126));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        statusLabel = new JLabel("Connecting...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        usersLabel = new JLabel("Online: 0");
        usersLabel.setForeground(Color.WHITE);
        usersLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        header.add(statusLabel, BorderLayout.WEST);
        header.add(usersLabel, BorderLayout.EAST);

        // Left Sidebar - User List
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(200, 200, 200)));
        sidebar.setBackground(Color.WHITE);

        JPanel sidebarHeader = new JPanel();
        sidebarHeader.setBackground(new Color(240, 240, 240));
        sidebarHeader.setBorder(new EmptyBorder(12, 10, 12, 10));
        JLabel chatLabel = new JLabel("Select Chat");
        chatLabel.setFont(new Font("Arial", Font.BOLD, 15));
        sidebarHeader.add(chatLabel);

        userModel = new DefaultListModel<>();
        userModel.addElement("All (Everyone)");
        userList = new JList<>(userModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setSelectedIndex(0);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.setFixedCellHeight(50);
        userList.setCellRenderer(new UserListRenderer());
        userList.setBackground(Color.WHITE);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    if (selected.equals("All (Everyone)")) {
                        selectedUser = "All";
                    } else {
                        selectedUser = selected;
                    }
                    updateChatHeader();
                }
            }
        });

        sidebar.add(sidebarHeader, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(userList), BorderLayout.CENTER);

        // Center - Chat Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(230, 221, 212));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(15, 15, 15, 15));
        chatArea.setBackground(new Color(230, 221, 212));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        centerPanel.add(chatScroll, BorderLayout.CENTER);

        // Bottom - Message Input with TWO SEND BUTTONS
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(new Color(245, 245, 245));

        msgInput = new JTextField();
        msgInput.setFont(new Font("Arial", Font.PLAIN, 15));
        msgInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        msgInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (selectedUser.equals("All")) {
                        sendToAll();
                    } else {
                        sendPrivate();
                    }
                }
            }
        });

        // Button Panel with TWO BUTTONS side by side
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(new Color(245, 245, 245));

        JButton sendAllBtn = new JButton("Send to All");
        sendAllBtn.setFont(new Font("Arial", Font.BOLD, 14));
        sendAllBtn.setBackground(new Color(25, 118, 210));
        sendAllBtn.setForeground(Color.WHITE);
        sendAllBtn.setFocusPainted(false);
        sendAllBtn.setBorderPainted(false);
        sendAllBtn.setPreferredSize(new Dimension(140, 45));
        sendAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendAllBtn.addActionListener(e -> sendToAll());

        JButton sendPrivateBtn = new JButton("Send Private");
        sendPrivateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        sendPrivateBtn.setBackground(new Color(18, 140, 126));
        sendPrivateBtn.setForeground(Color.WHITE);
        sendPrivateBtn.setFocusPainted(false);
        sendPrivateBtn.setBorderPainted(false);
        sendPrivateBtn.setPreferredSize(new Dimension(140, 45));
        sendPrivateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendPrivateBtn.addActionListener(e -> sendPrivate());

        btnPanel.add(sendAllBtn);
        btnPanel.add(sendPrivateBtn);

        inputPanel.add(msgInput, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);

        // Layout
        add(header, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                username = JOptionPane.showInputDialog(this, "Enter Your Username:", "Login", JOptionPane.QUESTION_MESSAGE);
                if (username == null || username.trim().isEmpty()) System.exit(0);

                username = username.trim();

                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(username);

                // Check for username error
                String response = in.readLine();
                if (response != null && response.startsWith("ERROR:")) {
                    JOptionPane.showMessageDialog(this, "Username already exists! Choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                    socket.close();
                    System.exit(1);
                    return;
                }

                connected = true;
                SwingUtilities.invokeLater(() -> {
                    setTitle("Chat - " + username);
                    statusLabel.setText(username);
                });

                if (response != null) processMessage(response);
                new MessageListener().start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }).start();
    }

    private void sendToAll() {
        if (!connected) return;
        String msg = msgInput.getText().trim();
        if (!msg.isEmpty()) {
            out.println(msg);
            msgInput.setText("");
        }
    }

    private void sendPrivate() {
        if (!connected) return;
        String msg = msgInput.getText().trim();
        if (msg.isEmpty()) return;

        if (selectedUser.equals("All")) {
            JOptionPane.showMessageDialog(this, 
                "Please select a specific user from the list for private chat!", 
                "Select User First", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        out.println("PRIVATE:" + selectedUser + ":" + msg);
        msgInput.setText("");
    }

    private void addMessage(String time, String user, String msg, boolean isPrivate, boolean isSent) {
        SwingUtilities.invokeLater(() -> {
            String prefix = "";
            if (isPrivate) {
                prefix = isSent ? "You to " + user : user + " to You";
                chatArea.append(String.format("[%s] %s (private): %s\n", time, prefix, msg));
            } else {
                chatArea.append(String.format("[%s] %s: %s\n", time, user, msg));
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void updateUsers(int count, String users) {
        SwingUtilities.invokeLater(() -> {
            usersLabel.setText("Online: " + count);
            String currentSelection = selectedUser;
            userModel.clear();
            userModel.addElement("All (Everyone)");

            if (!users.isEmpty()) {
                for (String user : users.split(",")) {
                    String trimmed = user.trim();
                    if (!trimmed.equals(username)) {
                        userModel.addElement(trimmed);
                    }
                }
            }

            if (currentSelection.equals("All")) {
                userList.setSelectedIndex(0);
            } else {
                for (int i = 0; i < userModel.size(); i++) {
                    if (userModel.get(i).equals(currentSelection)) {
                        userList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
    }

    private void updateChatHeader() {
        chatArea.append("\n--- Now chatting with: " + selectedUser + " ---\n\n");
    }

    private void processMessage(String msg) {
        if (msg.startsWith("USERS:")) {
            String[] parts = msg.split(":", 3);
            if (parts.length == 3) {
                updateUsers(Integer.parseInt(parts[1]), parts[2]);
            }
        } else if (msg.startsWith("PRIVATE:")) {
            String[] parts = msg.split(":", 4);
            if (parts.length == 4) {
                addMessage(parts[1], parts[2], parts[3], true, false);
            }
        } else if (msg.startsWith("SENT:")) {
            String[] parts = msg.split(":", 4);
            if (parts.length == 4) {
                addMessage(parts[1], parts[2], parts[3], true, true);
            }
        } else if (msg.startsWith("[")) {
            int closeIdx = msg.indexOf("]");
            if (closeIdx > 0) {
                String time = msg.substring(1, closeIdx);
                String rest = msg.substring(closeIdx + 2);
                int colonIdx = rest.indexOf(": ");
                if (colonIdx > 0) {
                    String user = rest.substring(0, colonIdx);
                    String message = rest.substring(colonIdx + 2);
                    addMessage(time, user, message, false, false);
                }
            }
        }
    }

    class MessageListener extends Thread {
        public void run() {
            try {
                String msg;
                while (connected && (msg = in.readLine()) != null) {
                    processMessage(msg);
                }
            } catch (IOException e) {
            } finally {
                connected = false;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Disconnected");
                    statusLabel.setForeground(Color.RED);
                    msgInput.setEnabled(false);
                });
            }
        }
    }

    class UserListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(10, 15, 10, 15));
            if (isSelected) {
                label.setBackground(new Color(220, 248, 198));
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            }
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}