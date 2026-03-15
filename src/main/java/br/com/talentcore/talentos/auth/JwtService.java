package br.com.talentcore.talentos.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Serviço responsável por gerar e validar tokens JWT.
 *
 * - Assinatura: HMAC SHA-256
 * - Configurações: security.jwt.secret / security.jwt.exp-minutes (application.yml)
 * - subject do token = username (no nosso caso, o e-mail)
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.exp-minutes}") long expMinutes
    ) {
        /*
         * O secret precisa ter tamanho suficiente (>= 32 bytes) para HMAC-SHA-256.
         * No application.yml você já definiu um valor seguro para DEV.
         */
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expMinutes * 60_000L; // minutos → millis
    }

    /* =========================================================
       Emissão
       ========================================================= */
    public String generateToken(UserDetails user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(user.getUsername()) // e-mail/username
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /* =========================================================
       Validação e extração
       ========================================================= */

    /**
     * Retorna o username (subject) contido no token, ou null se inválido.
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Verifica se o token é válido para o usuário informado:
     *  - Assinatura/estrutura válidas
     *  - subject = username do usuário
     *  - não expirado
     */
    public boolean isTokenValid(String token, UserDetails user) {
        try {
            final String username = extractUsername(token);
            return username != null
                    && username.equals(user.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** true se o token já expirou. */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /* =========================================================
       Helpers internos
       ========================================================= */

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Lança JwtException se token for inválido/assinado com outra chave
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}