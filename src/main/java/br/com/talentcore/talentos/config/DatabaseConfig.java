package br.com.talentcore.talentos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Configuração simples de JDBC para Oracle (ojdbc17).
 * Lê URL/USER/PASSWORD de variáveis de ambiente (ou system properties).

 * ENV esperadas:
 *  - TC_DB_URL       (ex.: jdbc:oracle:thin:@//localhost:1521/XEPDB1)
 *  - TC_DB_USER      (ex.: talentcore)
 *  - TC_DB_PASSWORD  (ex.: talentcore)
 */
public final class DatabaseConfig {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Driver moderno do ojdbc17 dispensa Class.forName, mas não atrapalha se quiser habilitar:
        // try { Class.forName("oracle.jdbc.OracleDriver"); } catch (Throwable ignore) {}

        URL       = firstNonBlank(env("TC_DB_URL"),       prop("TC_DB_URL"));
        USER      = firstNonBlank(env("TC_DB_USER"),      prop("TC_DB_USER"));
        PASSWORD  = firstNonBlank(env("TC_DB_PASSWORD"),  prop("TC_DB_PASSWORD"));

        if (isBlank(URL) || isBlank(USER)) {
            System.err.println("[WARN] DatabaseConfig: TC_DB_URL/TC_DB_USER não configurados. " +
                    "Defina as variáveis para usar o adapter Oracle.");
        }
    }

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        if (isBlank(URL)) {
            throw new SQLException("URL do banco não configurada (TC_DB_URL).");
        }
        if (isBlank(USER)) {
            throw new SQLException("Usuário do banco não configurado (TC_DB_USER).");
        }
        // PASSWORD pode ser vazia dependendo do ambiente.
        return DriverManager.getConnection(URL, USER, Objects.toString(PASSWORD, ""));
    }

    // ------------------ helpers ------------------

    private static String env(String k) { return System.getenv(k); }
    private static String prop(String k) { return System.getProperty(k); }
    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : (!isBlank(b) ? b : null);
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}