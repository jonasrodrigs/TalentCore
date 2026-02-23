package br.com.talentcore.talentos.domain;

public class Habilidade {
    private String id;
    private String candidatoId;
    private String nome;      // ex.: Java, Angular, Oracle
    private String categoria; // Frontend, Backend, DevOps, DB...
    private NivelConhecimento nivel; // BASICO, INTERMEDIARIO, AVANCADO, EXPERT

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public NivelConhecimento getNivel() { return nivel; }
    public void setNivel(NivelConhecimento nivel) { this.nivel = nivel; }
}