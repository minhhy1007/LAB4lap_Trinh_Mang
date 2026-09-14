package ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Login UI frame for user authentication and connecting to the server.
 */
public class LoginFrame extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Chat Client - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        panel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost");
        panel.add(hostField);

        panel.add(new JLabel("Port:"));
        portField = new JTextField("8888");
        panel.add(portField);

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        loginButton = new JButton("Connect & Login");
        panel.add(new JLabel()); // empty placeholder
        panel.add(loginButton);

        add(panel, BorderLayout.CENTER);
    }
}
