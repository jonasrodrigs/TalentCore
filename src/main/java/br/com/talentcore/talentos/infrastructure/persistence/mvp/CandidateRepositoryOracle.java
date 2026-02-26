package br.com.talentcore.talentos.infrastructure.persistence.mvp;

import br.com.talentcore.talentos.config.DatabaseConfig;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório JDBC para o MVP baseado na tabela CANDIDATE.
 *
 * Compatível com o DDL enviado por você:
 *  - Tabela: CANDIDATE (ID NUMBER PK, FULL_NAME, EMAIL UNIQUE, PHONE, SKILLS CLOB, CREATED_AT default, UPDATED_AT).
 *  - Sequence: SEQ_CANDIDATE_ID
 *  - Triggers: TRG_CANDIDATE_ID (preenche ID no INSERT), TRG_CANDIDATE_UPDATED_AT (preenche UPDATED_AT no UPDATE)
 *  - Índices: IDX_CANDIDATE_EMAIL_LOWER (LOWER(EMAIL)), IDX_CANDIDATE_FULL_NAME (opcional)
 *
 * Observação sobre ID:
 *  - Após o INSERT, recuperamos o ID usando "SELECT SEQ_CANDIDATE_ID.CURRVAL FROM DUAL" na MESMA conexão.
 *  - Isso funciona porque o INSERT foi realizado no mesmo "session context" da Connection.
 */
public class CandidateRepositoryOracle {

    // ===========================
    // DTO simples (aninhado) para o MVP
    // ===========================
    public static class Candidate {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private String skills;         // mapeado a partir do CLOB via getString/setString
        private Instant createdAt;
        private Instant updatedAt;

        public Candidate() {}

        public Candidate(Long id, String fullName, String email, String phone, String skills, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.skills = skills;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }

        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    }

    // ===========================
    // SQLs alinhados ao seu script
    // ===========================

    // existsByEmail (case-insensitive)
    private static final String SQL_EXISTS_BY_EMAIL = """
        SELECT 1
        FROM CANDIDATE
        WHERE LOWER(EMAIL) = LOWER(?)
        FETCH FIRST 1 ROWS ONLY
        """;

    // insert (TRG_CANDIDATE_ID atribui ID; CREATED_AT default configurado na coluna)
    private static final String SQL_INSERT = """
        INSERT INTO CANDIDATE (FULL_NAME, EMAIL, PHONE, SKILLS)
        VALUES (?, ?, ?, ?)
        """;

    // recuperar ID da sessão (após o INSERT)
    private static final String SQL_CURRVAL = "SELECT SEQ_CANDIDATE_ID.CURRVAL FROM DUAL";

    // buscar por id
    private static final String SQL_FIND_BY_ID = """
        SELECT ID, FULL_NAME, EMAIL, PHONE, SKILLS, CREATED_AT, UPDATED_AT
        FROM CANDIDATE
        WHERE ID = ?
        """;

    // listagem paginada
    private static final String SQL_LIST_PAGE = """
        SELECT ID, FULL_NAME, EMAIL, PHONE, SKILLS, CREATED_AT, UPDATED_AT
        FROM CANDIDATE
        ORDER BY ID
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;

    // update (TRG_CANDIDATE_UPDATED_AT cuida do UPDATED_AT)
    private static final String SQL_UPDATE = """
        UPDATE CANDIDATE
        SET FULL_NAME = ?, EMAIL = ?, PHONE = ?, SKILLS = ?
        WHERE ID = ?
        """;

    // delete
    private static final String SQL_DELETE = "DELETE FROM CANDIDATE WHERE ID = ?";

    // ===========================
    // Métodos públicos
    // ===========================

    /** Verifica duplicidade de e-mail (case-insensitive). */
    public boolean existsByEmail(String email) {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTS_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw translate("Erro ao verificar e-mail existente", e);
        }
    }

    /**
     * Salva um candidato.
     * Regras:
     *  - ID é gerado via trigger/sequence no Oracle (não enviar ID);
     *  - Após o INSERT, recupera o ID gerado via CURRVAL (mesma Connection).
     */
    public long save(Candidate c) {
        try (Connection con = DatabaseConfig.getConnection()) {
            // 1) insert
            try (PreparedStatement ps = con.prepareStatement(SQL_INSERT)) {
                ps.setString(1, c.getFullName());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getPhone());
                ps.setString(4, c.getSkills());
                ps.executeUpdate();
            } catch (SQLException e) {
                // ORA-00001 -> violação de unique (provavelmente e-mail)
                if (isUniqueViolation(e)) {
                    throw new RuntimeException("E-mail já cadastrado (violação de unicidade).", e);
                }
                throw e;
            }

            // 2) currval
            try (PreparedStatement ps = con.prepareStatement(SQL_CURRVAL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    c.setId(id);
                    return id;
                }
                throw new SQLException("Falha ao recuperar SEQ_CANDIDATE_ID.CURRVAL após INSERT.");
            }

        } catch (SQLException e) {
            throw translate("Erro ao salvar candidato", e);
        }
    }

    /** Busca por ID. */
    public Optional<Candidate> findById(long id) {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw translate("Erro ao buscar candidato por ID", e);
        }
    }

    /** Lista com paginação (offset/limit). Requer Oracle 12c+. */
    public List<Candidate> listPage(int offset, int limit) {
        List<Candidate> out = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LIST_PAGE)) {

            ps.setInt(1, Math.max(0, offset));
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
            return out;

        } catch (SQLException e) {
            throw translate("Erro ao listar candidatos (paginaçao)", e);
        }
    }

    /** Atualiza FULL_NAME, EMAIL, PHONE, SKILLS pelo ID. */
    public void update(Candidate c) {
        if (c.getId() == null) throw new IllegalArgumentException("ID obrigatório para update.");
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getSkills());
            ps.setLong(5, c.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Nenhum registro atualizado. ID inexistente: " + c.getId());
            }

        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new RuntimeException("E-mail já cadastrado (violação de unicidade).", e);
            }
            throw translate("Erro ao atualizar candidato", e);
        }
    }

    /** Deleta por ID. Retorna true se removeu. */
    public boolean delete(long id) {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw translate("Erro ao deletar candidato", e);
        }
    }

    // ===========================
    // Helpers
    // ===========================

    private Candidate mapRow(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setId(rs.getLong("ID"));
        c.setFullName(rs.getString("FULL_NAME"));
        c.setEmail(rs.getString("EMAIL"));
        c.setPhone(rs.getString("PHONE"));
        c.setSkills(rs.getString("SKILLS"));
        Timestamp created = rs.getTimestamp("CREATED_AT");
        c.setCreatedAt(created != null ? created.toInstant() : null);
        Timestamp updated = rs.getTimestamp("UPDATED_AT");
        c.setUpdatedAt(updated != null ? updated.toInstant() : null);
        return c;
    }

    private boolean isUniqueViolation(SQLException e) {
        // ORA-00001: unique constraint violated
        return "23000".equals(e.getSQLState()) || e.getMessage().toUpperCase().contains("ORA-00001");
    }

    private RuntimeException translate(String msg, SQLException e) {
        return new RuntimeException(msg + " | SQLState=" + e.getSQLState() + " | ErrorCode=" + e.getErrorCode(), e);
    }
}