package br.com.talentcore.talentos.application.port.out;

import br.com.talentcore.talentos.domain.Candidato;

import java.util.List;
import java.util.Optional;

/**
 * Port de saída da Arquitetura Hexagonal para o agregado Candidato.
 * Implementações: InMemory (perfil mvp) e Oracle JDBC (perfil oracle).
 */
public interface CandidatoRepository {

    boolean existsByEmail(String email);

    String salvar(Candidato c);

    Optional<Candidato> buscarPorId(String id);

    List<Candidato> buscarPorFiltros(String tecnologia,
                                     String nivel,
                                     String cidade,
                                     String estado,
                                     String idioma,
                                     String nivelIdioma);

    /** Atualização do agregado (dados principais como nome, email, endereço mínimo). */
    void atualizar(Candidato candidato);

    /**
     * Atualização do CURRÍCULO e campos complementares do candidato.
     * Deve persistir (quando presentes no domínio):
     *  - OCUPACAO, RESUMO_PROFISSIONAL
     *  - LINKEDIN, GITHUB, PORTFOLIO
     *  - NACIONALIDADE, ESTADO_CIVIL, PRETENSAO_SALARIAL
     *  - Endereço curto: BAIRRO, CIDADE, UF
     *  - Disponibilidade: HORARIOS, ACEITA_VIAGENS, ACEITA_MUDANCA
     *  - FOTO_URL
     */
    void atualizarCurriculo(Candidato candidato);
}