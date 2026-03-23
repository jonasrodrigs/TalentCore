package br.com.talentcore.talentos.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * DEV helper: imprime no console um hash BCrypt da senha 123456.
 * Remova este arquivo depois que terminar os testes.
 */
@Configuration
@Profile("oracle")
public class PrintBcrypt implements CommandLineRunner {
    @Override
    public void run(String... args) {
        var enc = new BCryptPasswordEncoder();
        var raw = "123456";
        System.out.println("=== BCRYPT(123456) ===");
        System.out.println(enc.encode(raw));
        System.out.println("======================");
    }
}