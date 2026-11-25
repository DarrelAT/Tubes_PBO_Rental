package models;
public abstract class Vehicle implements Rentalable, Maintainable {
    private int id;
    private String brand, model, status;
    private double pricePerDay;

    public Vehicle(int id, String brand, String model, double pricePerDay, String status) {
        this.id = id; this.brand = brand; this.model = model;
        this.pricePerDay = pricePerDay; this.status = status;
    }
    public int getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPricePerDay() { return pricePerDay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public abstract String getVehicleType();
}