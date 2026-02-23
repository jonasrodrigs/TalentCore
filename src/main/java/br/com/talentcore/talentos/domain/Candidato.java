package br.com.talentcore.talentos.domain;

import java.util.List;
import java.time.LocalDate;

public class Candidato {
    private String id; // UUID
    private String nomeCompleto;
    private LocalDate dataNascimento;

    private Contato contato;
    private Endereco endereco;
    private String linkedin;
    private String github;
    private String portfolio;
    private String nacionalidade;
    private String estadoCivil;
    private String resumoProfissional;
    private String pretensaoSalarial;
    private Disponibilidade disponibilidade;

    private List<Formacao> formacoes;
    private List<Experiencia> experiencias;
    private List<Habilidade> habilidadesTecnicas;
    private List<SoftSkill> habilidadesComportamentais;
    private List<Certificacao> certificacoes;
    private List<Curso> cursos;
    private List<Idioma> idiomas;
    private List<Projeto> projetos;
    private List<Conquista> conquistas;
    private List<ReferenciaProfissional> referencias;
    private List<Anexo> anexos;

}