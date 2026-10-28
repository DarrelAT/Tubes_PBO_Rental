                package controllers;

import exceptions.VehicleNotAvailableException;
import models.Rentable;
import models.User;
import models.Vehicle;
import repository.VehicleRepository;

import java.util.List;

public class RentalController {
    private VehicleRepository vehicleRepo;

    public RentalController() {
        this.vehicleRepo = new VehicleRepository();
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepo.getAll();
    }

    public double processRental(int vehicleId, int days, User user) throws VehicleNotAvailableException {
        Vehicle vehicle = vehicleRepo.getById(vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        }

        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new VehicleNotAvailableException("Kendaraan " + vehicle.getBrand() + " " + vehicle.getModel() + " sedang tidak tersedia!");
        }

        // Hitung total via Interface Rentable
        double totalCost = 0;
        if (vehicle instanceof Rentable) {
            totalCost = ((Rentable) vehicle).calculateRentCost(days);
            ((Rentable) vehicle).processRental();
        }

        // Update status di PostgreSQL
        vehicleRepo.update(vehicle);

        return totalCost;
    }
}