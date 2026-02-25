package br.com.talentcore.talentos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    // Ajuste para o seu ambiente Oracle:
    private static final String URL  = "jdbc:oracle:thin:@//HOST:1521/ORCLPDB1";
    private static final String USER = "USUARIO";
    private static final String PASS = "SENHA";

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        // Se precisar, habilite o driver explicitamente:
        // Class.forName("oracle.jdbc.OracleDriver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static String url()  { return URL; }
    public static String user() { return USER; }
    public static String pass() { return PASS; }
}