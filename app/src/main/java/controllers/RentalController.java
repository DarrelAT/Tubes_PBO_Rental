package controllers;

import config.DatabaseConnection;
import exceptions.VehicleNotAvailableException;
import factory.VehicleFactory;
import models.Car;
import models.Motorcycle;
import models.User;
import models.Vehicle;
import repository.VehicleRepository;
import strategy.MemberRentalStrategy;
import strategy.RentalCalculator;
import strategy.StandardRentalStrategy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalController {
    private final VehicleRepository vehicleRepo;
    private final AuthController authController;

    public RentalController(AuthController authController) {
        this.vehicleRepo = new VehicleRepository();
        this.authController = authController;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepo.getAll();
    }

    public String processCustomerRental(int vehicleId, int days, User user)
            throws VehicleNotAvailableException, SQLException {
        if (days <= 0) {
            throw new IllegalArgumentException("Durasi minimal 1 hari!");
        }

        Vehicle vehicle = vehicleRepo.getById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        }
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new VehicleNotAvailableException("Kendaraan sedang disewa!");
        }

        boolean activeMember = user != null && authController.isActiveMember(user.getId());
        RentalCalculator calculator = activeMember
            ? new RentalCalculator(new MemberRentalStrategy())
            : new RentalCalculator(new StandardRentalStrategy());
        String statusMemberInfo = activeMember ? "Member Aktif - Diskon 15%" : "Tarif Reguler";

        double totalCost = calculator.executeCalculation(days, vehicle.getPricePerDay());

        Connection connDb = DatabaseConnection.getConnection();
        String insertRental = "INSERT INTO rentals (user_id, vehicle_id, total_days, total_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmtRental = connDb.prepareStatement(insertRental)) {
            stmtRental.setInt(1, user.getId());
            stmtRental.setInt(2, vehicleId);
            stmtRental.setInt(3, days);
            stmtRental.setDouble(4, totalCost);
            stmtRental.executeUpdate();
        }

        vehicle.setStatus("RENTED");
        vehicleRepo.update(vehicle);

        return "Sewa Sukses! Total: Rp " + String.format("%,.0f", totalCost) + " (" + statusMemberInfo + ")";
    }

    public String processStaffBooking(int vehicleId, int days, User actor)
            throws VehicleNotAvailableException, SQLException {
        if (actor == null) {
            throw new SecurityException("Sesi login tidak ditemukan.");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Durasi booking minimal 1 hari!");
        }

        Vehicle vehicle = vehicleRepo.getById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        }
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new VehicleNotAvailableException("Kendaraan sedang disewa (RENTED)!");
        }

        RentalCalculator calculator = new RentalCalculator(new StandardRentalStrategy());
        double totalCost = calculator.executeCalculation(days, vehicle.getPricePerDay());

        Connection connDb = DatabaseConnection.getConnection();
        String insertRental = "INSERT INTO rentals (user_id, vehicle_id, total_days, total_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmtRental = connDb.prepareStatement(insertRental)) {
            stmtRental.setInt(1, actor.getId());
            stmtRental.setInt(2, vehicleId);
            stmtRental.setInt(3, days);
            stmtRental.setDouble(4, totalCost);
            stmtRental.executeUpdate();
        }

        vehicle.setStatus("RENTED");
        vehicleRepo.update(vehicle);

        return "Booking sukses (" + days + " hari)! Total: Rp " + String.format("%,.0f", totalCost);
    }

    public void addVehicle(String brand, String model, String priceText, String type, String extra, User actor) {
        if (actor == null || !"ADMIN".equalsIgnoreCase(actor.getRole())) {
            throw new SecurityException("Akses ditolak: hanya ADMIN yang dapat menambah kendaraan.");
        }
        if (brand == null || brand.trim().isEmpty()
                || model == null || model.trim().isEmpty()
                || extra == null || extra.trim().isEmpty()) {
            throw new IllegalArgumentException("Semua field wajib diisi!");
        }

        double price = Double.parseDouble(priceText.trim());
        Vehicle newVehicle;
        if ("CAR".equalsIgnoreCase(type)) {
            int doors = Integer.parseInt(extra.trim());
            newVehicle = VehicleFactory.createVehicle(0, brand.trim(), model.trim(), price, "AVAILABLE", "CAR", doors, null);
        } else {
            newVehicle = VehicleFactory.createVehicle(0, brand.trim(), model.trim(), price, "AVAILABLE", "MOTORCYCLE", 0, extra.trim());
        }

        if (!vehicleRepo.save(newVehicle)) {
            throw new IllegalStateException("Gagal menyimpan ke database.");
        }
    }

    public void unrentVehicle(int vehicleId, User actor) {
        if (actor == null) {
            throw new SecurityException("Sesi login tidak ditemukan.");
        }
        Vehicle v = vehicleRepo.getById(vehicleId);
        if (v == null) {
            throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        }
        v.setStatus("AVAILABLE");
        vehicleRepo.update(v);
    }

    public void deleteVehicle(int vehicleId, User actor) {
        if (actor == null || !"ADMIN".equalsIgnoreCase(actor.getRole())) {
            throw new SecurityException("Akses ditolak: hanya ADMIN yang dapat menghapus kendaraan.");
        }
        if (!vehicleRepo.delete(vehicleId)) {
            throw new IllegalStateException("Gagal menghapus kendaraan.");
        }
    }

    public List<Object[]> getAdminVehicleRows() {
        List<Object[]> rows = new ArrayList<>();
        for (Vehicle v : vehicleRepo.getAll()) {
            String extraInfo = "";
            if (v instanceof Car) {
                extraInfo = "Pintu: " + ((Car) v).getJumlahPintu();
            } else if (v instanceof Motorcycle) {
                extraInfo = "Helm: " + ((Motorcycle) v).getTipeHelm();
            }
            rows.add(new Object[]{
                v.getId(), v.getBrand(), v.getModel(), v.getPricePerDay(), v.getStatus(), v.getVehicleType(), extraInfo
            });
        }
        return rows;
    }

    public List<Object[]> getCustomerVehicleRows() {
        List<Object[]> rows = new ArrayList<>();
        for (Vehicle v : vehicleRepo.getAll()) {
            rows.add(new Object[]{
                v.getId(), v.getBrand(), v.getModel(), v.getPricePerDay(), v.getStatus(), v.getVehicleType()
            });
        }
        return rows;
    }

    public List<Object[]> getRentalHistoryRows() {
        List<Object[]> rows = new ArrayList<>();
        String sqlHistory = "SELECT r.id, u.nama_lengkap, CONCAT(v.brand, ' ', v.model) AS kendaraan, r.total_days, r.total_price, r.rental_date " +
                            "FROM rentals r " +
                            "JOIN users u ON r.user_id = u.id " +
                            "JOIN vehicles v ON r.vehicle_id = v.id " +
                            "ORDER BY r.rental_date DESC";
        try {
            Connection c = DatabaseConnection.getConnection();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sqlHistory)) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_lengkap"),
                        rs.getString("kendaraan"),
                        rs.getInt("total_days"),
                        String.format("%,.0f", rs.getDouble("total_price")),
                        rs.getTimestamp("rental_date")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat riwayat transaksi: " + e.getMessage());
        }
        return rows;
    }

    public List<Object[]> getCustomerListRows() {
        List<Object[]> rows = new ArrayList<>();
        String sqlUsers = "SELECT id, username, nama_lengkap, is_member, membership_tier, membership_expiry FROM users WHERE role = 'CUSTOMER' ORDER BY id ASC";
        try {
            Connection c = DatabaseConnection.getConnection();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sqlUsers)) {
                while (rs.next()) {
                    boolean isMember = rs.getBoolean("is_member");
                    Date exp = rs.getDate("membership_expiry");
                    rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("nama_lengkap"),
                        isMember ? "AKTIF" : "NON-MEMBER",
                        rs.getString("membership_tier"),
                        exp != null ? exp.toString() : "-"
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat daftar customer: " + e.getMessage());
        }
        return rows;
    }
}