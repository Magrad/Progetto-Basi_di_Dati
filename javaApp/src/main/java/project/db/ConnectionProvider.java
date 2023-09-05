package project.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionProvider {
    private final String username;
    private final String password;
    private final String dbName;

    /**
     * @param username the username used to connect to the database
     * @param password the password used to connect to the database
     * @param dbName the name of the database to connect to
     */
    public ConnectionProvider (final String username, final String password, final String dbName) {
        this.username = username;
        this.password = password;
        this.dbName = dbName;
    }

    /**
     * @return a Connection with the database specified in the class constructor
     * @throws IllegalStateException if the connection could not be established
     */
    public Connection getMySQLConnection() {
        final String dbUri = "jdbc:mysql://localhost:3306/" + this.dbName + "?useSSL=false";
        try {
            // Thanks to the JDBC DriverManager we can get a connection to the database.
            return DriverManager.getConnection(dbUri, this.username, this.password);
        } catch (final SQLException e) {
            // If the database does not exist, we create it.
            if (e.getMessage().contains("Unknown database")) {
                final String dbURL = "jdbc:mysql://localhost:3306";
                try (Connection conn = DriverManager.getConnection(dbURL, this.username, this.password)) {
                    conn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS `" + this.dbName + "`");
                    return DriverManager.getConnection(dbUri, this.username, this.password);
                } catch (final SQLException ex) {
                    throw new IllegalStateException("Could not establish a connection with db", ex);
                }
            }

            throw new IllegalStateException("Could not establish a connection with db", e);
        }
    }
}