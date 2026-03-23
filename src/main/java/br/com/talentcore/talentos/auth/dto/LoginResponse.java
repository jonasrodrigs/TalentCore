package br.com.talentcore.talentos.auth.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Resposta de login.
 *
 * Mantém compatibilidade com a versão anterior (construtor que recebe apenas token),
 * e adiciona campos opcionais para o frontend montar a UI (FREE/PRO) sem precisar
 * decodificar o JWT.
 *
 * Campos:
 *  - token     : JWT emitido
 *  - email     : username (e-mail) do usuário autenticado
 *  - roles     : lista de papéis (ex.: ["ADMIN","RECRUITER"])
 *  - plan      : plano do usuário ("FREE" | "PRO")
 *  - expiresAt : instante de expiração do token em epoch millis
 *  - features  : recursos habilitados (ex.: ["BASIC_SEARCH","ADV_FILTERS"])
 */
public final class LoginResponse {

    private final String token;
    private final String email;
    private final List<String> roles;
    private final String plan;
    private final Long expiresAt;
    private final List<String> features;

    /**
     * Construtor de compatibilidade (comportamento antigo): só o token.
     * Útil enquanto o AuthService ainda não preenche os campos adicionais.
     */
    public LoginResponse(String token) {
        this.token = token;
        this.email = null;
        this.roles = List.of();
        this.plan = null;
        this.expiresAt = null;
        this.features = List.of();
    }

    /**
     * Construtor completo (nova resposta enriquecida).
     */
    public LoginResponse(
            String token,
            String email,
            List<String> roles,
            String plan,
            Long expiresAt,
            List<String> features
    ) {
        this.token = Objects.requireNonNull(token, "token não pode ser nulo");
        this.email = email;
        this.roles = roles == null ? List.of() : Collections.unmodifiableList(roles);
        this.plan = plan;
        this.expiresAt = expiresAt;
        this.features = features == null ? List.of() : Collections.unmodifiableList(features);
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getPlan() {
        return plan;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public List<String> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='***'," +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", plan='" + plan + '\'' +
                ", expiresAt=" + expiresAt +
                ", features=" + features +
                '}';
    }
}