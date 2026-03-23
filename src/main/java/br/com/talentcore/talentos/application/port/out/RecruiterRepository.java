package br.com.talentcore.talentos.application.port.out;

import br.com.talentcore.talentos.domain.Recruiter;

import java.util.Optional;

/**
 * Port Out para persistência de Recruiter (conta de acesso do recrutador).
 * <p>
 * Responsável por contratos que o Adapter (Oracle JDBC, InMemory, etc.) deve cumprir.
 * Mantém o Application layer desacoplado da tecnologia de banco de dados.
 * <p>
 * Uso no sprint:
 * - signup(): validar e-mail único e criar recruiter com PLAN=FREE e ROLE=RECRUTADOR
 * - me(): buscar dados básicos por id (subject do JWT)
 * - login (futuro): buscar por e-mail para validar senha
 */
public interface RecruiterRepository {

    /**
     * Verifica se já existe um recruiter com o e-mail informado.
     *
     * @param email e-mail normalizado (trim/lowercase) preferencialmente
     * @return true se existir
     */
    boolean existsByEmail(String email);

    /**
     * Cria um novo recruiter.
     *
     * @param recruiter entidade de domínio preenchida (com hash de senha)
     * @return ID gerado pelo banco
     */
    Long create(Recruiter recruiter);

    /**
     * Busca recruiter pelo ID.
     *
     * @param id identificador (normalmente o 'sub' do JWT)
     * @return Optional com o recruiter, se encontrado
     */
    Optional<Recruiter> findById(Long id);

    /**
     * Busca recruiter pelo e-mail.
     *
     * @param email e-mail normalizado
     * @return Optional com o recruiter, se encontrado
     */
    Optional<Recruiter> findByEmail(String email);
}