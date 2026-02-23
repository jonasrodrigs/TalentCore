package br.com.talentcore.talentos.domain;

import java.time.LocalDate;

public class Certificacao {
    private String id;
    private String candidatoId;
    private String nome;
    private String instituicao;
    private LocalDate dataObtencao;
    private LocalDate dataExpiracao; // null = não expirq
    private String numeroCredencial;
    private String urlCredencial;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public LocalDate getDataObtencao() { return dataObtencao; }
    public void setDataObtencao(LocalDate dataObtencao) { this.dataObtencao = dataObtencao; }
    public LocalDate getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDate dataExpiracao) { this.dataExpiracao = dataExpiracao; }
    public String getNumeroCredencial() { return numeroCredencial; }
    public void setNumeroCredencial(String numeroCredencial) { this.numeroCredencial = numeroCredencial; }
    public String getUrlCredencial() { return urlCredencial; }
    public void setUrlCredencial(String urlCredencial) { this.urlCredencial = urlCredencial; }
}