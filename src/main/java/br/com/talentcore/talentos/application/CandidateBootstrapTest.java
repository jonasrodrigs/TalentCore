package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.infrastructure.persistence.mvp.CandidateRepositoryOracle;
import br.com.talentcore.talentos.infrastructure.persistence.mvp.CandidateRepositoryOracle.Candidate;

import java.util.List;

public class CandidateBootstrapTest {
    public static void main(String[] args) {

        System.out.println("URL  = " + br.com.talentcore.talentos.config.DatabaseConfig.url());
        System.out.println("USER = " + br.com.talentcore.talentos.config.DatabaseConfig.user());
        System.out.println("PING = " + br.com.talentcore.talentos.config.DatabaseConfig.ping()); // true = OK

        // 0) Sanity check de conexão crua (ISOLAR ORA-01017 antes do repo)
        System.out.println("URL  = " + br.com.talentcore.talentos.config.DatabaseConfig.url());
        System.out.println("USER = " + br.com.talentcore.talentos.config.DatabaseConfig.user());
        try (var con = br.com.talentcore.talentos.config.DatabaseConfig.getConnection()) {
            System.out.println("Conectou OK!");
        } catch (Exception e) {
            System.err.println("Falhou na conexão direta: " + e.getMessage());
            e.printStackTrace();
            return; // se não conectou, pare aqui
        }

        CandidateRepositoryOracle repo = new CandidateRepositoryOracle();

        try {
            // 1) existsByEmail
            boolean jaExiste = repo.existsByEmail("maria.dev@example.com");
            System.out.println("existsByEmail(maria.dev@example.com) = " + jaExiste);

            // 2) save
            Candidate novo = new Candidate();
            novo.setFullName("Jonas Teste JDBC");
            novo.setEmail("jonas.jdbc." + System.currentTimeMillis() + "@dev.example.com"); // evita unique violation
            novo.setPhone("1197000-0000");
            novo.setSkills("Java; JDBC; Oracle");

            long id = repo.save(novo);
            System.out.println("ID salvo = " + id);

            // 3) findById
            repo.findById(id).ifPresent(c ->
                    System.out.println("Encontrado: " + c.getId() + " | " + c.getFullName() + " | " + c.getEmail())
            );

            // 4) listPage
            List<Candidate> page = repo.listPage(0, 10);
            System.out.println("listPage(0,10) -> " + page.size() + " registros");

            // 5) update
            novo.setPhone("1198888-7777");
            repo.update(novo);
            System.out.println("Atualizado phone. Verifique UPDATED_AT via SELECT, se quiser.");

            // 6) delete
            boolean removed = repo.delete(id);
            System.out.println("Deletado? " + removed);

        } catch (RuntimeException ex) {
            System.err.println("Falhou no fluxo do repositório: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}