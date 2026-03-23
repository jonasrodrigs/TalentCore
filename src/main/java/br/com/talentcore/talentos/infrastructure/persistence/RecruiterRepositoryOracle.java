package br.com.talentcore.talentos.infrastructure.persistence;

import br.com.talentcore.talentos.application.port.out.RecruiterRepository;
import br.com.talentcore.talentos.domain.Recruiter;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 * Adapter de persistência (Oracle JDBC) para Recruiter.
 *
 * Tabela esperada (sugestão):
 *   RECRUITER (
 *     ID           NUMBER(19)       PRIMARY KEY,
 *     NOME         VARCHAR2(200)    NOT NULL,
 *     EMAIL        VARCHAR2(200)    NOT NULL UNIQUE,
 *     SENHA_HASH   VARCHAR2(200)    NOT NULL,
 *     PLAN         VARCHAR2(10)     NOT NULL, -- 'FREE' | 'PRO'
 *     PAIS         VARCHAR2(100)    NOT NULL,
 *     UF           VARCHAR2(10)     NOT NULL,
 *     EMPRESA      VARCHAR2(200)    NULL,
 *     CREATED_AT   TIMESTAMP        DEFAULT SYSTIMESTAMP NOT NULL
 *   );
 *
 * Sequência esperada:
 *   CREATE SEQUENCE RECRUITER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;
 *
 * Observação:
 *  - Usamos SELECT RECRUITER_SEQ.NEXTVAL FROM DUAL para gerar o ID antes do INSERT
 *    (evita dependência de "RETURNING INTO" específico do driver).
 *
 * Integração:
 *  - A criação deste adapter NÃO adiciona anotações Spring (@Repository/@Profile),
 *    para manter o mesmo padrão do projeto (instanciado via BeanConfig).
 */
public class RecruiterRepositoryOracle implements RecruiterRepository {

    private final DataSource dataSource;

    public RecruiterRepositoryOracle(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ============================================================
    // ====================== CRUD BÁSICO =========================
    // ============================================================

    @Override
    public boolean existsByEmail(String email) {
        final String sql = """
                SELECT 1
                  FROM RECRUITER
                 WHERE LOWER(EMAIL) = LOWER(?)
                   AND ROWNUM = 1
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeTrim(email));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro verificando e-mail existente em RECRUITER", e);
        }
    }

    @Override
    public Long create(Recruiter recruiter) {
        if (recruiter == null) throw new IllegalArgumentException("recruiter não pode ser nulo");

        final String insert = """
                INSERT INTO RECRUITER
                  (ID, NOME, EMAIL, SENHA_HASH, PLAN, PAIS, UF, EMPRESA, CREATED_AT)
                VALUES
                  (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long id = nextVal(conn, "RECRUITER_SEQ");

                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    int i = 0;
                    ps.setLong(++i, id);
                    ps.setString(++i, recruiter.getNome());
                    ps.setString(++i, recruiter.getEmail());
                    ps.setString(++i, recruiter.getSenhaHash());
                    ps.setString(++i, normalizePlan(recruiter.getPlan()));
                    ps.setString(++i, recruiter.getPais());
                    ps.setString(++i, recruiter.getUf());
                    ps.setString(++i, recruiter.getEmpresa());
                    ps.executeUpdate();
                }

                conn.commit();
                return id;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao inserir RECRUITER", e);
        }
    }

    @Override
    public Optional<Recruiter> findById(Long id) {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA_HASH, PLAN, PAIS, UF, EMPRESA, CREATED_AT
                  FROM RECRUITER
                 WHERE ID = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar RECRUITER por ID", e);
        }
    }

    @Override
    public Optional<Recruiter> findByEmail(String email) {
        final String sql = """
                SELECT ID, NOME, EMAIL, SENHA_HASH, PLAN, PAIS, UF, EMPRESA, CREATED_AT
                  FROM RECRUITER
                 WHERE LOWER(EMAIL) = LOWER(?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeTrim(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar RECRUITER por e-mail", e);
        }
    }

    // ============================================================
    // ========================= Helpers ==========================
    // ============================================================

    /**
     * Obtém o próximo valor de uma sequência Oracle.
     */
    private Long nextVal(Connection conn, String sequenceName) throws SQLException {
        final String sql = "SELECT " + sequenceName + ".NEXTVAL AS ID FROM DUAL";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("ID");
            }
            throw new SQLException("Falha ao obter NEXTVAL de " + sequenceName);
        }
    }

    /**
     * Mapeia um ResultSet para a entidade Recruiter.
     */
    private Recruiter mapper(ResultSet rs) throws SQLException {
        Recruiter r = new Recruiter();
        r.setId(rs.getLong("ID"));
        r.setNome(rs.getString("NOME"));
        r.setEmail(rs.getString("EMAIL"));
        r.setSenhaHash(rs.getString("SENHA_HASH"));
        r.setPlan(normalizePlan(rs.getString("PLAN")));
        r.setPais(rs.getString("PAIS"));
        r.setUf(rs.getString("UF"));
        r.setEmpresa(rs.getString("EMPRESA"));

        Timestamp ts = rs.getTimestamp("CREATED_AT");
        if (ts != null) r.setCreatedAt(ts.toInstant());

        return r;
    }

    private static String safeTrim(String v) {
        return v == null ? null : v.trim();
    }

    private static String normalizePlan(String plan) {
        if (plan == null) return "FREE";
        String p = plan.trim().toUpperCase();
        return "PRO".equals(p) ? "PRO" : "FREE";
    }

    // ============================================================
    // =============== Exceção de persistência ====================
    // ============================================================

    public static class PersistenceException extends RuntimeException {
        public PersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}