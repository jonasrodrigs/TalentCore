package br.com.talentcore.talentos.domain;

import java.util.Objects;

/**
 * Disponibilidade do candidato.
 *
 * Observações de modelagem (MVP):
 * - Os campos booleanos foram modelados como {@link Boolean} (caixa alta) para permitir estado "desconhecido" (null).
 * - Mantidas as assinaturas clássicas de getters/ setters com primitivo (para compatibilidade),
 *   normalizando null -> false nas leituras.
 * - Helpers utilitários permitem trabalhar com representações 'S' / 'N' quando necessário
 *   (útil para persistência em bancos que usam CHAR(1)).
 */
public class Disponibilidade {

    /** Aceita viagens? (S/N/indefinido) */
    private Boolean aceitaViagens;

    /** Aceita mudança? (S/N/indefinido) */
    private Boolean aceitaMudanca;

    /** Janela/observação de horários (ex.: "Comercial", "Turnos", "Noturno") */
    private String horarios;

    // ------------------------------------------------------------
    // Construtores
    // ------------------------------------------------------------

    public Disponibilidade() {
    }

    public Disponibilidade(Boolean aceitaViagens, Boolean aceitaMudanca, String horarios) {
        this.aceitaViagens = aceitaViagens;
        this.aceitaMudanca = aceitaMudanca;
        this.horarios = horarios;
    }

    // ------------------------------------------------------------
    // Getters/Setters (compatíveis com o que você já usava)
    // ------------------------------------------------------------

    /**
     * Getter "compatível": retorna false quando nulo.
     * Use {@link #getAceitaViagensNullable()} se precisar distinguir null.
     */
    public boolean isAceitaViagens() {
        return Boolean.TRUE.equals(aceitaViagens);
    }

    /** Versão que preserva null (desconhecido). */
    public Boolean getAceitaViagensNullable() {
        return aceitaViagens;
    }

    /** Setter compatível (primitivo). */
    public void setAceitaViagens(boolean aceitaViagens) {
        this.aceitaViagens = aceitaViagens;
    }

    /** Setter que aceita null. */
    public void setAceitaViagens(Boolean aceitaViagens) {
        this.aceitaViagens = aceitaViagens;
    }

    /**
     * Getter "compatível": retorna false quando nulo.
     * Use {@link #getAceitaMudancaNullable()} se precisar distinguir null.
     */
    public boolean isAceitaMudanca() {
        return Boolean.TRUE.equals(aceitaMudanca);
    }

    /** Versão que preserva null (desconhecido). */
    public Boolean getAceitaMudancaNullable() {
        return aceitaMudanca;
    }

    /** Setter compatível (primitivo). */
    public void setAceitaMudanca(boolean aceitaMudanca) {
        this.aceitaMudanca = aceitaMudanca;
    }

    /** Setter que aceita null. */
    public void setAceitaMudanca(Boolean aceitaMudanca) {
        this.aceitaMudanca = aceitaMudanca;
    }

    public String getHorarios() {
        return horarios;
    }

    public void setHorarios(String horarios) {
        this.horarios = horarios;
    }

    // ------------------------------------------------------------
    // Helpers: conversões 'S'/'N' (úteis para persistência Char(1))
    // ------------------------------------------------------------

    /** Converte aceitaViagens para 'S', 'N' ou null. */
    public String getAceitaViagensSN() {
        if (aceitaViagens == null) return null;
        return Boolean.TRUE.equals(aceitaViagens) ? "S" : "N";
    }

    /** Define aceitaViagens a partir de 'S'/'N' (ignora maiúsc./minúsc.), outros valores viram null. */
    public void setAceitaViagensFromSN(String v) {
        if (v == null) { this.aceitaViagens = null; return; }
        switch (v.trim().toUpperCase()) {
            case "S": this.aceitaViagens = true; break;
            case "N": this.aceitaViagens = false; break;
            default:  this.aceitaViagens = null;
        }
    }

    /** Converte aceitaMudanca para 'S', 'N' ou null. */
    public String getAceitaMudancaSN() {
        if (aceitaMudanca == null) return null;
        return Boolean.TRUE.equals(aceitaMudanca) ? "S" : "N";
    }

    /** Define aceitaMudanca a partir de 'S'/'N'. */
    public void setAceitaMudancaFromSN(String v) {
        if (v == null) { this.aceitaMudanca = null; return; }
        switch (v.trim().toUpperCase()) {
            case "S": this.aceitaMudanca = true; break;
            case "N": this.aceitaMudanca = false; break;
            default:  this.aceitaMudanca = null;
        }
    }

    // ------------------------------------------------------------
    // equals/hashCode/toString — úteis para testes/logs
    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disponibilidade)) return false;
        Disponibilidade that = (Disponibilidade) o;
        return Objects.equals(aceitaViagens, that.aceitaViagens)
                && Objects.equals(aceitaMudanca, that.aceitaMudanca)
                && Objects.equals(horarios, that.horarios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aceitaViagens, aceitaMudanca, horarios);
    }

    @Override
    public String toString() {
        return "Disponibilidade{" +
                "aceitaViagens=" + aceitaViagens +
                ", aceitaMudanca=" + aceitaMudanca +
                ", horarios='" + horarios + '\'' +
                '}';
    }
}