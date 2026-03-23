package br.com.talentcore.talentos.domain;

import java.util.Objects;

/**
 * Endereço do candidato (valor simples do agregado Candidato).
 *
 * Campos opcionais: todos podem ser nulos/blank no MVP.
 * Observação: mantido enxuto, sem validações rígidas no domínio por enquanto.
 */
public class Endereco {

    /** Logradouro (ex.: Avenida Paulista) */
    private String logradouro;

    /** Número (ex.: 1000) */
    private String numero;

    /** Complemento (ex.: Apto 12, Bloco B) */
    private String complemento;

    /** Bairro (ex.: Boa Viagem) */
    private String bairro;

    /** Cidade (ex.: Recife) */
    private String cidade;

    /** Estado/UF (ex.: PE) */
    private String estado;

    /** País (ex.: Brasil) */
    private String pais;

    /** CEP (ex.: 51011-000) */
    private String cep;

    // ------------------------------------------------------------
    // Construtores
    // ------------------------------------------------------------

    public Endereco() {
    }

    public Endereco(String logradouro,
                    String numero,
                    String complemento,
                    String bairro,
                    String cidade,
                    String estado,
                    String pais,
                    String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
        this.cep = cep;
    }

    // ------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    // ------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------

    /**
     * Retorna uma representação curta para exibição (ex.: "Boa Viagem · Recife/PE").
     * Este método não faz formatação de maiúsculas/minúsculas.
     */
    public String toEnderecoCurto() {
        String b = safe(bairro);
        String c = safe(cidade);
        String uf = safe(estado);

        String cidadeUf = c + (uf.isEmpty() ? "" : "/" + uf);
        if (b.isEmpty() && cidadeUf.isEmpty()) return "";

        if (b.isEmpty()) return cidadeUf;
        if (cidadeUf.isEmpty()) return b;
        return b + " · " + cidadeUf;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ------------------------------------------------------------
    // equals/hashCode/toString - úteis para testes/logs
    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endereco)) return false;
        Endereco endereco = (Endereco) o;
        return Objects.equals(logradouro, endereco.logradouro)
                && Objects.equals(numero, endereco.numero)
                && Objects.equals(complemento, endereco.complemento)
                && Objects.equals(bairro, endereco.bairro)
                && Objects.equals(cidade, endereco.cidade)
                && Objects.equals(estado, endereco.estado)
                && Objects.equals(pais, endereco.pais)
                && Objects.equals(cep, endereco.cep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logradouro, numero, complemento, bairro, cidade, estado, pais, cep);
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "logradouro='" + logradouro + '\'' +
                ", numero='" + numero + '\'' +
                ", complemento='" + complemento + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", pais='" + pais + '\'' +
                ", cep='" + cep + '\'' +
                '}';
    }
}