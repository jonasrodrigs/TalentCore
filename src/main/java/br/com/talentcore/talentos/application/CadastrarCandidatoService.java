package br.com.talentcore.talentos.application;

import java.util.UUID;

import br.com.talentcore.talentos.application.port.in.CadastrarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.service.CandidatoService; // <- instância

public class CadastrarCandidatoService implements CadastrarCandidatoUseCase {

    private final CandidatoRepository repository;
    private final CandidatoService candidatoService = new CandidatoService();

    public CadastrarCandidatoService(CandidatoRepository repository) {
        this.repository = repository;
    }

    @Override
    public String executar(Candidato candidato) {
        if (candidato.getId() == null || candidato.getId().trim().isEmpty()) {
            candidato.setId(UUID.randomUUID().toString());
        }

        // validação via instância
        candidatoService.validar(candidato);

        String email = (candidato.getContato() != null) ? candidato.getContato().getEmail() : null;
        if (email != null && !email.trim().isEmpty() && repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado: " + email);
        }

        String id = repository.salvar(candidato);
        System.out.println("[INFO] Candidato salvo com id: " + id);
        return id;
    }
}