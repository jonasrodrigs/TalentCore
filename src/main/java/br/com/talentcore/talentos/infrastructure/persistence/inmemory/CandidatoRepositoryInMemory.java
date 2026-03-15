package br.com.talentcore.talentos.infrastructure.persistence.inmemory;

import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.Habilidade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CandidatoRepositoryInMemory implements CandidatoRepository {

    private final Map<String, Candidato> store = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String key = email.trim().toUpperCase(Locale.ROOT);
        return store.values().stream()
                .anyMatch(c -> c.getContato() != null
                        && c.getContato().getEmail() != null
                        && c.getContato().getEmail().trim().toUpperCase(Locale.ROOT).equals(key));
    }

    @Override
    public String salvar(Candidato c) {
        if (c.getId() == null || c.getId().isBlank()) {
            c.setId(UUID.randomUUID().toString());
        }
        store.put(c.getId(), deepCopy(c));
        System.out.println("[INFO] (MVP) Candidato salvo com id: " + c.getId());
        return c.getId();
    }

    @Override
    public Optional<Candidato> buscarPorId(String id) {
        Candidato c = store.get(id);
        return Optional.ofNullable(c != null ? deepCopy(c) : null);
    }

    @Override
    public List<Candidato> buscarPorFiltros(String tecnologia, String nivel, String cidade, String estado, String idioma, String nivelIdioma) {
        return store.values().stream()
                .filter(c -> matchCidadeEstado(c, cidade, estado))
                .filter(c -> matchTecnologia(c, tecnologia))
                .filter(c -> matchNivelHabilidade(c, nivel))
                .filter(c -> matchIdioma(c, idioma, nivelIdioma))
                .map(this::deepCopy)
                .collect(Collectors.toList());
    }

    @Override
    public void atualizar(Candidato candidato) {
        // Atualização substitutiva do agregado inteiro
        if (candidato == null || candidato.getId() == null || !store.containsKey(candidato.getId())) {
            throw new IllegalArgumentException("Candidato não encontrado para atualização: " + (candidato != null ? candidato.getId() : null));
        }
        store.put(candidato.getId(), deepCopy(candidato));
        System.out.println("[INFO] (MVP) Candidato atualizado id=" + candidato.getId());
    }

    // --- Predicados de filtro ---

    private boolean matchCidadeEstado(Candidato c, String cidade, String estado) {
        if ((cidade == null || cidade.isBlank()) && (estado == null || estado.isBlank())) return true;
        String c1 = c.getEndereco() != null && c.getEndereco().getCidade() != null ? c.getEndereco().getCidade().trim().toUpperCase(Locale.ROOT) : "";
        String e1 = c.getEndereco() != null && c.getEndereco().getEstado() != null ? c.getEndereco().getEstado().trim().toUpperCase(Locale.ROOT) : "";
        boolean ok = true;
        if (cidade != null && !cidade.isBlank()) ok &= c1.equals(cidade.trim().toUpperCase(Locale.ROOT));
        if (estado != null && !estado.isBlank()) ok &= e1.equals(estado.trim().toUpperCase(Locale.ROOT));
        return ok;
    }

    private boolean matchTecnologia(Candidato c, String tecnologia) {
        if (tecnologia == null || tecnologia.isBlank()) return true;
        String tech = tecnologia.trim().toUpperCase(Locale.ROOT);
        return c.getExperiencias() != null && c.getExperiencias().stream().anyMatch(e ->
                e.getTecnologias() != null && e.getTecnologias().stream()
                        .filter(Objects::nonNull)
                        .map(t -> t.trim().toUpperCase(Locale.ROOT))
                        .anyMatch(t -> t.equals(tech))
        ) || c.getProjetos() != null && c.getProjetos().stream().anyMatch(p ->
                p.getTecnologias() != null && p.getTecnologias().stream()
                        .filter(Objects::nonNull)
                        .map(t -> t.trim().toUpperCase(Locale.ROOT))
                        .anyMatch(t -> t.equals(tech))
        );
    }

    private boolean matchNivelHabilidade(Candidato c, String nivel) {
        if (nivel == null || nivel.isBlank()) return true;
        String nv = nivel.trim().toUpperCase(Locale.ROOT);
        return c.getHabilidadesTecnicas() != null && c.getHabilidadesTecnicas().stream()
                .filter(Objects::nonNull)
                .map(Habilidade::getNivel)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .anyMatch(s -> s.equals(nv));
    }

    private boolean matchIdioma(Candidato c, String idioma, String nivelIdioma) {
        if ((idioma == null || idioma.isBlank()) && (nivelIdioma == null || nivelIdioma.isBlank())) return true;
        return c.getIdiomas() != null && c.getIdiomas().stream().anyMatch(i -> {
            boolean ok = true;
            if (idioma != null && !idioma.isBlank()) ok &= i.getIdioma() != null && i.getIdioma().trim().equalsIgnoreCase(idioma.trim());
            if (nivelIdioma != null && !nivelIdioma.isBlank()) ok &= i.getNivel() != null && i.getNivel().name().equalsIgnoreCase(nivelIdioma.trim());
            return ok;
        });
    }

    // --- Util: deep copy simples (para não expor referências internas do store) ---
    private Candidato deepCopy(Candidato src) {
        // Para MVP, retornamos a própria instância (ou, se preferir, clone superficial).
        // Se quiser cópia profunda real, posso implementar com ModelMapper ou manual.
        return src;
    }
}
