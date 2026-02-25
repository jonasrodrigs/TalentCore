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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Contato getContato() {
        return contato;
    }

    public void setContato(Contato contato) {
        this.contato = contato;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getResumoProfissional() {
        return resumoProfissional;
    }

    public void setResumoProfissional(String resumoProfissional) {
        this.resumoProfissional = resumoProfissional;
    }

    public Disponibilidade getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(Disponibilidade disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public String getPretensaoSalarial() {
        return pretensaoSalarial;
    }

    public void setPretensaoSalarial(String pretensaoSalarial) {
        this.pretensaoSalarial = pretensaoSalarial;
    }

    public List<Formacao> getFormacoes() {
        return formacoes;
    }

    public void setFormacoes(List<Formacao> formacoes) {
        this.formacoes = formacoes;
    }

    public List<Experiencia> getExperiencias() {
        return experiencias;
    }

    public void setExperiencias(List<Experiencia> experiencias) {
        this.experiencias = experiencias;
    }

    public List<Habilidade> getHabilidadesTecnicas() {
        return habilidadesTecnicas;
    }

    public void setHabilidadesTecnicas(List<Habilidade> habilidadesTecnicas) {
        this.habilidadesTecnicas = habilidadesTecnicas;
    }

    public List<SoftSkill> getHabilidadesComportamentais() {
        return habilidadesComportamentais;
    }

    public void setHabilidadesComportamentais(List<SoftSkill> habilidadesComportamentais) {
        this.habilidadesComportamentais = habilidadesComportamentais;
    }

    public List<Certificacao> getCertificacoes() {
        return certificacoes;
    }

    public void setCertificacoes(List<Certificacao> certificacoes) {
        this.certificacoes = certificacoes;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public List<Idioma> getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(List<Idioma> idiomas) {
        this.idiomas = idiomas;
    }

    public List<Projeto> getProjetos() {
        return projetos;
    }

    public void setProjetos(List<Projeto> projetos) {
        this.projetos = projetos;
    }

    public List<Conquista> getConquistas() {
        return conquistas;
    }

    public void setConquistas(List<Conquista> conquistas) {
        this.conquistas = conquistas;
    }

    public List<ReferenciaProfissional> getReferencias() {
        return referencias;
    }

    public void setReferencias(List<ReferenciaProfissional> referencias) {
        this.referencias = referencias;
    }

    public List<Anexo> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<Anexo> anexos) {
        this.anexos = anexos;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}