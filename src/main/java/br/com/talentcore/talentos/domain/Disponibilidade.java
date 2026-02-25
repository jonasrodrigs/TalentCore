package br.com.talentcore.talentos.domain;

public class Disponibilidade {
    private boolean aceitaViagens;
    private boolean aceitaMudanca;
    private String horarios;

    public boolean isAceitaViagens() {
        return aceitaViagens;
    }

    public void setAceitaViagens(boolean aceitaViagens) {
        this.aceitaViagens = aceitaViagens;
    }

    public boolean isAceitaMudanca() {
        return aceitaMudanca;
    }

    public void setAceitaMudanca(boolean aceitaMudanca) {
        this.aceitaMudanca = aceitaMudanca;
    }

    public String getHorarios() {
        return horarios;
    }

    public void setHorarios(String horarios) {
        this.horarios = horarios;
    }
}