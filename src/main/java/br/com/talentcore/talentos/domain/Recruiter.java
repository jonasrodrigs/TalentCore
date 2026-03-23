package br.com.talentcore.talentos.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidade de domínio: Recruiter (conta de acesso do recrutador).
 * <p>
 * Observações:
 * - Role é implícito (RECRUTADOR) para este agregado.
 * - Plan pode ser "FREE" ou "PRO" (string normalizada em UPPERCASE).
 * - Não expõe senha em toString; mantém senha em forma de hash.
 */
public class Recruiter {

    private Long id;
    private String nome;
    private String email;
    private String senhaHash;

    // Plano de assinatura: "FREE" | "PRO"
    private String plan;

    private String pais;
    private String uf;
    private String empresa;

    private Instant createdAt;

    // ============================
    // Construtores
    // ============================

    public Recruiter() {
        // Required by some mappers/reflective libs; keep empty.
    }

    private Recruiter(Builder b) {
        this.id = b.id;
        this.nome = b.nome;
        this.email = b.email;
        this.senhaHash = b.senhaHash;
        this.plan = b.plan;
        this.pais = b.pais;
        this.uf = b.uf;
        this.empresa = b.empresa;
        this.createdAt = b.createdAt;
    }

    // ============================
    // Fábricas / Validação
    // ============================

    /**
     * Cria um novo Recruiter com valores normalizados e plano default = FREE.
     * Não calcula hash da senha: espera receber senha já "hasheada" (responsabilidade da aplicação).
     */
    public static Recruiter createNew(String nome,
                                      String email,
                                      String senhaHash,
                                      String pais,
                                      String uf,
                                      String empresa) {
        ensureNotBlank(nome, "nome");
        ensureNotBlank(email, "email");
        ensureNotBlank(senhaHash, "senhaHash");
        ensureNotBlank(pais, "pais");
        ensureNotBlank(uf, "uf");

        Recruiter r = new Recruiter();
        r.nome = trim(nome);
        r.email = trimLower(email); // normaliza para comparação por e-mail
        r.senhaHash = trim(senhaHash);
        r.plan = "FREE";
        r.pais = trimUpper(pais);   // opcional: manter UPPER para consistência
        r.uf = trimUpper(uf);       // ex.: "PE", "SP"
        r.empresa = trimOrNull(empresa);
        r.createdAt = Instant.now();
        return r;
    }

    public void upgradeToPro() {
        this.plan = "PRO";
    }

    public void downgradeToFree() {
        this.plan = "FREE";
    }

    // ============================
    // Getters / Setters mínimos
    // ============================

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getPlan() {
        return plan;
    }

    public String getPais() {
        return pais;
    }

    public String getUf() {
        return uf;
    }

    public String getEmpresa() {
        return empresa;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = trim(nome);
    }

    public void setEmail(String email) {
        this.email = trimLower(email);
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = trim(senhaHash);
    }

    public void setPlan(String plan) {
        this.plan = normalizePlan(plan);
    }

    public void setPais(String pais) {
        this.pais = trimUpper(pais);
    }

    public void setUf(String uf) {
        this.uf = trimUpper(uf);
    }

    public void setEmpresa(String empresa) {
        this.empresa = trimOrNull(empresa);
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Role é implícito para este agregado.
     */
    public String getRole() {
        return "RECRUTADOR";
    }

    // ============================
    // Equals / HashCode / ToString
    // ============================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recruiter that)) return false;
        // Igualdade por ID (quando existir). Sem ID, fallback por e-mail.
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }
        return Objects.equals(this.email, that.email);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Recruiter{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", plan='" + plan + '\'' +
                ", pais='" + pais + '\'' +
                ", uf='" + uf + '\'' +
                ", empresa='" + empresa + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    // ============================
    // Builder
    // ============================

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String nome;
        private String email;
        private String senhaHash;
        private String plan = "FREE";
        private String pais;
        private String uf;
        private String empresa;
        private Instant createdAt = Instant.now();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = trim(nome);
            return this;
        }

        public Builder email(String email) {
            this.email = trimLower(email);
            return this;
        }

        public Builder senhaHash(String senhaHash) {
            this.senhaHash = trim(senhaHash);
            return this;
        }

        public Builder plan(String plan) {
            this.plan = normalizePlan(plan);
            return this;
        }

        public Builder pais(String pais) {
            this.pais = trimUpper(pais);
            return this;
        }

        public Builder uf(String uf) {
            this.uf = trimUpper(uf);
            return this;
        }

        public Builder empresa(String empresa) {
            this.empresa = trimOrNull(empresa);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Recruiter build() {
            // Validações mínimas
            ensureNotBlank(nome, "nome");
            ensureNotBlank(email, "email");
            ensureNotBlank(senhaHash, "senhaHash");
            ensureNotBlank(pais, "pais");
            ensureNotBlank(uf, "uf");

            // Normalizações finais
            this.plan = normalizePlan(this.plan);
            if (this.createdAt == null) this.createdAt = Instant.now();

            return new Recruiter(this);
        }
    }

    // ============================
    // Helpers de normalização/validação
    // ============================

    private static void ensureNotBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " é obrigatório");
        }
    }

    private static String trim(String v) {
        return v == null ? null : v.trim();
    }

    private static String trimOrNull(String v) {
        String t = trim(v);
        return (t == null || t.isEmpty()) ? null : t;
    }

    private static String trimLower(String v) {
        return v == null ? null : v.trim().toLowerCase();
    }

    private static String trimUpper(String v) {
        return v == null ? null : v.trim().toUpperCase();
    }

    private static String normalizePlan(String plan) {
        if (plan == null) return "FREE";
        String p = plan.trim().toUpperCase();
        return ("PRO".equals(p)) ? "PRO" : "FREE";
    }
}