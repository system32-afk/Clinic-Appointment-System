package Util;

import java.sql.*;

public class Database {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/medical_consultation_system";
    private static final String USER = "root";
<<<<<<< Updated upstream
    private static final String PASSWORD = "Sample";//ENTER YOUR MYSQL PASSWORD HERE
=======
    private static final String PASSWORD = "Gabby1024";//ENTER YOUR MYSQL PASSWORD HERE
>>>>>>> Stashed changes


    // Establish connection with database
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected!");
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

}
