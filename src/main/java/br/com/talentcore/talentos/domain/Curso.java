package br.com.talentcore.talentos.domain;

import java.time.LocalDate;

public class Curso {
    private String id;
    private String candidatoId;
    private String nome;
    private String instituicao;
    private Integer cargaHoraria;
    private LocalDate dataConclusao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public Integer getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(Integer cargaHoraria) { this.cargaHoraria = cargaHoraria; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }
}