
package com.banksphere;

import com.banksphere.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class BankSphereApplication {

    public static void main(String[] args) {

        System.out.println("==================================");
        System.out.println("       WELCOME TO BANKSPHERE");
        System.out.println("==================================");

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            if (connection != null) {
                System.out.println("Database Connection Successful!");
                System.out.println("Connected Database: "
                        + connection.getCatalog());
            }

        } catch (SQLException e) {

            System.out.println("Database Connection Failed!");
            e.printStackTrace();

        }

        System.out.println("==================================");
    }
}
