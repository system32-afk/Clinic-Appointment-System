package Util;

import java.sql.*;

public class Database {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/medical_consultation_system";
    private static final String USER = "root";
    private static final String PASSWORD = "Patriotism#1";//ENTER YOUR MYSQL PASSWORD HERE


    // Establish connection with database
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected!");

                // Ensure required tables and data exist
                initializeDatabase();
            } catch (ClassNotFoundException e) {
                System.out.println("MySQL JDBC Driver not found!");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("CANNOT CONNECT TO DATABASE!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create doctor_specialization table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS doctor_specialization (" +
                "DoctorID INT NOT NULL, " +
                "SpecializationID INT NOT NULL, " +
                "PRIMARY KEY (DoctorID, SpecializationID), " +
                "FOREIGN KEY (DoctorID) REFERENCES doctor(DoctorID) ON DELETE CASCADE, " +
                "FOREIGN KEY (SpecializationID) REFERENCES specialization(SpecializationID) ON DELETE CASCADE" +
            ")");

            // Insert specializations if not exist
            stmt.execute("INSERT IGNORE INTO specialization (SpecializationName) VALUES " +
                "('Cardiology'), ('Neurology'), ('Oncology'), ('Pediatrics'), ('Orthopedics'), " +
                "('Dermatology'), ('Psychiatry'), ('Radiology'), ('Anesthesiology'), ('Endocrinology')");

            // Add Notes column to procedurerequest if not exists
            try {
                stmt.execute("ALTER TABLE procedurerequest ADD COLUMN Notes TEXT");
            } catch (SQLException e) {
                // Column might already exist, ignore
            }

            // Add Status column to procedurerequest if not exists
            try {
                stmt.execute("ALTER TABLE procedurerequest ADD COLUMN Status ENUM('Pending','In-Progress','Completed','Canceled') DEFAULT 'Pending'");
            } catch (SQLException e) {
                // Column might already exist, ignore
            }

            // Migrate existing data from doctor table to junction table
            stmt.execute("INSERT IGNORE INTO doctor_specialization (DoctorID, SpecializationID) " +
                "SELECT DoctorID, SpecializationID FROM doctor WHERE SpecializationID IS NOT NULL");
        }
    }

    public static ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement pst = getConnection().prepareStatement(sql);

            // Set all parameters
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }

            return pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Run INSERT, UPDATE, DELETE
    public static int update(String sql, Object... params) {
        try {
            PreparedStatement pst = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            return pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Run INSERT and return generated key
    public static int insertAndGetKey(String sql, Object... params) {
        try {
            PreparedStatement pst = getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
