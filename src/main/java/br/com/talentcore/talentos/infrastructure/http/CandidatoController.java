package br.com.talentcore.talentos.infrastructure.http;

import br.com.talentcore.talentos.api.dto.CandidatoRequest;
import br.com.talentcore.talentos.api.dto.CandidatoResponse;
import br.com.talentcore.talentos.api.mapper.CandidatoMapper;
import br.com.talentcore.talentos.application.BuscarCandidatoService;
import br.com.talentcore.talentos.application.CadastrarCandidatoService;
import br.com.talentcore.talentos.application.AtualizarCandidatoService;
import br.com.talentcore.talentos.application.port.in.AtualizarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENDPOINTS:
 *  - POST /api/candidatos        -> cria candidato (DTO in/out)
 *  - GET  /api/candidatos/{id}   -> busca por id (DTO out)
 *  - GET  /api/candidatos        -> busca com filtros (DTO out)
 *  - PUT  /api/candidatos/{id}   -> atualiza candidato (DTO in/out)
 */
@RestController
@RequestMapping("/api/candidatos")
public class CandidatoController {

    private final CadastrarCandidatoService cadastrar;
    private final BuscarCandidatoService buscar;
    private final AtualizarCandidatoUseCase atualizar;
    private final CandidatoRepository repository;

    // Mantendo a construção explícita dos services como no seu projeto
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

    // GET /api/candidatos?tecnologia=&nivel=&cidade=&estado=&idioma=&nivelIdioma=
    @GetMapping
    public ResponseEntity<List<CandidatoResponse>> filtrar(
            @RequestParam(name = "tecnologia",  required = false) String tecnologia,
            @RequestParam(name = "nivel",       required = false) String nivel,
            @RequestParam(name = "cidade",      required = false) String cidade,
            @RequestParam(name = "estado",      required = false) String estado,
            @RequestParam(name = "idioma",      required = false) String idioma,
            @RequestParam(name = "nivelIdioma", required = false) String nivelIdioma,
            @RequestParam(name = "page",        required = false, defaultValue = "0")  int page,
            @RequestParam(name = "size",        required = false, defaultValue = "50") int size
    ) {
        BuscarCandidatoUseCase.Filtros f = new BuscarCandidatoUseCase.Filtros();
        f.tecnologia  = tecnologia;
        f.nivel       = nivel;
        f.cidade      = cidade;
        f.estado      = estado;
        f.idioma      = idioma;
        f.nivelIdioma = nivelIdioma;

        List<Candidato> lista = buscar.executar(f);

        // Paginação simples em memória
        int from = Math.max(0, page * size);
        int to   = Math.min(lista.size(), from + size);
        List<Candidato> paged = (from >= lista.size()) ? List.of() : lista.subList(from, to);

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

        // Aplica merge (apenas campos informados) no agregado existente
        CandidatoMapper.merge(atual, req);

        // Caso de uso: valida e persiste atualização
        atualizar.executar(atual);

        return ResponseEntity.ok(CandidatoMapper.toResponse(atual));
    }
}