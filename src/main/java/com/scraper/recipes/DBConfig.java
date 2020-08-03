package com.scraper.recipes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConfig {
    public static Connection conDB()
    {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/receptai", "root", "root");
            return con;
        } catch (SQLException ex) {
            System.err.println("ConnectionUtil : " + ex.getMessage());
            return null;
        }
    }
}