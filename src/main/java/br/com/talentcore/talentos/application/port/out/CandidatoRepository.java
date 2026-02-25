package br.com.talentcore.talentos.application.port.out;

import br.com.talentcore.talentos.domain.Candidato;
import java.util.List;
import java.util.Optional;

public interface CandidatoRepository {
    String salvar(Candidato candidato);
    Optional<Candidato> buscarPorId(String id);
    List<Candidato> buscarPorFiltros(String tecnologia, String nivel, String cidade, String estado, String idioma, String nivelIdioma);
}