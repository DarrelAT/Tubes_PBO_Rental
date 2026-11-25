package models;

public interface Rentalable {
    double calculateRentCost(int days);
    void processRental();
}