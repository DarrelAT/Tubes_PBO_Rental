package models;
public class Motorcycle extends MotorizedVehicle {
    private String tipeHelm;
    public Motorcycle(int id, String b, String m, double p, String s, String helmet) {
        super(id, b, m, p, s); this.tipeHelm = helmet;
    }
    public String getTipeHelm() { return tipeHelm; }
    @Override public String getVehicleType() { return "MOTORCYCLE"; }
    @Override public double calculateRentCost(int days) { return getPricePerDay() * days; }
    @Override public void processRental() { setStatus("RENTED"); }
    @Override public boolean needsService() { return getStatus().equals("MAINTENANCE"); }
    @Override public void performService() { setStatus("AVAILABLE"); }
}