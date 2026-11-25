package controllers;
import exceptions.VehicleNotAvailableException;
import models.Rentalable;
import models.User;
import models.Vehicle;
import repository.VehicleRepository;
import java.util.List;

public class RentalController {
    private VehicleRepository vehicleRepo;

    public RentalController() {
        this.vehicleRepo = new VehicleRepository();
    }

    public List<Vehicle> getAllVehicles() { return vehicleRepo.getAll(); }

    public double processRental(int vehicleId, int days, User user) throws VehicleNotAvailableException {
        Vehicle vehicle = vehicleRepo.getById(vehicleId);
        if (vehicle == null) throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new VehicleNotAvailableException("Kendaraan sedang tidak tersedia!");
        }

        double totalCost = 0;
        if (vehicle instanceof Rentalable) {
            totalCost = ((Rentalable) vehicle).calculateRentCost(days);
            ((Rentalable) vehicle).processRental();
        }
        vehicleRepo.update(vehicle);
        return totalCost;
    }
}