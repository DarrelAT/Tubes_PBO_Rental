package repository;

import config.DatabaseConnection;
import factory.VehicleFactory;
import models.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleRepository implements BaseRepository<Vehicle> {
    private Connection conn;

    public VehicleRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    @Override
    public List<Vehicle> getAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getAll Vehicles: " + e.getMessage());
        }
        return vehicles;
    }

    @Override
    public Vehicle getById(int id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicle(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getById Vehicle: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (brand, model, price_per_day, status, vehicle_type, jumlah_pintu, tipe_helm) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicle.getBrand());
            stmt.setString(2, vehicle.getModel());
            stmt.setDouble(3, vehicle.getPricePerDay());
            stmt.setString(4, vehicle.getStatus());
            stmt.setString(5, vehicle.getVehicleType());

            // Handle atribut spesifik turunan
            if (vehicle instanceof models.Car) {
                stmt.setInt(6, ((models.Car) vehicle).getJumlahPintu());
                stmt.setNull(7, java.sql.Types.VARCHAR);
            } else if (vehicle instanceof models.Motorcycle) {
                stmt.setNull(6, java.sql.Types.INTEGER);
                stmt.setString(7, ((models.Motorcycle) vehicle).getTipeHelm());
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error save Vehicle: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET brand = ?, model = ?, price_per_day = ?, status = ?, vehicle_type = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicle.getBrand());
            stmt.setString(2, vehicle.getModel());
            stmt.setDouble(3, vehicle.getPricePerDay());
            stmt.setString(4, vehicle.getStatus());
            stmt.setString(5, vehicle.getVehicleType());
            stmt.setInt(6, vehicle.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error update Vehicle: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error delete Vehicle: " + e.getMessage());
            return false;
        }
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        double price = rs.getDouble("price_per_day");
        String status = rs.getString("status");
        String type = rs.getString("vehicle_type");
        int doors = rs.getInt("jumlah_pintu");
        String helmet = rs.getString("tipe_helm");

        return VehicleFactory.createVehicle(id, brand, model, price, status, type, doors, helmet);
    }
}