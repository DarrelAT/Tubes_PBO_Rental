// File: app/src/main/java/App.java
import config.DatabaseConnection;
import controllers.AuthController;
import controllers.RentalController;
import views.LoginView;
import views.MainDashboardView;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Gagal terhubung ke database PostgreSQL!");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Aplikasi Rental Kendaraan - PBO Tubes");
            frame.setSize(1080, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            CardLayout rootLayout = new CardLayout();
            JPanel rootPanel = new JPanel(rootLayout);

            AuthController authController = new AuthController();
            RentalController rentalController = new RentalController(authController);

            MainDashboardView[] dashboardHolder = new MainDashboardView[1];

            LoginView loginView = new LoginView(authController, loggedInUser -> {
                dashboardHolder[0].setActiveUser(loggedInUser);
                rootLayout.show(rootPanel, "DASHBOARD");
            });

            MainDashboardView dashboardView = new MainDashboardView(
                authController,
                rentalController,
                () -> {
                    loginView.resetView();
                    rootLayout.show(rootPanel, "AUTH");
                }
            );
            dashboardHolder[0] = dashboardView;

            rootPanel.add(loginView, "AUTH");
            rootPanel.add(dashboardView, "DASHBOARD");

            frame.add(rootPanel);
            rootLayout.show(rootPanel, "AUTH");
            frame.setVisible(true);
        });
    }
}