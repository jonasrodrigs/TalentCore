package br.com.talentcore.talentos.infrastructure.persistence;

import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.config.DatabaseConfig;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.Contato;
import br.com.talentcore.talentos.domain.Disponibilidade;
import br.com.talentcore.talentos.domain.Endereco;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class CandidatoRepositoryOracle implements CandidatoRepository {

    private static final String INSERT_CANDIDATO = """
        INSERT INTO CANDIDATO (
          ID, NOME_COMPLETO, DATA_NASCIMENTO,
          TELEFONE, EMAIL,
          LOGRADOURO, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, PAIS, CEP,
          LINKEDIN, GITHUB, PORTFOLIO,
          NACIONALIDADE, ESTADO_CIVIL,
          RESUMO_PROFISSIONAL, PRETENSAO_SALARIAL,
          ACEITA_VIAGENS, ACEITA_MUDANCA, HORARIOS
        ) VALUES (
          ?, ?, ?,
          ?, ?,
          ?, ?, ?, ?, ?, ?, ?, ?,
          ?, ?, ?,
          ?, ?,
          ?, ?,
          ?, ?, ?
        )
        """;

    private static final String SELECT_CANDIDATO_BY_ID = """
        SELECT
          ID, NOME_COMPLETO, DATA_NASCIMENTO,
          TELEFONE, EMAIL,
          LOGRADOURO, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, PAIS, CEP,
          LINKEDIN, GITHUB, PORTFOLIO,
          NACIONALIDADE, ESTADO_CIVIL,
          RESUMO_PROFISSIONAL, PRETENSAO_SALARIAL,
          ACEITA_VIAGENS, ACEITA_MUDANCA, HORARIOS
        FROM CANDIDATO
        WHERE ID = ?
        """;

    @Override
    public String salvar(Candidato c) {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CANDIDATO)) {

            int i = 1;
            ps.setString(i++, c.getId());
            ps.setString(i++, c.getNomeCompleto());
            setDate(ps, i++, c.getDataNascimento());

            Contato contato = c.getContato();
            ps.setString(i++, contato != null ? contato.getTelefone() : null);
            ps.setString(i++, contato != null ? contato.getEmail() : null);

            Endereco e = c.getEndereco();
            ps.setString(i++, e != null ? e.getLogradouro()  : null);
            ps.setString(i++, e != null ? e.getNumero()      : null);
            ps.setString(i++, e != null ? e.getComplemento() : null);
            ps.setString(i++, e != null ? e.getBairro()      : null);
            ps.setString(i++, e != null ? e.getCidade()      : null);
            ps.setString(i++, e != null ? e.getEstado()      : null);
            ps.setString(i++, e != null ? e.getPais()        : null);
            ps.setString(i++, e != null ? e.getCep()         : null);

            ps.setString(i++, c.getLinkedin());
            ps.setString(i++, c.getGithub());
            ps.setString(i++, c.getPortfolio());
            ps.setString(i++, c.getNacionalidade());
            ps.setString(i++, c.getEstadoCivil());
            ps.setString(i++, c.getResumoProfissional());
            ps.setString(i++, c.getPretensaoSalarial());

            Disponibilidade d = c.getDisponibilidade();
            ps.setString(i++, d != null && d.isAceitaViagens() ? "S" : "N");
            ps.setString(i++, d != null && d.isAceitaMudanca() ? "S" : "N");
            ps.setString(i++, d != null ? d.getHorarios() : null);

            ps.executeUpdate();
            return c.getId();

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao salvar candidato: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<Candidato> buscarPorId(String id) {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_CANDIDATO_BY_ID)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Candidato c = new Candidato();
                c.setId(rs.getString("ID"));
                c.setNomeCompleto(rs.getString("NOME_COMPLETO"));
                c.setDataNascimento(getLocalDate(rs, "DATA_NASCIMENTO"));

                Contato contato = new Contato();
                contato.setTelefone(rs.getString("TELEFONE"));
                contato.setEmail(rs.getString("EMAIL"));
                c.setContato(contato);

                Endereco e = new Endereco();
                e.setLogradouro(rs.getString("LOGRADOURO"));
                e.setNumero(rs.getString("NUMERO"));
                e.setComplemento(rs.getString("COMPLEMENTO"));
                e.setBairro(rs.getString("BAIRRO"));
                e.setCidade(rs.getString("CIDADE"));
                e.setEstado(rs.getString("ESTADO"));
                e.setPais(rs.getString("PAIS"));
                e.setCep(rs.getString("CEP"));
                c.setEndereco(e);

                c.setLinkedin(rs.getString("LINKEDIN"));
                c.setGithub(rs.getString("GITHUB"));
                c.setPortfolio(rs.getString("PORTFOLIO"));
                c.setNacionalidade(rs.getString("NACIONALIDADE"));
                c.setEstadoCivil(rs.getString("ESTADO_CIVIL"));
                c.setResumoProfissional(rs.getString("RESUMO_PROFISSIONAL"));
                c.setPretensaoSalarial(rs.getString("PRETENSAO_SALARIAL"));

                Disponibilidade d = new Disponibilidade();
                d.setAceitaViagens("S".equalsIgnoreCase(rs.getString("ACEITA_VIAGENS")));
                d.setAceitaMudanca("S".equalsIgnoreCase(rs.getString("ACEITA_MUDANCA")));
                d.setHorarios(rs.getString("HORARIOS"));
                c.setDisponibilidade(d);

                return Optional.of(c);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar candidato por ID: " + ex.getMessage(), ex);
        }
    }

    @Override
    public java.util.List<Candidato> buscarPorFiltros(
            String tecnologia, String nivel, String cidade, String estado, String idioma, String nivelIdioma) {
        throw new UnsupportedOperationException("buscarPorFiltros ainda não implementado.");
    }

    private static void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) ps.setDate(index, null);
        else ps.setDate(index, Date.valueOf(date));
    }
    private static LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return (d == null) ? null : d.toLocalDate();
    }
}