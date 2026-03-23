package br.com.talentcore.talentos.infrastructure.http;

import br.com.talentcore.talentos.api.dto.CandidatoRequest;
import br.com.talentcore.talentos.api.dto.CandidatoResponse;
import br.com.talentcore.talentos.api.mapper.CandidatoMapper;
import br.com.talentcore.talentos.application.AtualizarCandidatoService;
import br.com.talentcore.talentos.application.BuscarCandidatoService;
import br.com.talentcore.talentos.application.CadastrarCandidatoService;
import br.com.talentcore.talentos.application.port.in.AtualizarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.Experiencia;
import br.com.talentcore.talentos.domain.Habilidade;
import br.com.talentcore.talentos.domain.Idioma;
import br.com.talentcore.talentos.domain.Projeto;
// Utilitário específico do MVP (InMemory)
import br.com.talentcore.talentos.infrastructure.persistence.inmemory.CandidatoRepositoryInMemory;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ENDPOINTS:
 *  - POST /api/candidatos
 *  - GET  /api/candidatos/{id}
 *  - GET  /api/candidatos             (suporta filtros existentes + q=busca livre)
 *  - PUT  /api/candidatos/{id}
 *
 * Parâmetro "q" (opcional) em GET /api/candidatos:
 *   Pesquisa livre (case-insensitive) em:
 *     • nomeCompleto
 *     • contato.email
 *     • endereco.cidade / endereco.estado / endereco.pais
 *     • habilidadesTecnicas.nome
 *     • experiencias.tecnologias
 *     • projetos.tecnologias
 *     • idiomas.idioma
 */
@RestController
@RequestMapping("/api/candidatos")
public class CandidatoController {

    private final CadastrarCandidatoService cadastrar;
    private final BuscarCandidatoService buscar;
    private final AtualizarCandidatoUseCase atualizar;
    private final CandidatoRepository repository;

    public CandidatoController(CandidatoRepository repository) {
        this.repository = repository;
        this.cadastrar  = new CadastrarCandidatoService(repository);
        this.buscar     = new BuscarCandidatoService(repository);
        this.atualizar  = new AtualizarCandidatoService(repository);
    }

    // POST /api/candidatos
    @PostMapping
    public ResponseEntity<CandidatoResponse> criar(@Valid @RequestBody CandidatoRequest req) {
        Candidato cand = CandidatoMapper.fromRequest(req);
        String id = cadastrar.executar(cand);
        cand.setId(id);
        return ResponseEntity.ok(CandidatoMapper.toResponse(cand));
    }

    // GET /api/candidatos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CandidatoResponse> porId(@PathVariable("id") String id) {
        Optional<Candidato> opt = repository.buscarPorId(id);
        return opt.map(CandidatoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/candidatos?tecnologia=&nivel=&cidade=&estado=&idioma=&nivelIdioma=&q=&page=&size=
    @GetMapping
    public ResponseEntity<List<CandidatoResponse>> filtrar(
            @RequestParam(name = "tecnologia",  required = false) String tecnologia,
            @RequestParam(name = "nivel",       required = false) String nivel,
            @RequestParam(name = "cidade",      required = false) String cidade,
            @RequestParam(name = "estado",      required = false) String estado,
            @RequestParam(name = "idioma",      required = false) String idioma,
            @RequestParam(name = "nivelIdioma", required = false) String nivelIdioma,
            @RequestParam(name = "q",           required = false) String q,
            @RequestParam(name = "page",        required = false, defaultValue = "0")  int page,
            @RequestParam(name = "size",        required = false, defaultValue = "50") int size
    ) {
        // 1) Filtros já existentes (MVP)
        BuscarCandidatoUseCase.Filtros f = new BuscarCandidatoUseCase.Filtros();
        f.tecnologia  = tecnologia;
        f.nivel       = nivel;
        f.cidade      = cidade;
        f.estado      = estado;
        f.idioma      = idioma;
        f.nivelIdioma = nivelIdioma;

        List<Candidato> lista = buscar.executar(f);

        // 2) Busca livre q (case-insensitive)
        String qNorm = normalize(q);
        if (qNorm != null && !qNorm.isBlank()) {
            if (repository instanceof CandidatoRepositoryInMemory inm) {
                // Usa utilitário do repositório InMemory quando ativo
                lista = inm.aplicarBuscaLivre(lista, qNorm);
            } else {
                // Fallback genérico (outros adapters)
                Predicate<Candidato> matches = freeTextMatcher(qNorm);
                lista = lista.stream().filter(matches).collect(Collectors.toList());
            }
        }

        // 3) Paginação simples em memória
        int from = Math.max(0, page * size);
        int to   = Math.min(lista.size(), from + size);
        List<Candidato> paged = (from >= lista.size()) ? List.of() : lista.subList(from, to);

        // 4) DTO
        List<CandidatoResponse> out = paged.stream()
                .map(CandidatoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    // PUT /api/candidatos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CandidatoResponse> atualizar(
            @PathVariable("id") String id,
            @Valid @RequestBody CandidatoRequest req
    ) {
        Optional<Candidato> opt = repository.buscarPorId(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Candidato atual = opt.get();

        CandidatoMapper.merge(atual, req);
        atualizar.executar(atual);

        return ResponseEntity.ok(CandidatoMapper.toResponse(atual));
    }

    // ----------------- Helpers -----------------

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase(Locale.ROOT);
    }

    private Predicate<Candidato> freeTextMatcher(String qUpper) {
        return c -> {
            if (contains(c.getNomeCompleto(), qUpper)) return true;

            if (c.getContato() != null && contains(c.getContato().getEmail(), qUpper)) return true;

            if (c.getEndereco() != null) {
                if (contains(c.getEndereco().getCidade(), qUpper)) return true;
                if (contains(c.getEndereco().getEstado(), qUpper)) return true; // mapeado de UF no repo
                if (contains(c.getEndereco().getPais(), qUpper)) return true;   // pode ser null
            }

            if (c.getHabilidadesTecnicas() != null) {
                for (Habilidade h : c.getHabilidadesTecnicas()) {
                    if (h != null && contains(h.getNome(), qUpper)) return true;
                }
            }

            if (c.getExperiencias() != null) {
                for (Experiencia e : c.getExperiencias()) {
                    if (e != null && e.getTecnologias() != null) {
                        for (String t : e.getTecnologias()) {
                            if (contains(t, qUpper)) return true;
                        }
                    }
                }
            }

            if (c.getProjetos() != null) {
                for (Projeto p : c.getProjetos()) {
                    if (p != null && p.getTecnologias() != null) {
                        for (String t : p.getTecnologias()) {
                            if (contains(t, qUpper)) return true;
                        }
                    }
                }
            }

            if (c.getIdiomas() != null) {
                for (Idioma i : c.getIdiomas()) {
                    if (i != null && contains(i.getIdioma(), qUpper)) return true;
                }
            }

            return false;
        };
    }

    private boolean contains(String value, String qUpper) {
        if (value == null) return false;
        String v = value.trim().toUpperCase(Locale.ROOT);
        return v.contains(qUpper);
    }
}