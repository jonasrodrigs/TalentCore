package br.com.talentcore.talentos.auth;

import br.com.talentcore.talentos.auth.dto.LoginRequest;
import br.com.talentcore.talentos.auth.dto.LoginResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller de autenticação.
 *
 * Endpoints:
 *   - POST /auth/login          -> autentica e retorna o JWT + payload enriquecido
 *   - GET  /api/auth/me         -> retorna o perfil (email, roles, plan, features, expiresAt)
 *
 * Observações:
 *   - Mantém o /auth/login como já estava.
 *   - /api/auth/me usa o JwtAuthFilter já existente (que filtra /api/**) para validar o Bearer.
 *   - Não altera nada do módulo de candidatos.
 */
@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Realiza o login e retorna um token JWT.
     *
     * Request:
     *   POST /auth/login
     *   {
     *     "email": "usuario@empresa.com",
     *     "password": "senha"
     *   }
     *
     * Response (200):
     *   {
     *     "token": "JWT_AQUI",
     *     "email": "...",
     *     "roles": ["ADMIN","RECRUITER"],
     *     "plan": "PRO",
     *     "expiresAt": 1710000000000,
     *     "features": ["BASIC_SEARCH", ...]
     *   }
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna o perfil do usuário autenticado (sem gerar novo token).
     *
     * Importante: usamos o prefixo /api para aproveitar o JwtAuthFilter já configurado.
     *
     * Request:
     *   GET /api/auth/me
     *   (com Authorization: Bearer <token>)
     *
     * Response (200):
     *   {
     *     "token": null,                 // não reemitimos token aqui
     *     "email": "user@talentcore.dev",
     *     "roles": ["USER","RECRUITER"], // derivado do SecurityContext e/ou claims
     *     "plan": "FREE",                // lido das claims, se presente
     *     "expiresAt": 1710000000000,    // lido do 'exp' do JWT
     *     "features": ["BASIC_SEARCH"]   // lido das claims, se presente
     *   }
     *
     * Notas:
     *  - Se o token tiver sido emitido antes de adicionarmos claims (MVP inicial), os campos
     *    plan/features podem vir vazios; isso é esperado e compatível.
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<LoginResponse> me(HttpServletRequest request) {
        // 1) Recupera o Authentication já populado pelo JwtAuthFilter (porque estamos em /api/**)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // Por segurança, retorna 401; em geral o SecurityConfig já barraria antes.
            return ResponseEntity.status(401).build();
        }

        // 2) Extrai o email/username do principal (normalmente o e-mail)
        String email = Objects.toString(authentication.getName(), null);

        // 3) Extrai roles das authorities (ROLE_X -> X)
        List<String> roles = authentication.getAuthorities() == null ? List.of() :
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(AuthController::stripRolePrefixStatic)
                        .collect(Collectors.toList());

        // 4) Lê o token do header Authorization para acessar claims (plan/features/exp)
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                token = null;
            }
        }

        String plan = null;
        List<String> features = List.of();
        Long expiresAt = null;

        if (token != null) {
            try {
                Claims claims = jwtService.extractAllClaims(token);
                // exp: Date -> epoch millis
                if (claims.getExpiration() != null) {
                    expiresAt = claims.getExpiration().getTime();
                }
                // plan/features: podem não existir em tokens antigos; trate como opcionais
                Object planObj = claims.get("plan");
                if (planObj != null) {
                    plan = String.valueOf(planObj);
                }
                Object feats = claims.get("features");
                if (feats instanceof List<?> list) {
                    // mapeia valores para string
                    features = list.stream()
                            .map(String::valueOf)
                            .collect(Collectors.toUnmodifiableList());
                }
            } catch (Exception ignored) {
                // Token inválido aqui é improvável (já teria sido barrado pelo filtro).
                // Mantemos campos opcionais nulos/vazios.
            }
        }

        // 5) Não reemitimos token em /me; retornamos apenas o perfil atual
        LoginResponse body = new LoginResponse(
                null,              // token (não reemitido aqui)
                email,             // email
                roles,             // roles
                plan,              // plan (se presente nas claims)
                expiresAt,         // expiresAt do JWT
                features           // features (se presente nas claims)
        );

        return ResponseEntity.ok(body);
    }

    // ===== Helpers internos (estáticos) =====

    private static String stripRolePrefixStatic(String authority) {
        if (authority == null) return null;
        if (authority.startsWith("ROLE_")) {
            return authority.substring("ROLE_".length());
        }
        return authority;
    }
}