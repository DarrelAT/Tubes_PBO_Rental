package models;
public class Admin extends Employee {
    public Admin(int id, String u, String p, String n) { super(id, u, p, n, "ADMIN"); }
    @Override public String getDashboardAccess() { return "Admin Dashboard"; }
}