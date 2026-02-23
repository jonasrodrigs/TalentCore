package br.com.talentcore.talentos.domain;

public class SoftSkill {
    private String id;
    private String candidatoId;
    private String nome; // Comunicação, Liderança...
    private NivelConhecimento nivel;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public NivelConhecimento getNivel() { return nivel; }
    public void setNivel(NivelConhecimento nivel) { this.nivel = nivel; }
}