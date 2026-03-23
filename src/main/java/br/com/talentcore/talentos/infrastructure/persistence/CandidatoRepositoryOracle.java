package br.com.talentcore.talentos.infrastructure.persistence;

import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.config.DatabaseConfig;
import br.com.talentcore.talentos.domain.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.Locale;

public class CandidatoRepositoryOracle implements CandidatoRepository {

    // ============================= Contrato básico =============================

    @Override
    public boolean existsByEmail(String email) {
        throw new UnsupportedOperationException("Oracle: existsByEmail ainda não implementado");
    }

    @Override
    public String salvar(Candidato c) {
        Objects.requireNonNull(c, "candidato não pode ser nulo");
        if (c.getId() == null || c.getId().isBlank()) c.setId(UUID.randomUUID().toString());

        final String sql = """
            INSERT INTO CANDIDATO (
                ID, NOME_COMPLETO, DATA_NASCIMENTO,
                EMAIL, TELEFONE,
                CIDADE, UF, BAIRRO, PAIS
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getId());
            ps.setString(2, c.getNomeCompleto());
            setDateOrNull(ps, 3, c.getDataNascimento());

            Contato contato = c.getContato();
            ps.setString(4, contato != null ? contato.getEmail() : null);
            ps.setString(5, contato != null ? contato.getTelefone() : null);

            Endereco end = c.getEndereco();
            ps.setString(6, end != null ? end.getCidade() : null);
            ps.setString(7, end != null ? end.getEstado() : null); // UF
            ps.setString(8, end != null ? end.getBairro() : null);
            ps.setString(9, end != null ? end.getPais()   : null);

            int updated = ps.executeUpdate();
            if (updated != 1) throw new RuntimeException("Falha ao inserir candidato: " + updated);
            return c.getId();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar candidato no Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Candidato> buscarPorId(String id) {
        Objects.requireNonNull(id, "id não pode ser nulo");

        final String sql = """
            SELECT
                c.ID,
                c.NOME_COMPLETO,
                c.DATA_NASCIMENTO,
                c.EMAIL,
                c.TELEFONE,
                c.CIDADE,
                c.UF,
                c.BAIRRO,
                c.PAIS,
                c.OCUPACAO,
                c.RESUMO_PROFISSIONAL,
                c.LINKEDIN,
                c.GITHUB,
                c.PORTFOLIO,
                c.FOTO_URL,
                c.NACIONALIDADE,
                c.ESTADO_CIVIL,
                c.PRETENSAO_SALARIAL,
                c.HORARIOS,
                c.ACEITA_VIAGENS,
                c.ACEITA_MUDANCA
            FROM CANDIDATO c
            WHERE c.ID = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Candidato cand = mapRow(rs);

                // Coleções carregadas no detalhe
                cand.setIdiomas(buscarIdiomas(conn, id));
                cand.setHabilidadesTecnicas(buscarHabilidades(conn, id));
                cand.setHabilidadesComportamentais(buscarSoftSkills(conn, id));
                cand.setExperiencias(buscarExperiencias(conn, id));
                cand.setProjetos(buscarProjetos(conn, id)); // <<< NOVO

                return Optional.of(cand);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar candidato por ID no Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Candidato c) {
        Objects.requireNonNull(c, "candidato não pode ser nulo");
        if (c.getId() == null || c.getId().isBlank())
            throw new IllegalArgumentException("ID do candidato é obrigatório para atualização.");

        final String sql = """
            UPDATE CANDIDATO
               SET NOME_COMPLETO   = ?,
                   DATA_NASCIMENTO = ?,
                   EMAIL           = ?,
                   TELEFONE        = ?,
                   CIDADE          = ?,
                   UF              = ?,
                   BAIRRO          = ?,
                   PAIS            = ?
             WHERE ID = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNomeCompleto());
            setDateOrNull(ps, 2, c.getDataNascimento());

            Contato contato = c.getContato();
            ps.setString(3, contato != null ? contato.getEmail() : null);
            ps.setString(4, contato != null ? contato.getTelefone() : null);

            Endereco end = c.getEndereco();
            ps.setString(5, end != null ? end.getCidade() : null);
            ps.setString(6, end != null ? end.getEstado() : null);
            ps.setString(7, end != null ? end.getBairro() : null);
            ps.setString(8, end != null ? end.getPais()   : null);

            ps.setString(9, c.getId());

            int updated = ps.executeUpdate();
            if (updated != 1) throw new RuntimeException("Falha ao atualizar candidato: " + updated);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar candidato no Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizarCurriculo(Candidato c) {
        Objects.requireNonNull(c, "candidato não pode ser nulo");
        if (c.getId() == null || c.getId().isBlank())
            throw new IllegalArgumentException("ID do candidato é obrigatório para atualização de currículo.");

        final String sql = """
            UPDATE CANDIDATO SET
                OCUPACAO            = ?,
                RESUMO_PROFISSIONAL = ?,
                LINKEDIN            = ?,
                GITHUB              = ?,
                PORTFOLIO           = ?,
                NACIONALIDADE       = ?,
                ESTADO_CIVIL        = ?,
                PRETENSAO_SALARIAL  = ?,
                BAIRRO              = ?,
                CIDADE              = ?,
                UF                  = ?,
                HORARIOS            = ?,
                ACEITA_VIAGENS      = ?,
                ACEITA_MUDANCA      = ?,
                FOTO_URL            = ?
            WHERE ID = ?
            """;

        String aceViagensSN  = null;
        String aceMudancaSN  = null;
        String horarios      = null;

        if (c.getDisponibilidade() != null) {
            Disponibilidade d = c.getDisponibilidade();
            aceViagensSN = boolToSN(getBooleanSafe(d, true));
            aceMudancaSN = boolToSN(getBooleanSafe(d, false));
            horarios     = d.getHorarios();
        }

        Endereco end = c.getEndereco();

        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean auto = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                // Update campos simples
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, c.getOcupacao());
                    ps.setString(i++, c.getResumoProfissional());
                    ps.setString(i++, c.getLinkedin());
                    ps.setString(i++, c.getGithub());
                    ps.setString(i++, c.getPortfolio());
                    ps.setString(i++, c.getNacionalidade());
                    ps.setString(i++, c.getEstadoCivil());
                    ps.setString(i++, c.getPretensaoSalarial());
                    ps.setString(i++, end != null ? end.getBairro() : null);
                    ps.setString(i++, end != null ? end.getCidade() : null);
                    ps.setString(i++, end != null ? end.getEstado() : null); // UF
                    ps.setString(i++, horarios);
                    ps.setString(i++, aceViagensSN);
                    ps.setString(i++, aceMudancaSN);
                    ps.setString(i++, c.getFotoUrl());
                    ps.setString(i++, c.getId());

                    int updated = ps.executeUpdate();
                    if (updated != 1) throw new RuntimeException("Falha ao atualizar currículo: " + updated);
                }

                // SINCRONIZA coleções
                sincronizarIdiomas(conn, c);
                sincronizarHabilidades(conn, c);
                sincronizarSoftSkills(conn, c);
                sincronizarExperiencias(conn, c);
                sincronizarProjetos(conn, c); // <<< NOVO

                conn.commit();
            } catch (SQLException ex) {
                try { conn.rollback(); } catch (SQLException ignore) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(auto); } catch (SQLException ignore) {}
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar currículo do candidato no Oracle: " + e.getMessage(), e);
        }
    }

    public void excluir(String id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        final String sql = "DELETE FROM CANDIDATO WHERE ID = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int updated = ps.executeUpdate();
            if (updated != 1) throw new RuntimeException("Falha ao excluir candidato: " + updated);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir candidato no Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Candidato> buscarPorFiltros(
            String tecnologia,
            String nivel,
            String cidade,
            String estado,
            String idioma,
            String nivelIdioma
    ) {
        final String sql = """
            SELECT
                c.ID,
                c.NOME_COMPLETO,
                c.DATA_NASCIMENTO,
                c.EMAIL,
                c.TELEFONE,
                c.CIDADE,
                c.UF,
                c.BAIRRO,
                c.PAIS,
                c.OCUPACAO,
                c.RESUMO_PROFISSIONAL,
                c.LINKEDIN,
                c.GITHUB,
                c.PORTFOLIO,
                c.FOTO_URL,
                c.NACIONALIDADE,
                c.ESTADO_CIVIL,
                c.PRETENSAO_SALARIAL,
                c.HORARIOS,
                c.ACEITA_VIAGENS,
                c.ACEITA_MUDANCA
            FROM CANDIDATO c
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Candidato> out = new ArrayList<>();
            while (rs.next()) {
                Candidato cand = mapRow(rs);

                // Carrega coleções essenciais para exibição
                cand.setIdiomas(buscarIdiomas(conn, cand.getId()));
                cand.setHabilidadesTecnicas(buscarHabilidades(conn, cand.getId()));
                cand.setHabilidadesComportamentais(buscarSoftSkills(conn, cand.getId()));
                cand.setExperiencias(buscarExperiencias(conn, cand.getId()));
                cand.setProjetos(buscarProjetos(conn, cand.getId())); // <<< NOVO

                out.add(cand);
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar candidatos (Oracle): " + e.getMessage(), e);
        }
    }

    // ============================= Helpers básicos =============================

    private void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.DATE);
        else ps.setDate(index, Date.valueOf(value));
    }

    private Candidato mapRow(ResultSet rs) throws SQLException {
        Candidato cand = new Candidato();
        cand.setId(rs.getString("ID"));
        cand.setNomeCompleto(rs.getString("NOME_COMPLETO"));

        Date dt = rs.getDate("DATA_NASCIMENTO");
        if (dt != null) cand.setDataNascimento(dt.toLocalDate());

        // Contato
        String email = rs.getString("EMAIL");
        String telefone = safeGet(rs, "TELEFONE");
        if (email != null || telefone != null) {
            Contato contato = new Contato();
            contato.setEmail(email);
            contato.setTelefone(telefone);
            cand.setContato(contato);
        }

        // Endereço
        String cidade = rs.getString("CIDADE");
        String uf     = rs.getString("UF");
        String bairro = safeGet(rs, "BAIRRO");
        String pais   = safeGet(rs, "PAIS");
        if (cidade != null || uf != null || bairro != null || pais != null) {
            Endereco end = new Endereco();
            end.setCidade(cidade);
            end.setEstado(uf);
            end.setBairro(bairro);
            end.setPais(pais);
            cand.setEndereco(end);
        }

        // Currículo e dados pessoais
        cand.setOcupacao(safeGet(rs, "OCUPACAO"));
        cand.setResumoProfissional(safeGet(rs, "RESUMO_PROFISSIONAL"));
        cand.setLinkedin(safeGet(rs, "LINKEDIN"));
        cand.setGithub(safeGet(rs, "GITHUB"));
        cand.setPortfolio(safeGet(rs, "PORTFOLIO"));

        cand.setFotoUrl(safeGet(rs, "FOTO_URL"));
        cand.setNacionalidade(safeGet(rs, "NACIONALIDADE"));
        cand.setEstadoCivil(safeGet(rs, "ESTADO_CIVIL"));
        cand.setPretensaoSalarial(safeGet(rs, "PRETENSÃO_SALARIAL")); // pode estar sem acento no seu schema
        if (cand.getPretensaoSalarial() == null) {
            cand.setPretensaoSalarial(safeGet(rs, "PRETENSAO_SALARIAL"));
        }

        // Disponibilidade
        String aceViagens = safeGet(rs, "ACEITA_VIAGENS");
        String aceMudanca = safeGet(rs, "ACEITA_MUDANCA");
        String horarios   = safeGet(rs, "HORARIOS");
        if (aceViagens != null || aceMudanca != null || horarios != null) {
            Disponibilidade d = new Disponibilidade();
            d.setHorarios(horarios);
            d.setAceitaViagens(snToBoolean(aceViagens));
            d.setAceitaMudanca(snToBoolean(aceMudanca));
            cand.setDisponibilidade(d);
        }

        return cand;
    }

    private String safeGet(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException ignore) { return null; }
    }

    private String boolToSN(Boolean b) {
        if (b == null) return null;
        return Boolean.TRUE.equals(b) ? "S" : "N";
    }

    private Boolean snToBoolean(String v) {
        if (v == null) return null;
        if ("S".equalsIgnoreCase(v)) return Boolean.TRUE;
        if ("N".equalsIgnoreCase(v)) return Boolean.FALSE;
        return null;
    }

    private Boolean getBooleanSafe(Disponibilidade d, boolean viagens) {
        try {
            if (viagens) {
                try { return (Boolean) Disponibilidade.class.getMethod("getAceitaViagensNullable").invoke(d); }
                catch (NoSuchMethodException ignore) { }
                return (Boolean) d.getClass().getMethod("isAceitaViagens").invoke(d);
            } else {
                try { return (Boolean) Disponibilidade.class.getMethod("getAceitaMudancaNullable").invoke(d); }
                catch (NoSuchMethodException ignore) { }
                return (Boolean) d.getClass().getMethod("isAceitaMudanca").invoke(d);
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ============================= Idiomas =============================

    private List<Idioma> buscarIdiomas(Connection conn, String candidatoId) throws SQLException {
        String nomeCol = resolveNomeCol(conn, "IDIOMA", "NOME", "IDIOMA");
        final String sql = "SELECT ID, CANDIDATO_ID, " + nomeCol + ", NIVEL FROM IDIOMA WHERE CANDIDATO_ID = ? ORDER BY " + nomeCol;

        List<Idioma> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, candidatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Idioma idioma = new Idioma();
                    idioma.setIdioma(rs.getString(nomeCol));
                    String nivel = rs.getString("NIVEL");
                    if (nivel != null && !nivel.isBlank()) {
                        try { idioma.setNivel(NivelIdioma.valueOf(nivel.trim().toUpperCase(Locale.ROOT))); }
                        catch (IllegalArgumentException ignore) { }
                    }
                    out.add(idioma);
                }
            }
        }
        return out;
    }

    private void sincronizarIdiomas(Connection conn, Candidato c) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM IDIOMA WHERE CANDIDATO_ID = ?")) {
            del.setString(1, c.getId());
            del.executeUpdate();
        }
        if (c.getIdiomas() == null || c.getIdiomas().isEmpty()) return;

        String nomeCol = resolveNomeCol(conn, "IDIOMA", "NOME", "IDIOMA");
        final String insSql = "INSERT INTO IDIOMA (ID, CANDIDATO_ID, " + nomeCol + ", NIVEL) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ins = conn.prepareStatement(insSql)) {
            for (Idioma i : c.getIdiomas()) {
                if (i == null || i.getIdioma() == null || i.getIdioma().isBlank()) continue;
                ins.setString(1, UUID.randomUUID().toString());
                ins.setString(2, c.getId());
                ins.setString(3, i.getIdioma().trim());
                ins.setString(4, i.getNivel() != null ? i.getNivel().name() : null);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    // ============================= Habilidades (técnicas) =============================

    private List<Habilidade> buscarHabilidades(Connection conn, String candidatoId) throws SQLException {
        final String sql = """
            SELECT ID, CANDIDATO_ID, NOME, CATEGORIA, NIVEL
            FROM HABILIDADE
            WHERE CANDIDATO_ID = ?
            ORDER BY NOME
            """;
        List<Habilidade> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, candidatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Habilidade h = new Habilidade();
                    h.setNome(rs.getString("NOME"));
                    h.setCategoria(safeGet(rs, "CATEGORIA"));
                    String nivel = safeGet(rs, "NIVEL");
                    if (nivel != null && !nivel.isBlank()) {
                        try { h.setNivel(NivelConhecimento.valueOf(nivel.trim().toUpperCase(Locale.ROOT))); }
                        catch (IllegalArgumentException ignore) { }
                    }
                    out.add(h);
                }
            }
        }
        return out;
    }

    private void sincronizarHabilidades(Connection conn, Candidato c) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM HABILIDADE WHERE CANDIDATO_ID = ?")) {
            del.setString(1, c.getId());
            del.executeUpdate();
        }
        if (c.getHabilidadesTecnicas() == null || c.getHabilidadesTecnicas().isEmpty()) return;

        final String ins = "INSERT INTO HABILIDADE (ID, CANDIDATO_ID, NOME, CATEGORIA, NIVEL) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            for (Habilidade h : c.getHabilidadesTecnicas()) {
                if (h == null || h.getNome() == null || h.getNome().isBlank()) continue;
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, c.getId());
                ps.setString(3, h.getNome().trim());
                ps.setString(4, h.getCategoria());
                ps.setString(5, h.getNivel() != null ? h.getNivel().name() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ============================= Soft skills =============================

    private List<SoftSkill> buscarSoftSkills(Connection conn, String candidatoId) throws SQLException {
        final String sql = """
            SELECT ID, CANDIDATO_ID, NOME
            FROM SOFT_SKILL
            WHERE CANDIDATO_ID = ?
            ORDER BY NOME
            """;
        List<SoftSkill> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, candidatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SoftSkill s = new SoftSkill();
                    s.setNome(rs.getString("NOME"));
                    out.add(s);
                }
            }
        }
        return out;
    }

    private void sincronizarSoftSkills(Connection conn, Candidato c) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM SOFT_SKILL WHERE CANDIDATO_ID = ?")) {
            del.setString(1, c.getId());
            del.executeUpdate();
        }
        if (c.getHabilidadesComportamentais() == null || c.getHabilidadesComportamentais().isEmpty()) return;

        final String ins = "INSERT INTO SOFT_SKILL (ID, CANDIDATO_ID, NOME) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            for (SoftSkill s : c.getHabilidadesComportamentais()) {
                if (s == null || s.getNome() == null || s.getNome().isBlank()) continue;
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, c.getId());
                ps.setString(3, s.getNome().trim());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ============================= Experiências + Tecnologias =============================

    private List<Experiencia> buscarExperiencias(Connection conn, String candidatoId) throws SQLException {
        final String sql = """
            SELECT ID, CANDIDATO_ID, EMPRESA, CARGO, TIPO, DATA_INICIO, DATA_FIM
            FROM EXPERIENCIA
            WHERE CANDIDATO_ID = ?
            ORDER BY DATA_INICIO DESC NULLS LAST, EMPRESA
            """;
        List<Experiencia> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, candidatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String expId = rs.getString("ID");
                    Experiencia e = new Experiencia();
                    e.setEmpresa(rs.getString("EMPRESA"));
                    e.setCargo(rs.getString("CARGO"));
                    String tipo = safeGet(rs, "TIPO");
                    if (tipo != null && !tipo.isBlank()) {
                        try { e.setTipo(TipoContratacao.valueOf(tipo.trim().toUpperCase(Locale.ROOT))); }
                        catch (IllegalArgumentException ignore) { }
                    }
                    Date di = rs.getDate("DATA_INICIO");
                    Date df = rs.getDate("DATA_FIM");
                    if (di != null) e.setDataInicio(di.toLocalDate());
                    if (df != null) e.setDataFim(df.toLocalDate());

                    // Tecnologias da experiência
                    e.setTecnologias(buscarTecnologiasExperiencia(conn, expId));

                    // (Opcional) Lista de realizações – manter vazio por ora
                    e.setRealizacoes(Collections.emptyList());

                    out.add(e);
                }
            }
        }
        return out;
    }

    private List<String> buscarTecnologiasExperiencia(Connection conn, String experienciaId) throws SQLException {
        String nomeCol = resolveNomeCol(conn, "EXPERIENCIA_TECNOLOGIA", "TECNOLOGIA", "NOME");
        final String sql = "SELECT " + nomeCol + " FROM EXPERIENCIA_TECNOLOGIA WHERE EXPERIENCIA_ID = ? ORDER BY " + nomeCol;
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, experienciaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    private void sincronizarExperiencias(Connection conn, Candidato c) throws SQLException {
        // Apaga tecnologias das experiências do candidato e depois as experiências
        try (PreparedStatement delT = conn.prepareStatement(
                "DELETE FROM EXPERIENCIA_TECNOLOGIA WHERE EXPERIENCIA_ID IN (SELECT ID FROM EXPERIENCIA WHERE CANDIDATO_ID = ?)");
             PreparedStatement delE = conn.prepareStatement(
                     "DELETE FROM EXPERIENCIA WHERE CANDIDATO_ID = ?")) {

            delT.setString(1, c.getId()); delT.executeUpdate();
            delE.setString(1, c.getId()); delE.executeUpdate();
        }
        if (c.getExperiencias() == null || c.getExperiencias().isEmpty()) return;

        final String insExp = "INSERT INTO EXPERIENCIA (ID, CANDIDATO_ID, EMPRESA, CARGO, TIPO, DATA_INICIO, DATA_FIM) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String nomeCol = resolveNomeCol(conn, "EXPERIENCIA_TECNOLOGIA", "TECNOLOGIA", "NOME");
        final String insTec = "INSERT INTO EXPERIENCIA_TECNOLOGIA (ID, EXPERIENCIA_ID, " + nomeCol + ") VALUES (?, ?, ?)";

        for (Experiencia e : c.getExperiencias()) {
            if (e == null) continue;
            String expId = UUID.randomUUID().toString();

            try (PreparedStatement ps = conn.prepareStatement(insExp)) {
                int i = 1;
                ps.setString(i++, expId);
                ps.setString(i++, c.getId());
                ps.setString(i++, e.getEmpresa());
                ps.setString(i++, e.getCargo());
                ps.setString(i++, e.getTipo() != null ? e.getTipo().name() : null);
                if (e.getDataInicio() != null) ps.setDate(i++, Date.valueOf(e.getDataInicio())); else ps.setNull(i++, java.sql.Types.DATE);
                if (e.getDataFim() != null)    ps.setDate(i++, Date.valueOf(e.getDataFim()));    else ps.setNull(i++, java.sql.Types.DATE);
                ps.executeUpdate();
            }

            if (e.getTecnologias() != null && !e.getTecnologias().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insTec)) {
                    for (String t : e.getTecnologias()) {
                        if (t == null || t.isBlank()) continue;
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, expId);
                        ps.setString(3, t.trim());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    // ============================= Projetos + Tecnologias =============================

    private List<Projeto> buscarProjetos(Connection conn, String candidatoId) throws SQLException {
        // Seleciona colunas garantidas; descricao/url serão lidas via safeGet (podem não existir no SELECT)
        final String sql = """
            SELECT ID, CANDIDATO_ID, NOME, DATA_INICIO, DATA_FIM
            FROM PROJETO
            WHERE CANDIDATO_ID = ?
            ORDER BY DATA_INICIO DESC NULLS LAST, NOME
            """;
        List<Projeto> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, candidatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String projId = rs.getString("ID");
                    Projeto p = new Projeto();
                    p.setNome(rs.getString("NOME"));
                    // Campos opcionais (podem não existir no schema; safeGet evita falha)
                    p.setDescricao(safeGet(rs, "DESCRICAO"));
                    p.setUrl(safeGet(rs, "URL"));

                    Date di = rs.getDate("DATA_INICIO");
                    Date df = rs.getDate("DATA_FIM");
                    if (di != null) p.setDataInicio(di.toLocalDate());
                    if (df != null) p.setDataFim(df.toLocalDate());

                    p.setTecnologias(buscarTecnologiasProjeto(conn, projId));
                    out.add(p);
                }
            }
        }
        return out;
    }

    private List<String> buscarTecnologiasProjeto(Connection conn, String projetoId) throws SQLException {
        String nomeCol = resolveNomeCol(conn, "PROJETO_TECNOLOGIA", "TECNOLOGIA", "NOME");
        final String sql = "SELECT " + nomeCol + " FROM PROJETO_TECNOLOGIA WHERE PROJETO_ID = ? ORDER BY " + nomeCol;
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, projetoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    private void sincronizarProjetos(Connection conn, Candidato c) throws SQLException {
        // Remove tecnologias e projetos anteriores do candidato
        try (PreparedStatement delT = conn.prepareStatement(
                "DELETE FROM PROJETO_TECNOLOGIA WHERE PROJETO_ID IN (SELECT ID FROM PROJETO WHERE CANDIDATO_ID = ?)");
             PreparedStatement delP = conn.prepareStatement(
                     "DELETE FROM PROJETO WHERE CANDIDATO_ID = ?")) {

            delT.setString(1, c.getId()); delT.executeUpdate();
            delP.setString(1, c.getId()); delP.executeUpdate();
        }
        if (c.getProjetos() == null || c.getProjetos().isEmpty()) return;

        // Inserção dos projetos (somente colunas garantidas; descricao/url são opcionais)
        final String insProj = "INSERT INTO PROJETO (ID, CANDIDATO_ID, NOME, DATA_INICIO, DATA_FIM) VALUES (?, ?, ?, ?, ?)";
        String nomeCol = resolveNomeCol(conn, "PROJETO_TECNOLOGIA", "TECNOLOGIA", "NOME");
        final String insTec = "INSERT INTO PROJETO_TECNOLOGIA (ID, PROJETO_ID, " + nomeCol + ") VALUES (?, ?, ?)";

        for (Projeto p : c.getProjetos()) {
            if (p == null || p.getNome() == null || p.getNome().isBlank()) continue;
            String projId = UUID.randomUUID().toString();

            // Insere projeto
            try (PreparedStatement ps = conn.prepareStatement(insProj)) {
                int i = 1;
                ps.setString(i++, projId);
                ps.setString(i++, c.getId());
                ps.setString(i++, p.getNome().trim());
                if (p.getDataInicio() != null) ps.setDate(i++, Date.valueOf(p.getDataInicio())); else ps.setNull(i++, java.sql.Types.DATE);
                if (p.getDataFim() != null)    ps.setDate(i++, Date.valueOf(p.getDataFim()));    else ps.setNull(i++, java.sql.Types.DATE);
                ps.executeUpdate();
            }

            // Insere tecnologias do projeto (se houver)
            if (p.getTecnologias() != null && !p.getTecnologias().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insTec)) {
                    for (String t : p.getTecnologias()) {
                        if (t == null || t.isBlank()) continue;
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, projId);
                        ps.setString(3, t.trim());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    // ============================= Metadados =============================

    private boolean hasColumn(Connection conn, String table, String column) throws SQLException {
        final String sql = """
            SELECT 1
              FROM USER_TAB_COLS
             WHERE UPPER(TABLE_NAME)  = ?
               AND UPPER(COLUMN_NAME) = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.toUpperCase(Locale.ROOT));
            ps.setString(2, column.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Resolve dinamicamente a coluna de "nome" (ex.: NOME | IDIOMA | TECNOLOGIA). */
    private String resolveNomeCol(Connection conn, String table, String... candidates) throws SQLException {
        for (String c : candidates) {
            if (hasColumn(conn, table, c)) return c;
        }
        throw new SQLException("Tabela " + table + " não possui nenhuma das colunas: " + Arrays.toString(candidates));
    }
}