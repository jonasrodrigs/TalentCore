package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.AtualizarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.service.CandidatoService;

public class AtualizarCandidatoService implements AtualizarCandidatoUseCase {

    private final CandidatoRepository repository;
    private final CandidatoService domain = new CandidatoService();

    public AtualizarCandidatoService(CandidatoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void executar(Candidato candidato) {
        if (candidato == null || candidato.getId() == null || candidato.getId().isBlank()) {
            throw new IllegalArgumentException("Id do candidato é obrigatório para atualização.");
        }

        // Valida o agregado
        domain.validar(candidato);

        // (Opcional) Regra de e-mail único:
        // Se quiser bloquear mudança para e-mail existente de OUTRO candidato,
        // implemente repository.buscarPorEmail() e compare ids.
        if (candidato.getContato() != null && candidato.getContato().getEmail() != null) {
            String novoEmail = candidato.getContato().getEmail();
            // Esta verificação simples pode gerar falso positivo se o e-mail não mudou;
            // recomendo buscar por email e comparar ids (se necessário).
            // if (repository.existsByEmail(novoEmail)) { ... }
        }

        repository.atualizar(candidato);
    }
}