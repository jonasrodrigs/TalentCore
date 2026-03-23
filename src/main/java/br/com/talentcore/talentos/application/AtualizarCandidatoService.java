package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.AtualizarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.service.CandidatoService;

/**
 * Caso de uso: Atualizar Candidato.
 *
 * Orquestra a validação de domínio e a persistência em duas etapas:
 *  - repository.atualizar(candidato): dados principais (nome, dataNascimento, email, endereço mínimo)
 *  - repository.atualizarCurriculo(candidato): currículo/links/dados pessoais/disponibilidade/foto
 *
 * Observação:
 *  Mantemos duas chamadas separadas para não quebrar adapters existentes e permitir
 *  evolução incremental do repositório.
 */
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

        // Valida o agregado (invariantes de domínio, limites de campos etc.)
        domain.validar(candidato);

        // (Opcional) Regra de e-mail único:
        // Se quiser bloquear mudança para e-mail existente de OUTRO candidato,
        // implemente repository.buscarPorEmail() e compare ids.
        if (candidato.getContato() != null && candidato.getContato().getEmail() != null) {
            String novoEmail = candidato.getContato().getEmail();
            // Exemplo de verificação (desativada por padrão):
            // if (repository.existsByEmail(novoEmail)) { ... }
        }

        // 1) Atualiza dados principais (nome, dataNascimento, email, endereço curto)
        repository.atualizar(candidato);

        // 2) Atualiza currículo/links/dados pessoais/disponibilidade/foto
        repository.atualizarCurriculo(candidato);
    }
}