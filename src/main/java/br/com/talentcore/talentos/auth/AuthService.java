package br.com.talentcore.talentos.auth;

import br.com.talentcore.talentos.auth.dto.LoginRequest;
import br.com.talentcore.talentos.auth.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de autenticação.
 *
 * Responsabilidades:
 *  - Autenticar (email + password) via AuthenticationManager
 *  - Montar claims do JWT (roles, plan, features)
 *  - Emitir o JWT com JwtService (método com claims custom)
 *  - Retornar LoginResponse enriquecido para o frontend
 *
 * Observações:
 *  - Mantém compatibilidade com o fluxo atual (/auth/login).
 *  - Não altera nenhum endpoint / regra de Candidatos.
 *  - Plano (FREE/PRO) e features estão hardcoded por usuário (MVP).
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Autentica o usuário e gera o token JWT com claims customizadas.
     * Retorna payload enriquecido para que o front não precise decodificar o JWT.
     */
    public LoginResponse login(LoginRequest request) {
        // 1) Autenticação (email como username)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        var authentication = authenticationManager.authenticate(authToken);

        // 2) Usuário autenticado
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String email = user.getUsername();

        // 3) Montar roles a partir das authorities
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::stripRolePrefix) // ROLE_ADMIN -> ADMIN
                .collect(Collectors.toList());

        // 4) Plano e features (MVP: hardcoded por usuário)
        String plan = resolvePlanByEmail(email);
        List<String> features = resolveFeaturesByPlan(plan);

        // 5) Claims customizadas a irem dentro do JWT
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("roles", roles);
        claims.put("plan", plan);
        claims.put("features", features);

        // 6) Emissão do token com claims
        String token = jwtService.generateToken(user, claims);

        // 7) Calcula o expiresAt (epoch millis) apenas para conveniência no front
        long expiresAt = System.currentTimeMillis() + jwtService.getExpirationMillis();

        // 8) Resposta enriquecida (mantendo compatibilidade com o contrato antigo)
        return new LoginResponse(
                token,
                email,
                roles,
                plan,
                expiresAt,
                features
        );
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private String stripRolePrefix(String authority) {
        if (authority == null) return null;
        if (authority.startsWith("ROLE_")) {
            return authority.substring("ROLE_".length());
        }
        return authority;
    }

    /**
     * MVP: Plano por e-mail. Depois trocamos por consulta ao repositório/DB.
     *  - admin@talentcore.dev -> PRO
     *  - user@talentcore.dev  -> FREE
     *  - padrão -> FREE
     */
    private String resolvePlanByEmail(String email) {
        if (email == null) return "FREE";
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (e.equals("admin@talentcore.dev")) return "PRO";
        if (e.equals("user@talentcore.dev")) return "FREE";
        // default para novos usuários in-memory ou vindouros
        return "FREE";
    }

    /**
     * MVP: Features por plano.
     * FREE: funcionalidades essenciais (descoberta)
     * PRO : inclui ferramentas avançadas
     */
    private List<String> resolveFeaturesByPlan(String plan) {
        if ("PRO".equalsIgnoreCase(plan)) {
            return List.of(
                    "BASIC_SEARCH",
                    "ADV_FILTERS",
                    "EXPORT_CSV",
                    "BULK_MSG",
                    "PIPELINES",
                    "CANDIDATE_NOTES",
                    "SAVED_LISTS"
            );
        }
        // FREE
        return List.of(
                "BASIC_SEARCH"
        );
    }
}