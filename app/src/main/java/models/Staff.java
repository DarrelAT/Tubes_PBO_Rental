package models;
public class Staff extends Employee {
    public Staff(int id, String u, String p, String n) { super(id, u, p, n, "STAFF"); }
    @Override public String getDashboardAccess() { return "Staff Dashboard"; }
}