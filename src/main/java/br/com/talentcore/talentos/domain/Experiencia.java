package br.com.talentcore.talentos.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Experiencia {
    private String id;
    private String candidatoId;
    private String empresa;
    private String cargo;
    private TipoContratacao tipo; // CLT, PJ, ESTAGIO, FREELANCE
    private LocalDate dataInicio;
    private LocalDate dataFim; // null = atual
    private String descricaoAtividades;

    private List<String> tecnologias = new ArrayList<>();
    private List<String> realizacoes = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public TipoContratacao getTipo() { return tipo; }
    public void setTipo(TipoContratacao tipo) { this.tipo = tipo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getDescricaoAtividades() { return descricaoAtividades; }
    public void setDescricaoAtividades(String descricaoAtividades) { this.descricaoAtividades = descricaoAtividades; }
    public List<String> getTecnologias() { return tecnologias; }
    public void setTecnologias(List<String> tecnologias) { this.tecnologias = tecnologias != null ? tecnologias : new ArrayList<>(); }
    public List<String> getRealizacoes() { return realizacoes; }
    public void setRealizacoes(List<String> realizacoes) { this.realizacoes = realizacoes != null ? realizacoes : new ArrayList<>(); }
}