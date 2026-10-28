package views;

import controllers.AuthController;
import controllers.RentalController;
import exceptions.VehicleNotAvailableException;
import models.User;
import models.Vehicle;
import views.components.NotificationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainDashboardView extends JFrame {
    private User currentUser;
    private AuthController authController;
    private RentalController rentalController;
    private JTable tableVehicles;
    private DefaultTableModel tableModel;
    private NotificationPanel notificationPanel;

    public MainDashboardView(AuthController authController, User user) {
        this.authController = authController;
        this.currentUser = user;
        this.rentalController = new RentalController();

        setTitle("Dashboard Rental - " + user.getRole());
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        notificationPanel = new NotificationPanel();
        add(notificationPanel, BorderLayout.NORTH);

        // Header Panel
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel lblWelcome = new JLabel("Selamat Datang, " + user.getNamaLengkap() + " (" + user.getRole() + ")");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton btnLogout = new JButton("Logout");

        panelHeader.add(lblWelcome, BorderLayout.WEST);
        panelHeader.add(btnLogout, BorderLayout.EAST);
        add(panelHeader, BorderLayout.SOUTH);

        // Table Panel
        String[] columns = {"ID", "Brand", "Model", "Harga/Hari", "Status", "Tipe"};
        tableModel = new DefaultTableModel(columns, 0);
        tableVehicles = new JTable(tableModel);
        loadVehicleData();

        add(new JScrollPane(tableVehicles), BorderLayout.CENTER);

        // Action Panel
        JPanel panelAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRent = new JButton("Sewa Kendaraan");
        panelAction.add(btnRent);
        add(panelAction, BorderLayout.SOUTH);

        // Events
        btnLogout.addActionListener(e -> {
            authController.logout();
            dispose();
            new LoginView(authController).setVisible(true);
        });

        btnRent.addActionListener(e -> processRentAction());
    }

    private void loadVehicleData() {
        tableModel.setRowCount(0);
        List<Vehicle> vehicles = rentalController.getAllVehicles();
        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{
                v.getId(), v.getBrand(), v.getModel(), v.getPricePerDay(), v.getStatus(), v.getVehicleType()
            });
        }
    }

    private void processRentAction() {
        int selectedRow = tableVehicles.getSelectedRow();
        if (selectedRow == -1) {
            notificationPanel.showError("Pilih kendaraan dari tabel terlebih dahulu!");
            return;
        }

        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
        String daysInput = JOptionPane.showInputDialog(this, "Masukkan durasi sewa (hari):"); // Input sederhana
        
        if (daysInput != null && !daysInput.isEmpty()) {
            try {
                int days = Integer.parseInt(daysInput);
                double total = rentalController.processRental(vehicleId, days, currentUser);
                notificationPanel.showSuccess("Sewa Berhasil! Total Biaya: Rp " + total);
                loadVehicleData();
            } catch (VehicleNotAvailableException ex) {
                notificationPanel.showError(ex.getMessage());
            } catch (NumberFormatException ex) {
                notificationPanel.showError("Durasi sewa harus berupa angka!");
            }
        }
    }
}