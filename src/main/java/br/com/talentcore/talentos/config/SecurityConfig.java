package br.com.talentcore.talentos.config;

import br.com.talentcore.talentos.auth.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * SecurityConfig
 *
 * - API stateless (CSRF off, sem sessão)
 * - /auth/login, Swagger e Actuator: permitAll
 * - /api/**: exige JWT (validado pelo JwtAuthFilter)
 * - CORS habilitado (útil para Angular em localhost:4200)
 * - Respostas JSON para 401/403 quando não autenticado/autorizado
 */
@Configuration
public class SecurityConfig {

    /**
     * DEV: usuários em memória (username = e-mail).
     * Mantém compatibilidade com o fluxo atual de /auth/login.
     */
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.withUsername("admin@talentcore.dev")
                .password(encoder.encode("123456"))
                .roles("ADMIN", "RECRUITER")
                .build();

        var user = User.withUsername("user@talentcore.dev")
                .password(encoder.encode("123456"))
                .roles("USER", "RECRUITER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Cadeia de filtros / autorização.
     * - Mantém JwtAuthFilter antes de UsernamePasswordAuthenticationFilter.
     * - Habilita CORS para o front em localhost:4200.
     * - Padroniza 401/403 em JSON quando o acesso não chega a ser autenticado.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // API stateless (sem CSRF/sessão)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))

                // CORS (configurado no bean corsConfigurationSource)
                .cors(Customizer.withDefaults())

                // Regras de autorização
                .authorizeHttpRequests(auth -> auth
                        // Aberto
                        .requestMatchers(
                                "/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()

                        // Protegido por JWT
                        .requestMatchers("/api/**").authenticated()

                        // Qualquer outra rota: exige auth (mantenha assim por padrão)
                        .anyRequest().authenticated()
                )

                // (Dev) httpBasic como fallback; pode remover depois
                .httpBasic(Customizer.withDefaults())

                // JSON para 401/403 quando o acesso não é autenticado/autorizado
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthEntryPoint())
                        .accessDeniedHandler(jsonAccessDeniedHandler())
                );

        // Validação do Bearer <token> no header Authorization
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS para desenvolvimento: libera Angular (localhost:4200).
     * Ajuste conforme ambientes reais (origens, headers, métodos).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // ===== Helpers para respostas JSON em 401/403 =====

    private AuthenticationEntryPoint jsonAuthEntryPoint() {
        return (request, response, authException) ->
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    private AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = """
                {"status":%d,"error":"%s","message":"%s"}
                """.formatted(status, status == 401 ? "Unauthorized" : "Forbidden", message);
        response.getWriter().write(body);
    }
}