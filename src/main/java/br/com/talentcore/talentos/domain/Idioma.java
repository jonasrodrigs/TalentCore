package br.com.talentcore.talentos.domain;

public class Idioma {
    private String id;
    private String candidatoId;
    private String idioma; // ex.: "pt-BR", "en-US"
    private NivelIdioma nivel; // BASICO, INTERMEDIARIO, AVANCADO, FLUENTE, NATIVO

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidatoId() {
        return candidatoId;
    }

    public void setCandidatoId(String candidatoId) {
        this.candidatoId = candidatoId;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public NivelIdioma getNivel() {
        return nivel;
    }

    public void setNivel(NivelIdioma nivel) {
        this.nivel = nivel;
    }
}