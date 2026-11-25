package views;

import controllers.AuthController;
import exceptions.InvalidLoginException;
import models.User;
import views.components.NotificationPanel;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginView extends JPanel {
    private final AuthController authController;
    private final Consumer<User> onLoginSuccess;

    private final CardLayout authCardLayout;
    private final JPanel authCards;

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final NotificationPanel loginNotification = new NotificationPanel();

    private final JTextField regUser = new JTextField();
    private final JPasswordField regPass = new JPasswordField();
    private final JTextField regName = new JTextField();
    private final JComboBox<String> cmbMemberPlan = new JComboBox<>(new String[]{
        "Reguler (Gratis - Tanpa Diskon Sewa)",
        "Member 1 Bulan - Rp 100.000",
        "Member 6 Bulan (Promo Bayar 5 Bln) - Rp 500.000",
        "Member 1 Tahun (Promo Bayar 9 Bln) - Rp 900.000"
    });
    private final NotificationPanel regNotification = new NotificationPanel();

    public LoginView(AuthController authController, Consumer<User> onLoginSuccess) {
        this.authController = authController;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new BorderLayout());
        authCardLayout = new CardLayout();
        authCards = new JPanel(authCardLayout);

        // --- Panel Login ---
        JPanel loginWrapper = new JPanel(new BorderLayout(10, 10));
        loginWrapper.setBorder(BorderFactory.createEmptyBorder(40, 120, 40, 120));
        loginWrapper.add(loginNotification, BorderLayout.NORTH);

        JPanel loginForm = new JPanel(new GridLayout(3, 2, 10, 10));
        JButton btnLogin = new JButton("Login");
        JButton btnGoToReg = new JButton("Belum punya akun? Register");

        loginForm.add(new JLabel("Username:"));
        loginForm.add(txtUsername);
        loginForm.add(new JLabel("Password:"));
        loginForm.add(txtPassword);
        loginForm.add(btnLogin);
        loginForm.add(btnGoToReg);

        loginWrapper.add(loginForm, BorderLayout.CENTER);

        // --- Panel Register ---
        JPanel regWrapper = new JPanel(new BorderLayout(10, 10));
        regWrapper.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        regWrapper.add(regNotification, BorderLayout.NORTH);

        JPanel regForm = new JPanel(new GridLayout(5, 2, 10, 10));
        JButton btnRegister = new JButton("Daftar Sekarang");
        JButton btnGoToLogin = new JButton("Sudah punya akun? Login");

        regForm.add(new JLabel("Username:"));
        regForm.add(regUser);
        regForm.add(new JLabel("Password:"));
        regForm.add(regPass);
        regForm.add(new JLabel("Nama Lengkap:"));
        regForm.add(regName);
        regForm.add(new JLabel("Paket Membership:"));
        regForm.add(cmbMemberPlan);
        regForm.add(btnRegister);
        regForm.add(btnGoToLogin);

        regWrapper.add(regForm, BorderLayout.CENTER);

        authCards.add(loginWrapper, "LOGIN_FORM");
        authCards.add(regWrapper, "REGISTER_FORM");
        add(authCards, BorderLayout.CENTER);

        btnGoToReg.addActionListener(e -> {
            regNotification.clear();
            authCardLayout.show(authCards, "REGISTER_FORM");
        });

        btnGoToLogin.addActionListener(e -> {
            loginNotification.clear();
            authCardLayout.show(authCards, "LOGIN_FORM");
        });

        btnLogin.addActionListener(e -> processLogin());
        btnRegister.addActionListener(e -> processRegister());
    }

    public void resetView() {
        txtUsername.setText("");
        txtPassword.setText("");
        loginNotification.clear();
        regNotification.clear();
        authCardLayout.show(authCards, "LOGIN_FORM");
    }

    private void processLogin() {
        try {
            User user = authController.login(txtUsername.getText(), new String(txtPassword.getPassword()));
            loginNotification.showSuccess("Login Berhasil!");
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } catch (InvalidLoginException ex) {
            loginNotification.showError(ex.getMessage());
        } catch (Exception ex) {
            loginNotification.showError("Error: " + ex.getMessage());
        }
    }

    private void processRegister() {
        try {
            String tier = authController.registerCustomer(
                regUser.getText(),
                new String(regPass.getPassword()),
                regName.getText(),
                cmbMemberPlan.getSelectedIndex()
            );
            regUser.setText("");
            regPass.setText("");
            regName.setText("");
            cmbMemberPlan.setSelectedIndex(0);
            regNotification.showSuccess("Registrasi Berhasil (" + tier + ")! Silakan Login.");
        } catch (Exception ex) {
            regNotification.showError(ex.getMessage());
        }
    }
}