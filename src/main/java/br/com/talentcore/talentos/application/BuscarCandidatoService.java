package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;

import java.util.List;
import java.util.Objects;

public class BuscarCandidatoService implements BuscarCandidatoUseCase {

    private final CandidatoRepository repo;

    public BuscarCandidatoService(CandidatoRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Candidato> executar(Filtros f) {
        Objects.requireNonNull(f, "Filtros não podem ser nulos");
        return repo.buscarPorFiltros(
                safe(f.tecnologia),
                safe(f.nivel),
                safe(f.cidade),
                safe(f.estado),
                safe(f.idioma),
                safe(f.nivelIdioma)
        );
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}