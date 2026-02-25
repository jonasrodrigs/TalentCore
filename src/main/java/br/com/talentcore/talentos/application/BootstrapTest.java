package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.in.CadastrarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.*;
import br.com.talentcore.talentos.domain.service.CandidatoService;
import br.com.talentcore.talentos.infrastructure.persistence.CandidatoRepositoryOracle;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BootstrapTest {

    public static void main(String[] args) {
        // 1) Wiring manual (sem framework)
        CandidatoRepository repo = new CandidatoRepositoryOracle();
        CandidatoService domainService = new CandidatoService();
        CadastrarCandidatoUseCase cadastrarUC = new CadastrarCandidatoService(repo, domainService);
        BuscarCandidatoUseCase buscarUC = new BuscarCandidatoService(repo);

        // 2) Criar 2 candidatos (MVP só preenche campos da tabela CANDIDATO)
        String idJonas = cadastrarUC.executar(criarCandidato(
                "Jonas Teste Angular",
                "jonas.angular@talentcore.dev",
                "BARUERI", "SP",
                true, false));

        String idMaria = cadastrarUC.executar(criarCandidato(
                "Maria Java Oracle",
                "maria.oracle@talentcore.dev",
                "SÃO PAULO", "SP",
                false, true));

        System.out.println("Cadastrados: " + idJonas + " | " + idMaria);

        // 3) Buscar por ID
        repo.buscarPorId(idJonas).ifPresent(c ->
                System.out.println("[buscarPorId] " + c.getId() + " / " + c.getNomeCompleto() + " / " + c.getContato().getEmail())
        );

        // 4) Buscar por filtros (MVP: o método já filtra por cidade/estado e estrutura para tecnologia/nivel/idioma)
        BuscarCandidatoUseCase.Filtros filtros = new BuscarCandidatoUseCase.Filtros();
        filtros.cidade = "BARUERI";     // testando filtro por cidade
        filtros.estado = "SP";

        List<Candidato> encontrados = buscarUC.executar(filtros);
        System.out.println("[buscarPorFiltros] encontrados = " + encontrados.size());
        for (Candidato c : encontrados) {
            System.out.println(" - " + c.getNomeCompleto() + " | " + c.getEndereco().getCidade() + "/" + c.getEndereco().getEstado());
        }
    }

    private static Candidato criarCandidato(String nome, String email, String cidade, String estado,
                                            boolean aceitaViagens, boolean aceitaMudanca) {
        Candidato c = new Candidato();
        c.setId(UUID.randomUUID().toString());
        c.setNomeCompleto(nome);
        c.setDataNascimento(LocalDate.of(1990, 1, 1));

        Contato ct = new Contato();
        ct.setEmail(email);
        ct.setTelefone("+55 11 90000-0000");
        c.setContato(ct);

        Endereco e = new Endereco();
        e.setLogradouro("Rua Teste");
        e.setNumero("100");
        e.setBairro("Centro");
        e.setCidade(cidade);
        e.setEstado(estado);
        e.setPais("BRASIL");
        e.setCep("00000-000");
        c.setEndereco(e);

        Disponibilidade d = new Disponibilidade();
        d.setAceitaViagens(aceitaViagens);
        d.setAceitaMudanca(aceitaMudanca);
        d.setHorarios("Comercial");
        c.setDisponibilidade(d);

        // Observação: no MVP não estamos preenchendo as coleções (experiência, habilidades etc.)
        // porque o repositório ainda não persiste/consulta as tabelas filhas.
        return c;
    }
}