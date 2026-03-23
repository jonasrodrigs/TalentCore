package br.com.talentcore.talentos.api.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO de entrada para criação/atualização de Candidato.
 *
 * Observações:
 * - Mantém estrutura original (pública e validável) para não quebrar consumidores atuais.
 * - Campos de currículo foram adicionados como opcionais para alinhar ao schema Oracle:
 *   ocupacao, resumoProfissional, linkedin, github, portfolio, nacionalidade, estadoCivil,
 *   pretensaoSalarial (String), aceitaViagens, aceitaMudanca, horarios, fotoUrl, dataNascimento(ISO).
 */
public class CandidatoRequest {

    // ---------------- Identificação / principais ----------------

    @NotBlank(message = "nomeCompleto é obrigatório")
    @Size(min = 3, max = 120, message = "nomeCompleto deve ter entre 3 e 120 caracteres")
    public String nomeCompleto;

    /**
     * Data de nascimento em ISO-8601 (YYYY-MM-DD).
     * Opcional para compatibilidade; o backend pode converter para LocalDate.
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "dataNascimento deve estar no formato YYYY-MM-DD")
    public String dataNascimento;

    @NotNull(message = "contato é obrigatório")
    public ContatoDTO contato;

    @NotNull(message = "endereco é obrigatório")
    public EnderecoDTO endereco;

    // ---------------- Currículo (campos simples) ----------------

    /** Campo universal: cargo/função/ofício (OCUPACAO) */
    @Size(max = 120)
    public String ocupacao;

    /** RESUMO_PROFISSIONAL (CLOB) */
    public String resumoProfissional;

    @Size(max = 300) public String linkedin;
    @Size(max = 300) public String github;
    @Size(max = 300) public String portfolio;

    /** URL pública da foto (FOTO_URL VARCHAR2(500)) */
    @Size(max = 500)
    public String fotoUrl;

    // ---------------- Dados pessoais ----------------

    /** NACIONALIDADE (VARCHAR2(80)) */
    @Size(max = 80)
    public String nacionalidade;

    /** ESTADO_CIVIL (VARCHAR2(50)) */
    @Size(max = 50)
    public String estadoCivil;

    // ---------------- Objetivo / disponibilidade ----------------

    /**
     * PRETENSAO_SALARIAL no Oracle é VARCHAR2(50), então aqui mantemos String.
     * (Se necessário, validar numericamente em outra camada.)
     */
    @Size(max = 50)
    public String pretensaoSalarial;

    /** Mapeia para CHAR(1): 'S'|'N'|NULL */
    public Boolean aceitaViagens;

    /** Mapeia para CHAR(1): 'S'|'N'|NULL */
    public Boolean aceitaMudanca;

    /** HORARIOS (VARCHAR2(255)) */
    @Size(max = 255)
    public String horarios;

    // ---------------- Estrutura curricular (coleções) ----------------

    public List<IdiomaDTO> idiomas;
    public List<HabilidadeDTO> habilidadesTecnicas;
    public List<ExperienciaDTO> experiencias;
    public List<ProjetoDTO> projetos;

    // ========================== Tipos internos ==========================

    public static class ContatoDTO {
        @Pattern(regexp = "^[0-9\\-\\s+()]{8,20}$", message = "telefone inválido")
        public String telefone;

        @NotBlank @Email
        public String email;
    }

    public static class EnderecoDTO {
        @Size(max = 120) public String logradouro;
        @Size(max = 20)  public String numero;
        @Size(max = 80)  public String complemento;
        @Size(max = 80)  public String bairro;

        @NotBlank public String cidade;
        @NotBlank public String estado; // Nota: no Oracle atual usamos UF; o mapeamento pode convergir depois.
        @NotBlank public String pais;

        @Size(max = 12) public String cep;
    }

    public static class IdiomaDTO {
        @NotBlank public String idioma; // ex.: "Inglês B2", "Espanhol B1", etc.
        @NotBlank public String nivel;  // BASICO, INTERMEDIARIO, AVANCADO, FLUENTE, NATIVO (ou livre)
    }

    public static class HabilidadeDTO {
        @NotBlank public String nome;
        @NotBlank public String categoria;
        @NotBlank public String nivel; // BASICO|INTERMEDIARIO|AVANCADO|EXPERT
    }

    public static class ExperienciaDTO {
        @NotBlank public String empresa;
        @NotBlank public String cargo;
        @NotBlank public String tipo; // CLT|PJ|ESTAGIO|FREELANCE

        // ISO-8601: YYYY-MM-DD
        public String dataInicio;
        public String dataFim;

        public List<String> tecnologias;
    }

    public static class ProjetoDTO {
        @NotBlank public String nome;
        public String dataInicio;
        public String dataFim;
        public List<String> tecnologias;
    }
}