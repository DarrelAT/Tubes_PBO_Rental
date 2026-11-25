package models;
public class Car extends MotorizedVehicle {
    private int jumlahPintu;
    public Car(int id, String b, String m, double p, String s, int doors) {
        super(id, b, m, p, s); this.jumlahPintu = doors;
    }
    public int getJumlahPintu() { return jumlahPintu; }
    @Override public String getVehicleType() { return "CAR"; }
    @Override public double calculateRentCost(int days) { return getPricePerDay() * days; }
    @Override public void processRental() { setStatus("RENTED"); }
    @Override public boolean needsService() { return getStatus().equals("MAINTENANCE"); }
    @Override public void performService() { setStatus("AVAILABLE"); }
}