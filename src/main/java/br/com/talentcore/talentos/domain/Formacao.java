
package br.com.talentcore.talentos.domain;

import java.time.LocalDate;

public class Formacao {
    private String id;
    private String candidatoId;
    private String instituicao;
    private String curso;
    private NivelFormacao nivel; // TECNICO, GRADUACAO, POS, MBA, MESTRADO, DOUTORADO
    private LocalDate dataInicio;
    private LocalDate dataFim; // null = cursando
    private String descricao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public NivelFormacao getNivel() { return nivel; }
    public void setNivel(NivelFormacao nivel) { this.nivel = nivel; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
