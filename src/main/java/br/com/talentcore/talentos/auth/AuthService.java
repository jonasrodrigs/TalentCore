package br.com.talentcore.talentos.auth;

import br.com.talentcore.talentos.auth.dto.LoginRequest;
import br.com.talentcore.talentos.auth.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Serviço de autenticação.
 *
 * - Recebe email + password
 * - Autentica via AuthenticationManager
 * - Gera JWT usando JwtService
 * - Retorna LoginResponse { token }
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
     * Autentica o usuário e gera o token JWT.
     */
    public LoginResponse login(LoginRequest request) {
        // Cria o token de autenticação usando email como username
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                );

        // Dispara o fluxo de autenticação do Spring Security
        var authentication = authenticationManager.authenticate(authToken);

        // Usuário autenticado
        UserDetails user = (UserDetails) authentication.getPrincipal();

        // Gera o JWT
        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }
}