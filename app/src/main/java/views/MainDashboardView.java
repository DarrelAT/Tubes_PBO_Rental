package views;

import controllers.AuthController;
import controllers.RentalController;
import exceptions.VehicleNotAvailableException;
import models.User;
import views.components.NotificationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MainDashboardView extends JPanel {
    private final AuthController authController;
    private final RentalController rentalController;
    private final Runnable onLogout;

    private final CardLayout roleCardLayout;
    private final JPanel roleCards;

    private User currentUser;

    // --- Admin & Staff ---
    private final NotificationPanel adminNotification = new NotificationPanel();
    private final JLabel lblAdminWelcome = new JLabel("Dashboard", SwingConstants.CENTER);
    private final DefaultTableModel adminTableModel = new DefaultTableModel(
        new String[]{"ID", "Brand", "Model", "Harga/Hari", "Status", "Tipe", "Info Khusus"}, 0
    );
    private final JTable tableAdminVehicles = new JTable(adminTableModel);

    private final DefaultTableModel historyTableModel = new DefaultTableModel(
        new String[]{"ID Sewa", "Nama Customer", "Kendaraan", "Durasi (Hari)", "Total Bayar (Rp)", "Tanggal Sewa"}, 0
    );
    private final JTable tableHistory = new JTable(historyTableModel);

    private final DefaultTableModel customerListModel = new DefaultTableModel(
        new String[]{"ID User", "Username", "Nama Lengkap", "Status Member", "Paket", "Aktif Sampai"}, 0
    );
    private final JTable tableCustomerList = new JTable(customerListModel);

    private final JTextField txtBrand = new JTextField();
    private final JTextField txtModel = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"CAR", "MOTORCYCLE"});
    private final JTextField txtExtra = new JTextField();
    private final JTextField txtStaffDays = new JTextField("1");

    private final JButton btnAddVehicle = new JButton("Tambah Kendaraan");
    private final JButton btnStaffBookVehicle = new JButton("Book Kendaraan (Staff/Walk-in)");
    private final JButton btnUnrentVehicle = new JButton("Selesaikan Sewa (Unrent)");
    private final JButton btnDeleteVehicle = new JButton("Hapus Kendaraan");
    private final JButton btnRefreshAdmin = new JButton("Refresh Semua Data");
    private final JButton btnAdminLogout = new JButton("Logout");

    // --- Customer ---
    private final NotificationPanel custNotification = new NotificationPanel();
    private final JLabel lblCustWelcome = new JLabel("Dashboard Customer", SwingConstants.CENTER);
    private final JLabel lblMemberBanner = new JLabel("Status Akun: Memuat...", SwingConstants.CENTER);
    private final DefaultTableModel customerTableModel = new DefaultTableModel(
        new String[]{"ID", "Brand", "Model", "Harga/Hari", "Status", "Tipe"}, 0
    );
    private final JTable tableCustomerVehicles = new JTable(customerTableModel);

    private final JTextField txtDays = new JTextField("3");
    private final JButton btnRentAction = new JButton("Sewa Kendaraan Terpilih");
    private final JButton btnRefreshTable = new JButton("Refresh Data");
    private final JButton btnCustLogout = new JButton("Logout");

    public MainDashboardView(AuthController authController, RentalController rentalController, Runnable onLogout) {
        this.authController = authController;
        this.rentalController = rentalController;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        roleCardLayout = new CardLayout();
        roleCards = new JPanel(roleCardLayout);

        // --- Panel Admin & Staff ---
        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel adminTop = new JPanel(new BorderLayout(5, 5));
        lblAdminWelcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        adminTop.add(lblAdminWelcome, BorderLayout.NORTH);
        adminTop.add(adminNotification, BorderLayout.SOUTH);

        JTabbedPane adminTabs = new JTabbedPane();
        JPanel tabVehicles = new JPanel(new BorderLayout(10, 10));
        JPanel adminFormPanel = new JPanel(new GridLayout(10, 2, 5, 5));

        adminFormPanel.add(new JLabel("Brand:")); adminFormPanel.add(txtBrand);
        adminFormPanel.add(new JLabel("Model:")); adminFormPanel.add(txtModel);
        adminFormPanel.add(new JLabel("Harga/Hari (Rp):")); adminFormPanel.add(txtPrice);
        adminFormPanel.add(new JLabel("Tipe:")); adminFormPanel.add(cmbType);
        adminFormPanel.add(new JLabel("Info Pintu/Helm:")); adminFormPanel.add(txtExtra);
        adminFormPanel.add(new JLabel("Durasi Book (Hari):")); adminFormPanel.add(txtStaffDays);
        adminFormPanel.add(btnAddVehicle); adminFormPanel.add(btnStaffBookVehicle);
        adminFormPanel.add(btnUnrentVehicle); adminFormPanel.add(btnDeleteVehicle);
        adminFormPanel.add(btnRefreshAdmin); adminFormPanel.add(new JLabel(""));

        tabVehicles.add(new JScrollPane(tableAdminVehicles), BorderLayout.CENTER);
        tabVehicles.add(adminFormPanel, BorderLayout.EAST);

        JPanel tabHistory = new JPanel(new BorderLayout());
        tabHistory.add(new JScrollPane(tableHistory), BorderLayout.CENTER);

        JPanel tabCustomers = new JPanel(new BorderLayout());
        tabCustomers.add(new JScrollPane(tableCustomerList), BorderLayout.CENTER);

        adminTabs.addTab("Kelola Kendaraan", tabVehicles);
        adminTabs.addTab("Riwayat Transaksi", tabHistory);
        adminTabs.addTab("Daftar Customer & Member", tabCustomers);

        adminPanel.add(adminTop, BorderLayout.NORTH);
        adminPanel.add(adminTabs, BorderLayout.CENTER);
        adminPanel.add(btnAdminLogout, BorderLayout.SOUTH);

        // --- Panel Customer ---
        JPanel customerPanel = new JPanel(new BorderLayout(10, 10));
        customerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel custHeaderPanel = new JPanel(new GridLayout(2, 1));
        lblCustWelcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblMemberBanner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        custHeaderPanel.add(lblCustWelcome);
        custHeaderPanel.add(lblMemberBanner);

        JPanel custTop = new JPanel(new BorderLayout(5, 5));
        custTop.add(custHeaderPanel, BorderLayout.NORTH);
        custTop.add(custNotification, BorderLayout.SOUTH);

        JPanel actionPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        actionPanel.add(new JLabel("Durasi Sewa (Hari):")); actionPanel.add(txtDays);
        actionPanel.add(btnRefreshTable); actionPanel.add(btnRentAction);

        customerPanel.add(custTop, BorderLayout.NORTH);
        customerPanel.add(new JScrollPane(tableCustomerVehicles), BorderLayout.CENTER);
        customerPanel.add(actionPanel, BorderLayout.EAST);
        customerPanel.add(btnCustLogout, BorderLayout.SOUTH);

        roleCards.add(adminPanel, "ADMIN_OR_STAFF");
        roleCards.add(customerPanel, "CUSTOMER");
        add(roleCards, BorderLayout.CENTER);

        // --- Events ---
        btnAdminLogout.addActionListener(e -> handleLogout());
        btnCustLogout.addActionListener(e -> handleLogout());

        btnRefreshAdmin.addActionListener(e -> {
            refreshAdminAllData();
            adminNotification.showSuccess("Semua tabel diperbarui.");
        });

        btnAddVehicle.addActionListener(e -> handleAddVehicle());
        btnStaffBookVehicle.addActionListener(e -> handleStaffBook());
        btnUnrentVehicle.addActionListener(e -> handleUnrent());
        btnDeleteVehicle.addActionListener(e -> handleDeleteVehicle());

        btnRefreshTable.addActionListener(e -> {
            refreshCustomerData();
            custNotification.showSuccess("Data kendaraan & status member diperbarui.");
        });
        btnRentAction.addActionListener(e -> handleCustomerRent());
    }

    public void setActiveUser(User user) {
        this.currentUser = user;
        String role = user != null && user.getRole() != null
            ? user.getRole().trim().toUpperCase()
            : "CUSTOMER";

        if ("ADMIN".equals(role)) {
            lblAdminWelcome.setText("Halo, " + user.getNamaLengkap() + " (ADMIN - Akses Penuh)");
            setAdminControlsEnabled(true);
            adminNotification.clear();
            refreshAdminAllData();
            roleCardLayout.show(roleCards, "ADMIN_OR_STAFF");
        } else if ("STAFF".equals(role)) {
            lblAdminWelcome.setText("Halo, " + user.getNamaLengkap() + " (STAFF - Operasional Terbatas)");
            setAdminControlsEnabled(false);
            adminNotification.showSuccess("Mode STAFF: bisa Book & Unrent. Tambah/Hapus unit khusus ADMIN.");
            refreshAdminAllData();
            roleCardLayout.show(roleCards, "ADMIN_OR_STAFF");
        } else {
            lblCustWelcome.setText("Halo, " + user.getNamaLengkap() + " (CUSTOMER)");
            custNotification.clear();
            refreshCustomerData();
            roleCardLayout.show(roleCards, "CUSTOMER");
        }
    }

    private void setAdminControlsEnabled(boolean fullAdmin) {
        btnAddVehicle.setEnabled(fullAdmin);
        btnDeleteVehicle.setEnabled(fullAdmin);
        btnStaffBookVehicle.setEnabled(true);
        btnUnrentVehicle.setEnabled(true);
        txtBrand.setEnabled(fullAdmin);
        txtModel.setEnabled(fullAdmin);
        txtPrice.setEnabled(fullAdmin);
        cmbType.setEnabled(fullAdmin);
        txtExtra.setEnabled(fullAdmin);
    }

    private void handleLogout() {
        authController.logout();
        this.currentUser = null;
        if (onLogout != null) {
            onLogout.run();
        }
    }

    private void refreshAdminAllData() {
        fillTable(adminTableModel, rentalController.getAdminVehicleRows());
        fillTable(historyTableModel, rentalController.getRentalHistoryRows());
        fillTable(customerListModel, rentalController.getCustomerListRows());
    }

    private void refreshCustomerData() {
        if (currentUser != null) {
            String bannerText = authController.getMemberBannerText(currentUser.getId());
            boolean active = authController.isActiveMember(currentUser.getId());
            lblMemberBanner.setForeground(active ? new Color(0, 128, 0) : Color.DARK_GRAY);
            lblMemberBanner.setText(bannerText);
        }
        fillTable(customerTableModel, rentalController.getCustomerVehicleRows());
    }

    private void fillTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private void handleAddVehicle() {
        try {
            rentalController.addVehicle(
                txtBrand.getText(),
                txtModel.getText(),
                txtPrice.getText(),
                (String) cmbType.getSelectedItem(),
                txtExtra.getText(),
                currentUser
            );
            txtBrand.setText("");
            txtModel.setText("");
            txtPrice.setText("");
            txtExtra.setText("");
            adminNotification.showSuccess("Berhasil tambah kendaraan!");
            refreshAdminAllData();
        } catch (NumberFormatException ex) {
            adminNotification.showError("Harga/Pintu harus angka valid!");
        } catch (Exception ex) {
            adminNotification.showError(ex.getMessage());
        }
    }

    private void handleStaffBook() {
        int selectedRow = tableAdminVehicles.getSelectedRow();
        if (selectedRow == -1) {
            adminNotification.showError("Pilih kendaraan yang akan di-book di tabel!");
            return;
        }

        try {
            int vehicleId = (int) adminTableModel.getValueAt(selectedRow, 0);
            int days = Integer.parseInt(txtStaffDays.getText().trim());
            String msg = rentalController.processStaffBooking(vehicleId, days, currentUser);
            adminNotification.showSuccess(msg);
            refreshAdminAllData();
        } catch (NumberFormatException ex) {
            adminNotification.showError("Durasi hari harus angka valid!");
        } catch (VehicleNotAvailableException | SQLException | IllegalArgumentException | SecurityException ex) {
            adminNotification.showError(ex.getMessage());
        } catch (Exception ex) {
            adminNotification.showError("Gagal booking: " + ex.getMessage());
        }
    }

    private void handleUnrent() {
        int selectedRow = tableAdminVehicles.getSelectedRow();
        if (selectedRow == -1) {
            adminNotification.showError("Pilih baris kendaraan di tabel!");
            return;
        }

        try {
            int vehicleId = (int) adminTableModel.getValueAt(selectedRow, 0);
            rentalController.unrentVehicle(vehicleId, currentUser);
            adminNotification.showSuccess("Unrent sukses (Status AVAILABLE)");
            refreshAdminAllData();
        } catch (Exception ex) {
            adminNotification.showError("Gagal unrent: " + ex.getMessage());
        }
    }

    private void handleDeleteVehicle() {
        int selectedRow = tableAdminVehicles.getSelectedRow();
        if (selectedRow == -1) {
            adminNotification.showError("Pilih kendaraan yang akan dihapus!");
            return;
        }

        try {
            int vehicleId = (int) adminTableModel.getValueAt(selectedRow, 0);
            rentalController.deleteVehicle(vehicleId, currentUser);
            adminNotification.showSuccess("Kendaraan berhasil dihapus.");
            refreshAdminAllData();
        } catch (Exception ex) {
            adminNotification.showError(ex.getMessage());
        }
    }

    private void handleCustomerRent() {
        int selectedRow = tableCustomerVehicles.getSelectedRow();
        if (selectedRow == -1) {
            custNotification.showError("Pilih kendaraan di tabel!");
            return;
        }

        try {
            int vehicleId = (int) customerTableModel.getValueAt(selectedRow, 0);
            int days = Integer.parseInt(txtDays.getText().trim());
            String msg = rentalController.processCustomerRental(vehicleId, days, currentUser);
            custNotification.showSuccess(msg);
            refreshCustomerData();
        } catch (NumberFormatException ex) {
            custNotification.showError("Jumlah hari harus angka valid!");
        } catch (VehicleNotAvailableException | SQLException | IllegalArgumentException ex) {
            custNotification.showError(ex.getMessage());
        } catch (Exception ex) {
            custNotification.showError("Error: " + ex.getMessage());
        }
    }
}