package br.com.talentcore.talentos.infrastructure.persistence;

import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;

import java.util.List;
import java.util.Optional;

public class CandidatoRepositoryOracle implements CandidatoRepository {

    @Override
    public boolean existsByEmail(String email) {
        throw new UnsupportedOperationException("Oracle: existsByEmail ainda não implementado");
    }

    @Override
    public String salvar(Candidato c) {
        throw new UnsupportedOperationException("Oracle: salvar ainda não implementado");
    }

    @Override
    public Optional<Candidato> buscarPorId(String id) {
        throw new UnsupportedOperationException("Oracle: buscarPorId ainda não implementado");
    }

    @Override
    public List<Candidato> buscarPorFiltros(
            String tecnologia,
            String nivel,
            String cidade,
            String estado,
            String idioma,
            String nivelIdioma
    ) {
        throw new UnsupportedOperationException("Oracle: buscarPorFiltros ainda não implementado");
    }

    @Override
    public void atualizar(Candidato candidato) {
        throw new UnsupportedOperationException("Oracle: atualizar ainda não implementado");
    }
}