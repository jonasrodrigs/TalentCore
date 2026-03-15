package br.com.talentcore.talentos.auth;

import br.com.talentcore.talentos.auth.dto.LoginRequest;
import br.com.talentcore.talentos.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação.
 *
 * Endpoint:
 *   POST /auth/login
 *
 * Request:
 *   {
 *     "email": "usuario@empresa.com",
 *     "password": "senha"
 *   }
 *
 * Response (200):
 *   {
 *     "token": "JWT_AQUI"
 *   }
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Realiza o login e retorna um token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}