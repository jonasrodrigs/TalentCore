package br.com.talentcore.talentos.application.port.in;

import br.com.talentcore.talentos.domain.Candidato;

public interface CadastrarCandidatoUseCase {
    String executar(Candidato candidato);
}