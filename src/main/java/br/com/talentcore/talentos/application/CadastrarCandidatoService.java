package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.CadastrarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.service.CandidatoService;

import java.util.UUID;

public class CadastrarCandidatoService implements CadastrarCandidatoUseCase {

    private final CandidatoRepository repo;
    private final CandidatoService service;

    public CadastrarCandidatoService(CandidatoRepository repo, CandidatoService service) {
        this.repo = repo;
        this.service = service;
    }

    @Override
    public String executar(Candidato candidato) {
        if (candidato.getId() == null || candidato.getId().isBlank()) { // <- isBlank() com 's'
            candidato.setId(UUID.randomUUID().toString());
        }
        service.validarAntesDeSalvar(candidato);
        return repo.salvar(candidato);
    }
}