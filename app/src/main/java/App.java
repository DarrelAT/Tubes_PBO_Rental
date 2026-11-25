// File: app/src/main/java/App.java
import config.DatabaseConnection;
import models.User;
import models.Vehicle;
import repository.UserRepository;
import repository.VehicleRepository;
import factory.VehicleFactory;
import strategy.RentalCalculator;
import strategy.StandardRentalStrategy;
import strategy.MemberRentalStrategy;
import exceptions.InvalidLoginException;
import exceptions.VehicleNotAvailableException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class App {
    private static UserRepository userRepository = new UserRepository();
    private static VehicleRepository vehicleRepository = new VehicleRepository();
    private static User currentUser = null;

    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Gagal terhubung ke database PostgreSQL!");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Aplikasi Rental Kendaraan - PBO Tubes");
            frame.setSize(1050, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            // --- 1. PANEL LOGIN ---
            JPanel loginPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            loginPanel.setBorder(BorderFactory.createEmptyBorder(60, 120, 60, 120));

            JTextField txtUsername = new JTextField();
            JPasswordField txtPassword = new JPasswordField();
            JButton btnLogin = new JButton("Login");
            JButton btnGoToReg = new JButton("Belum punya akun? Register");
            JLabel lblLoginStatus = new JLabel("", SwingConstants.CENTER);
            lblLoginStatus.setForeground(Color.RED);

            loginPanel.add(new JLabel("Username:")); loginPanel.add(txtUsername);
            loginPanel.add(new JLabel("Password:")); loginPanel.add(txtPassword);
            loginPanel.add(btnLogin); loginPanel.add(btnGoToReg);
            loginPanel.add(lblLoginStatus);

            // --- 2. PANEL REGISTER (DENGAN PILIHAN PAKET MEMBER PROMO) ---
            JPanel regPanel = new JPanel(new GridLayout(7, 2, 10, 10));
            regPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
            JTextField regUser = new JTextField();
            JPasswordField regPass = new JPasswordField();
            JTextField regName = new JTextField();
            
            // Paket langganan member: Harga normal Rp 100.000/bulan
            String[] memberPlans = {
                "Reguler (Gratis - Tanpa Diskon Sewa)",
                "Member 1 Bulan - Rp 100.000",
                "Member 6 Bulan (Promo Bayar 5 Bln) - Rp 500.000",
                "Member 1 Tahun (Promo Bayar 9 Bln) - Rp 900.000"
            };
            JComboBox<String> cmbMemberPlan = new JComboBox<>(memberPlans);
            
            JButton btnRegister = new JButton("Daftar Sekarang");
            JButton btnGoToLogin = new JButton("Sudah punya akun? Login");
            JLabel lblRegStatus = new JLabel("", SwingConstants.CENTER);

            regPanel.add(new JLabel("Username:")); regPanel.add(regUser);
            regPanel.add(new JLabel("Password:")); regPanel.add(regPass);
            regPanel.add(new JLabel("Nama Lengkap:")); regPanel.add(regName);
            regPanel.add(new JLabel("Paket Membership:")); regPanel.add(cmbMemberPlan);
            regPanel.add(btnRegister); regPanel.add(btnGoToLogin);
            regPanel.add(lblRegStatus);

            // --- 3. PANEL DASHBOARD ADMIN (KENDARAAN + RIWAYAT TRANSAKSI + DATA CUSTOMER) ---
            JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
            adminPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JLabel lblAdminWelcome = new JLabel("Dashboard Admin", SwingConstants.CENTER);
            lblAdminWelcome.setFont(new Font("SansSerif", Font.BOLD, 15));

            JTabbedPane adminTabs = new JTabbedPane();

            // Tab 3A: Kelola Kendaraan
            JPanel tabVehicles = new JPanel(new BorderLayout(10, 10));
            DefaultTableModel adminTableModel = new DefaultTableModel(new String[]{"ID", "Brand", "Model", "Harga/Hari", "Status", "Tipe", "Info Khusus"}, 0);
            JTable tableAdminVehicles = new JTable(adminTableModel);
            
            JPanel adminFormPanel = new JPanel(new GridLayout(9, 2, 5, 5));
            JTextField txtBrand = new JTextField();
            JTextField txtModel = new JTextField();
            JTextField txtPrice = new JTextField();
            JComboBox<String> cmbType = new JComboBox<>(new String[]{"CAR", "MOTORCYCLE"});
            JTextField txtExtra = new JTextField(); 
            
            JButton btnAddVehicle = new JButton("Tambah Kendaraan");
            JButton btnUnrentVehicle = new JButton("Selesaikan Sewa (Unrent)");
            JButton btnDeleteVehicle = new JButton("Hapus Kendaraan");
            JButton btnRefreshAdmin = new JButton("Refresh Semua Data");
            JLabel lblAdminStatus = new JLabel("");

            adminFormPanel.add(new JLabel("Brand:")); adminFormPanel.add(txtBrand);
            adminFormPanel.add(new JLabel("Model:")); adminFormPanel.add(txtModel);
            adminFormPanel.add(new JLabel("Harga/Hari (Rp):")); adminFormPanel.add(txtPrice);
            adminFormPanel.add(new JLabel("Tipe:")); adminFormPanel.add(cmbType);
            adminFormPanel.add(new JLabel("Info Pintu/Helm:")); adminFormPanel.add(txtExtra);
            adminFormPanel.add(btnAddVehicle); adminFormPanel.add(btnUnrentVehicle);
            adminFormPanel.add(btnDeleteVehicle); adminFormPanel.add(btnRefreshAdmin);
            adminFormPanel.add(lblAdminStatus);

            tabVehicles.add(new JScrollPane(tableAdminVehicles), BorderLayout.CENTER);
            tabVehicles.add(adminFormPanel, BorderLayout.EAST);

            // Tab 3B: Riwayat Transaksi Rental
            DefaultTableModel historyTableModel = new DefaultTableModel(new String[]{"ID Sewa", "Nama Customer", "Kendaraan", "Durasi (Hari)", "Total Bayar (Rp)", "Tanggal Sewa"}, 0);
            JTable tableHistory = new JTable(historyTableModel);
            JPanel tabHistory = new JPanel(new BorderLayout());
            tabHistory.add(new JScrollPane(tableHistory), BorderLayout.CENTER);

            // Tab 3C: Daftar Customer & Status Member
            DefaultTableModel customerListModel = new DefaultTableModel(new String[]{"ID User", "Username", "Nama Lengkap", "Status Member", "Paket", "Aktif Sampai"}, 0);
            JTable tableCustomerList = new JTable(customerListModel);
            JPanel tabCustomers = new JPanel(new BorderLayout());
            tabCustomers.add(new JScrollPane(tableCustomerList), BorderLayout.CENTER);

            adminTabs.addTab("Kelola Kendaraan", tabVehicles);
            adminTabs.addTab("Riwayat Transaksi", tabHistory);
            adminTabs.addTab("Daftar Customer & Member", tabCustomers);

            JButton btnAdminLogout = new JButton("Logout");

            adminPanel.add(lblAdminWelcome, BorderLayout.NORTH);
            adminPanel.add(adminTabs, BorderLayout.CENTER);
            adminPanel.add(btnAdminLogout, BorderLayout.SOUTH);

            // --- 4. PANEL DASHBOARD CUSTOMER ---
            JPanel customerPanel = new JPanel(new BorderLayout(10, 10));
            customerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JPanel custHeaderPanel = new JPanel(new GridLayout(2, 1));
            JLabel lblCustWelcome = new JLabel("Dashboard Customer", SwingConstants.CENTER);
            lblCustWelcome.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel lblMemberBanner = new JLabel("Status Akun: Memuat...", SwingConstants.CENTER);
            lblMemberBanner.setFont(new Font("SansSerif", Font.PLAIN, 13));
            custHeaderPanel.add(lblCustWelcome);
            custHeaderPanel.add(lblMemberBanner);

            DefaultTableModel customerTableModel = new DefaultTableModel(new String[]{"ID", "Brand", "Model", "Harga/Hari", "Status", "Tipe"}, 0);
            JTable tableVehicles = new JTable(customerTableModel);

            JPanel actionPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField txtDays = new JTextField("3");
            JButton btnRentAction = new JButton("Sewa Kendaraan Terpilih");
            JButton btnRefreshTable = new JButton("Refresh Data");
            JLabel lblCustStatus = new JLabel("");

            actionPanel.add(new JLabel("Durasi Sewa (Hari):")); actionPanel.add(txtDays);
            actionPanel.add(btnRefreshTable); actionPanel.add(btnRentAction);
            actionPanel.add(lblCustStatus);

            JButton btnCustLogout = new JButton("Logout");

            customerPanel.add(custHeaderPanel, BorderLayout.NORTH);
            customerPanel.add(new JScrollPane(tableVehicles), BorderLayout.CENTER);
            customerPanel.add(actionPanel, BorderLayout.EAST);
            customerPanel.add(btnCustLogout, BorderLayout.SOUTH);

            // Masukkan ke CardLayout
            mainPanel.add(loginPanel, "LOGIN");
            mainPanel.add(regPanel, "REGISTER");
            mainPanel.add(adminPanel, "ADMIN_DASHBOARD");
            mainPanel.add(customerPanel, "CUSTOMER_DASHBOARD");

            // Event Navigasi
            btnGoToReg.addActionListener(e -> { lblRegStatus.setText(""); cardLayout.show(mainPanel, "REGISTER"); });
            btnGoToLogin.addActionListener(e -> { lblLoginStatus.setText(""); cardLayout.show(mainPanel, "LOGIN"); });
            btnAdminLogout.addActionListener(e -> { txtUsername.setText(""); txtPassword.setText(""); cardLayout.show(mainPanel, "LOGIN"); });
            btnCustLogout.addActionListener(e -> { txtUsername.setText(""); txtPassword.setText(""); cardLayout.show(mainPanel, "LOGIN"); });

            // Event Register (Dengan Simpan Paket Member & Tanggal Kedaluwarsa)
            btnRegister.addActionListener(e -> {
                String u = regUser.getText().trim();
                String p = new String(regPass.getPassword()).trim();
                String name = regName.getText().trim();
                int planIdx = cmbMemberPlan.getSelectedIndex();

                if (u.isEmpty() || p.isEmpty() || name.isEmpty()) {
                    lblRegStatus.setForeground(Color.RED);
                    lblRegStatus.setText("Semua field wajib diisi!");
                    return;
                }

                User newUser = new models.Customer(0, u, p, name);
                if (userRepository.save(newUser)) {
                    // Update status member berdasarkan paket yang dipilih
                    boolean isMember = planIdx > 0;
                    String tier = "REGULER";
                    LocalDate expiry = null;

                    if (planIdx == 1) {
                        tier = "MEMBER 1 BULAN";
                        expiry = LocalDate.now().plusMonths(1);
                    } else if (planIdx == 2) {
                        tier = "MEMBER 6 BULAN (PROMO)";
                        expiry = LocalDate.now().plusMonths(6);
                    } else if (planIdx == 3) {
                        tier = "MEMBER 1 TAHUN (PROMO)";
                        expiry = LocalDate.now().plusYears(1);
                    }

                    try {
                        Connection c = DatabaseConnection.getConnection();
                        String sqlUpdateMember = "UPDATE users SET is_member = ?, membership_tier = ?, membership_expiry = ? WHERE username = ?";
                        PreparedStatement ps = c.prepareStatement(sqlUpdateMember);
                        ps.setBoolean(1, isMember);
                        ps.setString(2, tier);
                        if (expiry != null) {
                            ps.setDate(3, Date.valueOf(expiry));
                        } else {
                            ps.setNull(3, Types.DATE);
                        }
                        ps.setString(4, u);
                        ps.executeUpdate();

                        lblRegStatus.setForeground(new Color(0, 128, 0));
                        lblRegStatus.setText("Registrasi Berhasil (" + tier + ")! Silakan Login.");
                        regUser.setText(""); regPass.setText(""); regName.setText("");
                        cmbMemberPlan.setSelectedIndex(0);
                    } catch (SQLException ex) {
                        lblRegStatus.setForeground(Color.RED);
                        lblRegStatus.setText("Akun dibuat, tapi gagal set member: " + ex.getMessage());
                    }
                } else {
                    lblRegStatus.setForeground(Color.RED);
                    lblRegStatus.setText("Gagal Register (Username sudah dipakai).");
                }
            });

            // Event Login
            btnLogin.addActionListener(e -> {
                try {
                    String u = txtUsername.getText().trim();
                    String p = new String(txtPassword.getPassword()).trim();

                    if (u.isEmpty() || p.isEmpty()) {
                        throw new InvalidLoginException("Username & Password wajib diisi!");
                    }

                    currentUser = userRepository.getByUsername(u);

                    if (currentUser != null && currentUser.getPassword().equals(p)) {
                        if (currentUser.getRole().equalsIgnoreCase("ADMIN")) {
                            lblAdminWelcome.setText("Halo, " + currentUser.getNamaLengkap() + " (ADMIN)");
                            refreshAdminAllData(adminTableModel, historyTableModel, customerListModel);
                            cardLayout.show(mainPanel, "ADMIN_DASHBOARD");
                        } else {
                            lblCustWelcome.setText("Halo, " + currentUser.getNamaLengkap() + " (CUSTOMER)");
                            updateMemberBanner(currentUser.getId(), lblMemberBanner);
                            refreshCustomerTable(customerTableModel);
                            lblCustStatus.setText("");
                            cardLayout.show(mainPanel, "CUSTOMER_DASHBOARD");
                        }
                    } else {
                        throw new InvalidLoginException("Username atau Password salah!");
                    }
                } catch (InvalidLoginException ex) {
                    lblLoginStatus.setText(ex.getMessage());
                } catch (Exception ex) {
                    lblLoginStatus.setText("Error: " + ex.getMessage());
                }
            });

            // --- ADMIN ACTIONS ---
            btnRefreshAdmin.addActionListener(e -> {
                refreshAdminAllData(adminTableModel, historyTableModel, customerListModel);
                lblAdminStatus.setForeground(Color.BLUE);
                lblAdminStatus.setText("Semua tabel diperbarui.");
            });

            btnAddVehicle.addActionListener(e -> {
                try {
                    String brand = txtBrand.getText().trim();
                    String model = txtModel.getText().trim();
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    String type = (String) cmbType.getSelectedItem();
                    String extra = txtExtra.getText().trim();

                    if (brand.isEmpty() || model.isEmpty() || extra.isEmpty()) {
                        lblAdminStatus.setForeground(Color.RED);
                        lblAdminStatus.setText("Semua field wajib diisi!");
                        return;
                    }

                    Vehicle newVehicle;
                    if (type.equals("CAR")) {
                        int doors = Integer.parseInt(extra);
                        newVehicle = VehicleFactory.createVehicle(0, brand, model, price, "AVAILABLE", "CAR", doors, null);
                    } else {
                        newVehicle = VehicleFactory.createVehicle(0, brand, model, price, "AVAILABLE", "MOTORCYCLE", 0, extra);
                    }

                    if (vehicleRepository.save(newVehicle)) {
                        lblAdminStatus.setForeground(new Color(0, 128, 0));
                        lblAdminStatus.setText("Berhasil tambah kendaraan!");
                        txtBrand.setText(""); txtModel.setText(""); txtPrice.setText(""); txtExtra.setText("");
                        refreshAdminAllData(adminTableModel, historyTableModel, customerListModel);
                    } else {
                        lblAdminStatus.setForeground(Color.RED);
                        lblAdminStatus.setText("Gagal menyimpan ke database.");
                    }
                } catch (NumberFormatException ex) {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Harga/Pintu harus angka valid!");
                } catch (Exception ex) {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Error: " + ex.getMessage());
                }
            });

            btnUnrentVehicle.addActionListener(e -> {
                int selectedRow = tableAdminVehicles.getSelectedRow();
                if (selectedRow == -1) {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Pilih baris kendaraan di tabel!");
                    return;
                }

                int vehicleId = (int) adminTableModel.getValueAt(selectedRow, 0);
                try {
                    Vehicle v = vehicleRepository.getById(vehicleId);
                    v.setStatus("AVAILABLE");
                    vehicleRepository.update(v);
                    lblAdminStatus.setForeground(new Color(0, 128, 0));
                    lblAdminStatus.setText("Unrent sukses (Status AVAILABLE)");
                    refreshAdminAllData(adminTableModel, historyTableModel, customerListModel);
                } catch (Exception ex) {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Gagal unrent: " + ex.getMessage());
                }
            });

            btnDeleteVehicle.addActionListener(e -> {
                int selectedRow = tableAdminVehicles.getSelectedRow();
                if (selectedRow == -1) {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Pilih kendaraan yang akan dihapus!");
                    return;
                }

                int vehicleId = (int) adminTableModel.getValueAt(selectedRow, 0);
                if (vehicleRepository.delete(vehicleId)) {
                    lblAdminStatus.setForeground(new Color(0, 128, 0));
                    lblAdminStatus.setText("Kendaraan berhasil dihapus.");
                    refreshAdminAllData(adminTableModel, historyTableModel, customerListModel);
                } else {
                    lblAdminStatus.setForeground(Color.RED);
                    lblAdminStatus.setText("Gagal menghapus kendaraan.");
                }
            });

            // --- CUSTOMER ACTIONS (OTOMATIS DETEKSI STATUS MEMBER DARI DATABASE) ---
            btnRefreshTable.addActionListener(e -> {
                refreshCustomerTable(customerTableModel);
                updateMemberBanner(currentUser.getId(), lblMemberBanner);
            });

            btnRentAction.addActionListener(e -> {
                try {
                    int selectedRow = tableVehicles.getSelectedRow();
                    if (selectedRow == -1) {
                        lblCustStatus.setForeground(Color.RED);
                        lblCustStatus.setText("Pilih kendaraan di tabel!");
                        return;
                    }

                    int vehicleId = (int) customerTableModel.getValueAt(selectedRow, 0);
                    String status = (String) customerTableModel.getValueAt(selectedRow, 4);
                    double pricePerDay = (double) customerTableModel.getValueAt(selectedRow, 3);

                    if (!status.equalsIgnoreCase("AVAILABLE")) {
                        throw new VehicleNotAvailableException("Kendaraan sedang disewa!");
                    }

                    int days = Integer.parseInt(txtDays.getText().trim());
                    if (days <= 0) {
                        lblCustStatus.setForeground(Color.RED);
                        lblCustStatus.setText("Durasi minimal 1 hari!");
                        return;
                    }

                    // Cek langsung ke database apakah customer ini Member aktif
                    boolean activeMember = isUserActiveMember(currentUser.getId());

                    RentalCalculator calculator;
                    String statusMemberInfo;
                    
                    if (activeMember) {
                        calculator = new RentalCalculator(new MemberRentalStrategy());
                        statusMemberInfo = "Member Aktif - Diskon 15%";
                    } else {
                        calculator = new RentalCalculator(new StandardRentalStrategy());
                        statusMemberInfo = "Tarif Reguler";
                    }

                    double totalCost = calculator.executeCalculation(days, pricePerDay);

                    Connection connDb = DatabaseConnection.getConnection();
                    String insertRental = "INSERT INTO rentals (user_id, vehicle_id, total_days, total_price) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmtRental = connDb.prepareStatement(insertRental);
                    stmtRental.setInt(1, currentUser.getId());
                    stmtRental.setInt(2, vehicleId);
                    stmtRental.setInt(3, days);
                    stmtRental.setDouble(4, totalCost);
                    stmtRental.executeUpdate();

                    Vehicle v = vehicleRepository.getById(vehicleId);
                    v.setStatus("RENTED");
                    vehicleRepository.update(v);

                    lblCustStatus.setForeground(new Color(0, 128, 0));
                    lblCustStatus.setText("Sewa Sukses! Total: Rp " + String.format("%,.0f", totalCost) + " (" + statusMemberInfo + ")");
                    refreshCustomerTable(customerTableModel);

                } catch (VehicleNotAvailableException ex) {
                    lblCustStatus.setForeground(Color.RED);
                    lblCustStatus.setText(ex.getMessage());
                } catch (NumberFormatException ex) {
                    lblCustStatus.setForeground(Color.RED);
                    lblCustStatus.setText("Jumlah hari harus angka valid!");
                } catch (SQLException ex) {
                    lblCustStatus.setForeground(Color.RED);
                    lblCustStatus.setText("DB Error: " + ex.getMessage());
                } catch (Exception ex) {
                    lblCustStatus.setForeground(Color.RED);
                    lblCustStatus.setText("Error: " + ex.getMessage());
                }
            });

            frame.add(mainPanel);
            cardLayout.show(mainPanel, "LOGIN");
            frame.setVisible(true);
        });
    }

    // Cek apakah status member user aktif & belum expired
    private static boolean isUserActiveMember(int userId) {
        try {
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT is_member, membership_expiry FROM users WHERE id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isMember = rs.getBoolean("is_member");
                Date expiry = rs.getDate("membership_expiry");
                if (isMember && expiry != null) {
                    return !expiry.toLocalDate().isBefore(LocalDate.now());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update info status member di header Dashboard Customer
    private static void updateMemberBanner(int userId, JLabel banner) {
        try {
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT is_member, membership_tier, membership_expiry FROM users WHERE id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isMember = rs.getBoolean("is_member");
                String tier = rs.getString("membership_tier");
                Date expiry = rs.getDate("membership_expiry");

                if (isMember && expiry != null && !expiry.toLocalDate().isBefore(LocalDate.now())) {
                    banner.setForeground(new Color(0, 128, 0));
                    banner.setText("Status Akun: " + tier + " | Aktif sampai: " + expiry.toString() + " (Otomatis Diskon 15%)");
                } else {
                    banner.setForeground(Color.DARK_GRAY);
                    banner.setText("Status Akun: REGULER (Tarif Normal)");
                }
            }
        } catch (SQLException e) {
            banner.setText("Status Akun: Reguler");
        }
    }

    // Refresh semua tab Admin (Kendaraan, Riwayat Transaksi, Daftar Customer)
    private static void refreshAdminAllData(DefaultTableModel vehicleModel, DefaultTableModel historyModel, DefaultTableModel customerModel) {
        // 1. Refresh Tabel Kendaraan
        vehicleModel.setRowCount(0);
        List<Vehicle> vehicles = vehicleRepository.getAll();
        for (Vehicle v : vehicles) {
            String extraInfo = "";
            if (v instanceof models.Car) {
                extraInfo = "Pintu: " + ((models.Car) v).getJumlahPintu();
            } else if (v instanceof models.Motorcycle) {
                extraInfo = "Helm: " + ((models.Motorcycle) v).getTipeHelm();
            }
            vehicleModel.addRow(new Object[]{
                v.getId(), v.getBrand(), v.getModel(), v.getPricePerDay(), v.getStatus(), v.getVehicleType(), extraInfo
            });
        }

        // 2. Refresh Tabel Riwayat Transaksi (JOIN rentals, users, vehicles)
        historyModel.setRowCount(0);
        try {
            Connection c = DatabaseConnection.getConnection();
            String sqlHistory = "SELECT r.id, u.nama_lengkap, CONCAT(v.brand, ' ', v.model) AS kendaraan, r.total_days, r.total_price, r.rental_date " +
                                "FROM rentals r " +
                                "JOIN users u ON r.user_id = u.id " +
                                "JOIN vehicles v ON r.vehicle_id = v.id " +
                                "ORDER BY r.rental_date DESC";
            ResultSet rs = c.createStatement().executeQuery(sqlHistory);
            while (rs.next()) {
                historyModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_lengkap"),
                    rs.getString("kendaraan"),
                    rs.getInt("total_days"),
                    String.format("%,.0f", rs.getDouble("total_price")),
                    rs.getTimestamp("rental_date")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat riwayat transaksi: " + e.getMessage());
        }

        // 3. Refresh Tabel Daftar Customer & Member
        customerModel.setRowCount(0);
        try {
            Connection c = DatabaseConnection.getConnection();
            String sqlUsers = "SELECT id, username, nama_lengkap, is_member, membership_tier, membership_expiry FROM users WHERE role = 'CUSTOMER' ORDER BY id ASC";
            ResultSet rs = c.createStatement().executeQuery(sqlUsers);
            while (rs.next()) {
                boolean isMember = rs.getBoolean("is_member");
                Date exp = rs.getDate("membership_expiry");
                customerModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("nama_lengkap"),
                    isMember ? "AKTIF" : "NON-MEMBER",
                    rs.getString("membership_tier"),
                    exp != null ? exp.toString() : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat daftar customer: " + e.getMessage());
        }
    }

    private static void refreshCustomerTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Vehicle> vehicles = vehicleRepository.getAll();
        for (Vehicle v : vehicles) {
            model.addRow(new Object[]{
                v.getId(), v.getBrand(), v.getModel(), v.getPricePerDay(), v.getStatus(), v.getVehicleType()
            });
        }
    }
}