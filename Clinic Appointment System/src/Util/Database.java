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

    // Run SELECT queries
    public static ResultSet query(String sql) {
        try {
            Statement stat = getConnection().createStatement();
            return stat.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Run INSERT, UPDATE, DELETE
    public static int update(String sql) {
        try {
            Statement stat = getConnection().createStatement();
            return stat.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
