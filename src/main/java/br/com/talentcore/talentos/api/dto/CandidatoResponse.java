package br.com.talentcore.talentos.api.dto;

import java.util.List;

/**
 * DTO de resposta do Candidato.
 *
 * Estrutura FLAT para facilitar o consumo no frontend (Angular).
 * Datas são expostas como String (ISO yyyy-MM-dd) para formatação no front.
 */
public class CandidatoResponse {

    // ------------------------------------------------------------
    // Identificação
    // ------------------------------------------------------------

    public String id;
    public String nomeCompleto;
    public String dataNascimento; // ISO yyyy-MM-dd

    // ------------------------------------------------------------
    // Contato
    // ------------------------------------------------------------

    public String email;
    public String telefone;

    // ------------------------------------------------------------
    // Endereço (flat)
    // ------------------------------------------------------------

    public String bairro;
    public String cidade;
    public String estado;
    public String pais;

    // ------------------------------------------------------------
    // Currículo (campos universais)
    // ------------------------------------------------------------

    public String ocupacao;
    public String resumoProfissional;

    // ------------------------------------------------------------
    // Links / Mídia
    // ------------------------------------------------------------

    public String linkedin;
    public String github;
    public String portfolio;
    /** URL pública da foto (FOTO_URL) */
    public String fotoUrl;

    // ------------------------------------------------------------
    // Dados pessoais
    // ------------------------------------------------------------

    public String nacionalidade;
    public String estadoCivil;

    // ------------------------------------------------------------
    // Disponibilidade
    // ------------------------------------------------------------

    public Boolean aceitaViagens;
    public Boolean aceitaMudanca;
    public String horarios;
    public String pretensaoSalarial;

    // ------------------------------------------------------------
    // Listas (já usadas no front)
    // ------------------------------------------------------------

    public List<String> habilidades;
    public List<String> tecnologias;
    public List<String> idiomas;

    // ------------------------------------------------------------
    // Estrutura curricular (detalhe)
    // ------------------------------------------------------------

    public List<?> experiencias;
    public List<?> projetos;
}
