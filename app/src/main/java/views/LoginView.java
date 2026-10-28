package views;

import controllers.AuthController;
import views.components.NotificationPanel;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private NotificationPanel notificationPanel;
    private AuthController authController;

    public LoginView(AuthController authController) {
        this.authController = authController;

        setTitle("Login - Systems Rental Vehicle");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        notificationPanel = new NotificationPanel();
        add(notificationPanel, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelForm.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panelForm.add(txtUsername);

        panelForm.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        btnLogin = new JButton("Login");
        panelForm.add(new JLabel(""));
        panelForm.add(btnLogin);

        add(panelForm, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> processLogin());
    }

    private void processLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            notificationPanel.showError("Username dan Password tidak boleh kosong!");
            return;
        }

        if (authController.login(username, password)) {
            notificationPanel.showSuccess("Login Berhasil!");
            Timer timer = new Timer(1000, event -> {
                dispose();
                new MainDashboardView(authController, authController.getCurrentUser()).setVisible(true);
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            notificationPanel.showError("Username atau Password salah!");
        }
    }
}