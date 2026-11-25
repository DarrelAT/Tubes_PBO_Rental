package models;
public class Customer extends User {
    public Customer(int id, String u, String p, String n) { super(id, u, p, n, "CUSTOMER"); }
    @Override public String getDashboardAccess() { return "Customer Dashboard"; }
}