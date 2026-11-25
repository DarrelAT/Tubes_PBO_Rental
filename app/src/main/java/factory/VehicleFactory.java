package factory;
import models.Car;
import models.Motorcycle;
import models.Vehicle;

public class VehicleFactory {
    public static Vehicle createVehicle(int id, String brand, String model, double price, String status, String type, int doors, String helmet) {
        if ("CAR".equalsIgnoreCase(type)) {
            return new Car(id, brand, model, price, status, doors);
        } else if ("MOTORCYCLE".equalsIgnoreCase(type)) {
            return new Motorcycle(id, brand, model, price, status, helmet);
        }
        throw new IllegalArgumentException("Tipe kendaraan tidak dikenal: " + type);
    }
}