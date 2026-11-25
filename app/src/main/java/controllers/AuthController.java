package controllers;

import config.DatabaseConnection;
import exceptions.InvalidLoginException;
import models.Customer;
import models.User;
import repository.UserRepository;

import java.sql.*;
import java.time.LocalDate;

public class AuthController {
    private final UserRepository userRepo;
    private User currentUser;

    public AuthController() {
        this.userRepo = new UserRepository();
    }

    public User login(String username, String password) throws InvalidLoginException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new InvalidLoginException("Username dan Password wajib diisi!");
        }

        User user = userRepo.getByUsername(username.trim());
        if (user == null || !user.getPassword().equals(password.trim())) {
            throw new InvalidLoginException("Username atau Password salah!");
        }

        this.currentUser = user;
        return this.currentUser;
    }

    public String registerCustomer(String username, String password, String namaLengkap, int planIdx) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || namaLengkap == null || namaLengkap.trim().isEmpty()) {
            throw new IllegalArgumentException("Semua field wajib diisi!");
        }

        String cleanUser = username.trim();
        User newUser = new Customer(0, cleanUser, password.trim(), namaLengkap.trim());
        if (!userRepo.save(newUser)) {
            throw new IllegalStateException("Gagal Register (Username sudah dipakai).");
        }

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
            ps.setString(4, cleanUser);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Akun dibuat, tapi gagal set member: " + ex.getMessage(), ex);
        }

        return tier;
    }

    public boolean isActiveMember(int userId) {
        try {
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT is_member, membership_expiry FROM users WHERE id = ?");
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isMember = rs.getBoolean("is_member");
                    Date expiry = rs.getDate("membership_expiry");
                    if (isMember && expiry != null) {
                        return !expiry.toLocalDate().isBefore(LocalDate.now());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cek member: " + e.getMessage());
        }
        return false;
    }

    public String getMemberBannerText(int userId) {
        try {
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT is_member, membership_tier, membership_expiry FROM users WHERE id = ?");
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isMember = rs.getBoolean("is_member");
                    String tier = rs.getString("membership_tier");
                    Date expiry = rs.getDate("membership_expiry");
                    if (isMember && expiry != null && !expiry.toLocalDate().isBefore(LocalDate.now())) {
                        return "Status Akun: " + tier + " | Aktif sampai: " + expiry + " (Otomatis Diskon 15%)";
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ambil banner member: " + e.getMessage());
        }
        return "Status Akun: REGULER (Tarif Normal)";
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}