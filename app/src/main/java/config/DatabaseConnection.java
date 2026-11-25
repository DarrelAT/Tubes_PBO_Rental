package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Properties props = new Properties();
                
                // Membaca file dari folder resources
                InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
                
                if (input == null) {
                    throw new IOException("File db.properties tidak ditemukan di dalam resources!");
                }

                props.load(input);

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");

                connection = DriverManager.getConnection(url, user, pass);
            } catch (IOException | SQLException e) {
                System.err.println("Gagal koneksi database: " + e.getMessage());
            }
        }
        return connection;
    }
}