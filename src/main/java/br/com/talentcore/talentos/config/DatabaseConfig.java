package br.com.talentcore.talentos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuração de conexão JDBC (Oracle XE 21c / PDB XEPDB1).
 *
 * Observações:
 * - Formato EZConnect recomendado (service name): jdbc:oracle:thin:@//host:1521/XEPDB1
 * - USER criado sem aspas => UPPERCASE (use "TALENTCORE" no Java).
 * - Se a senha foi definida entre aspas, ela é case-sensitive: digite exatamente igual.
 * - Para diagnóstico rápido, use os helpers url(), user() e ping().
 */
public final class DatabaseConfig {

    // === Ajuste aqui, se necessário ===
    private static final String URL  = "jdbc:oracle:thin:@//192.168.101.10:1521/XEPDB1"; // XE 21c (PDB padrão)
    private static final String USER = "TALENTCORE";                                      // seu schema/usuário
    private static final String PASS = "250721";                                   // sua senha exata

    static {
        // Carrega explicitamente o driver; ajuda em ambientes que não fazem auto-discovery
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC do Oracle não encontrado no classpath (ojdbc17.jar).", e);
        }
    }

    private DatabaseConfig() {}

    /**
     * Retorna uma nova conexão (autoCommit padrão do JDBC = true).
     * Para operações transacionais (vários INSERTs/UPDATEs), chame con.setAutoCommit(false).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // =====================
    // Helpers de diagnóstico
    // =====================

    /** Retorna a URL configurada (útil para logs/sanity check). */
    public static String url() { return URL; }

    /** Retorna o usuário configurado (útil para logs/sanity check). */
    public static String user() { return USER; }

    /**
     * Executa um ping simples (SELECT 1 FROM DUAL).
     * Útil para validar credenciais e rede antes de chamar o repositório.
     */
    public static boolean ping() {
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.execute("SELECT 1 FROM DUAL");
            return true;
        } catch (SQLException e) {
            System.err.println("Ping falhou: " + e.getMessage());
            return false;
        }
    }
}