package br.com.talentcore.talentos.application;

import java.util.List;

import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;

public class BuscarCandidatoService implements BuscarCandidatoUseCase {

    private final CandidatoRepository repository;

    public BuscarCandidatoService(CandidatoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Candidato> executar(Filtros f) {
        return repository.buscarPorFiltros(
                f == null ? null : f.tecnologia,
                f == null ? null : f.nivel,
                f == null ? null : f.cidade,
                f == null ? null : f.estado,
                f == null ? null : f.idioma,
                f == null ? null : f.nivelIdioma
        );
    }
}