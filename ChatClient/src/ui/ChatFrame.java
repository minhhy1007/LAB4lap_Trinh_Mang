package ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * Main chat window UI providing messaging, active users list, and file transfer triggers.
 */
public class ChatFrame extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton sendFileButton;
    private JList<String> userList;

    public ChatFrame(String username) {
        setTitle("Chat Room - " + username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Active user list on the right
        userList = new JList<>();
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new java.awt.Dimension(150, 0));
        add(userScrollPane, BorderLayout.EAST);

        // Bottom input and action panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        sendButton = new JButton("Send");
        sendFileButton = new JButton("Send File");
        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }
}
