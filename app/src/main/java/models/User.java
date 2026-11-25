package models;

public abstract class User {
    private int id;
    private String username, password, namaLengkap, role;

    public User(int id, String username, String password, String namaLengkap, String role) {
        this.id = id; this.username = username; this.password = password;
        this.namaLengkap = namaLengkap; this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getRole() { return role; }

    public abstract String getDashboardAccess();
}