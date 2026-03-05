package br.com.talentcore.talentos.application.port.in;

import br.com.talentcore.talentos.domain.Candidato;

public interface AtualizarCandidatoUseCase {
    void executar(Candidato candidato);
}