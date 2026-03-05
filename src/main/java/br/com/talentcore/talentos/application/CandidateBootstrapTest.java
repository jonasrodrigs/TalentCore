package br.com.talentcore.talentos.application;

import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.in.CadastrarCandidatoUseCase;
import br.com.talentcore.talentos.application.port.in.BuscarCandidatoUseCase.Filtros;
import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.domain.Candidato;               // [ENTIDADE]
import br.com.talentcore.talentos.domain.Contato;               // [VO]
import br.com.talentcore.talentos.domain.Endereco;              // [VO]
import br.com.talentcore.talentos.domain.Experiencia;           // [ENTIDADE]
import br.com.talentcore.talentos.domain.Habilidade;            // [ENTIDADE]
import br.com.talentcore.talentos.domain.Idioma;                // [ENTIDADE]
import br.com.talentcore.talentos.domain.NivelConhecimento;     // [ENUM]
import br.com.talentcore.talentos.domain.NivelIdioma;           // [ENUM]
import br.com.talentcore.talentos.domain.Projeto;               // [ENTIDADE]
import br.com.talentcore.talentos.domain.TipoContratacao;       // [ENUM]

// Use Oracle (JDBC). Se quiser rodar in-memory, troque o import/instanciação.
import br.com.talentcore.talentos.infrastructure.persistence.CandidatoRepositoryOracle;
// import br.com.talentcore.talentos.infrastructure.persistence.mvp.CandidatoRepositoryInMemory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CandidateBootstrapTest {

    public static void main(String[] args) {
        System.out.println("=== TalentCore :: Bootstrap (Oracle JDBC, Java 17) ===");

        // 1) Repositório Oracle (JDBC)
        //    Certifique-se de ter configurado as variáveis (Run/Debug → Environment):
        //    - TC_DB_URL=jdbc:oracle:thin:@//192.168.101.10:1521/XEPDB1
        //    - TC_DB_USER=TALENTCORE
        //    - TC_DB_PASSWORD=<sua_senha>
        CandidatoRepository repo = new CandidatoRepositoryOracle();
        // Para rodar in-memory, use:
        // CandidatoRepository repo = new CandidatoRepositoryInMemory();

        // 2) Casos de uso
        CadastrarCandidatoUseCase cadastrar = new CadastrarCandidatoService(repo);
        BuscarCandidatoUseCase buscar = new BuscarCandidatoService(repo);

        // 3) Criar candidatos (e-mails sempre únicos por execução)
        Candidato c1 = criarCandidatoAngularBarueri();
        Candidato c2 = criarCandidatoJava17();
        Candidato c3 = criarCandidatoFluenteIngles();

        // 4) Cadastrar e imprimir ids
        String id1 = cadastrar.executar(c1);
        String id2 = cadastrar.executar(c2);
        String id3 = cadastrar.executar(c3);
        System.out.println("IDs cadastrados: " + Arrays.asList(id1, id2, id3));

        // 5) Buscar por ID do primeiro e imprimir resumo
        Optional<Candidato> opt = repo.buscarPorId(id1);
        if (opt.isPresent()) {
            Candidato found = opt.get();
            String email  = (found.getContato()  != null ? found.getContato().getEmail()  : "");
            String cidade = (found.getEndereco() != null ? found.getEndereco().getCidade() : "");
            String estado = (found.getEndereco() != null ? found.getEndereco().getEstado() : "");
            System.out.println("[RESUMO] " + found.getNomeCompleto() + " | " + email + " | " + cidade + "/" + estado);
        }

        // 6) Buscar por filtros e imprimir
        // cidade=BARUERI, estado=SP
        Filtros f1 = new Filtros();
        f1.cidade = "BARUERI";
        f1.estado = "SP";
        List<Candidato> resp1 = buscar.executar(f1);
        System.out.println("[BUSCA cidade=BARUERI, estado=SP] total=" + resp1.size() + " nomes=" + nomes(resp1));

        // tecnologia=ANGULAR
        Filtros f2 = new Filtros();
        f2.tecnologia = "ANGULAR";
        List<Candidato> resp2 = buscar.executar(f2);
        System.out.println("[BUSCA tecnologia=ANGULAR] total=" + resp2.size() + " nomes=" + nomes(resp2));

        // idioma=EN-US
        Filtros f3 = new Filtros();
        f3.idioma = "EN-US";
        List<Candidato> resp3 = buscar.executar(f3);
        System.out.println("[BUSCA idioma=EN-US] total=" + resp3.size() + " nomes=" + nomes(resp3));

        System.out.println("=== FIM ===");
    }

    // ------------------------------------------------------------
    // Helper: gera e-mails únicos por execução (nome+<sufixo>@dominio)
    // ------------------------------------------------------------
    private static String uniqueEmail(String baseEmail) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        int at = baseEmail.indexOf('@');
        if (at <= 0) return baseEmail + "+" + suffix; // fallback
        return baseEmail.substring(0, at) + "+" + suffix + baseEmail.substring(at);
    }

    private static String nomes(List<Candidato> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(lista.get(i).getNomeCompleto());
            if (i < lista.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    // ------------------------------------------------------------
    // Criação dos candidatos (setters + garantir listas antes de add)
    // ------------------------------------------------------------

    private static Candidato criarCandidatoAngularBarueri() {
        Candidato c = new Candidato();
        c.setNomeCompleto("Alice Front");
        c.setDataNascimento(LocalDate.of(1995, 5, 20));

        Contato contato = new Contato();
        contato.setTelefone("11-90000-0001");
        // E-mail único por execução
        contato.setEmail(uniqueEmail("alicie.front@talentcore.dev"));
        c.setContato(contato);

        Endereco end = new Endereco();
        end.setCidade("BARUERI");
        end.setEstado("SP");
        end.setPais("BRASIL");
        c.setEndereco(end);

        ensureLists(c);

        // Experiência com ANGULAR
        Experiencia exp = new Experiencia();
        exp.setEmpresa("TechX");
        exp.setCargo("Frontend Engineer");
        exp.setTipo(TipoContratacao.CLT);
        exp.setDataInicio(LocalDate.of(2023, 1, 1));
        exp.setDataFim(LocalDate.of(2025, 1, 1));
        if (exp.getTecnologias() == null) exp.setTecnologias(new ArrayList<>());
        exp.getTecnologias().add("ANGULAR");
        exp.getTecnologias().add("TYPESCRIPT");
        c.getExperiencias().add(exp);

        // Habilidade técnica
        Habilidade h = new Habilidade();
        h.setNome("Angular");
        h.setCategoria("Frontend");
        h.setNivel(NivelConhecimento.AVANCADO);
        c.getHabilidadesTecnicas().add(h);

        // Idiomas
        Idioma pt = new Idioma();
        pt.setIdioma("PT-BR");
        pt.setNivel(NivelIdioma.NATIVO);
        c.getIdiomas().add(pt);

        Idioma en = new Idioma();
        en.setIdioma("EN-US");
        en.setNivel(NivelIdioma.AVANCADO);
        c.getIdiomas().add(en);

        // Projeto
        Projeto p = new Projeto();
        p.setNome("Portal Clientes");
        p.setDataInicio(LocalDate.of(2024, 1, 1));
        p.setDataFim(LocalDate.of(2024, 12, 31));
        if (p.getTecnologias() == null) p.setTecnologias(new ArrayList<>());
        p.getTecnologias().add("ANGULAR");
        p.getTecnologias().add("RXJS");
        c.getProjetos().add(p);

        return c;
    }

    private static Candidato criarCandidatoJava17() {
        Candidato c = new Candidato();
        c.setNomeCompleto("Bruno Backend");
        c.setDataNascimento(LocalDate.of(1990, 3, 10));

        Contato contato = new Contato();
        contato.setTelefone("11-90000-0002");
        contato.setEmail(uniqueEmail("bruno.cackend@talentcore.dev")); // único
        c.setContato(contato);

        Endereco end = new Endereco();
        end.setCidade("SAO PAULO");
        end.setEstado("SP");
        end.setPais("BRASIL");
        c.setEndereco(end);

        ensureLists(c);

        Experiencia exp = new Experiencia();
        exp.setEmpresa("FinBank");
        exp.setCargo("Software Engineer");
        exp.setTipo(TipoContratacao.PJ);
        exp.setDataInicio(LocalDate.of(2022, 1, 1));
        exp.setDataFim(LocalDate.of(2025, 6, 1));
        if (exp.getTecnologias() == null) exp.setTecnologias(new ArrayList<>());
        exp.getTecnologias().add("JAVA 17");
        exp.getTecnologias().add("MICROSERVICES");
        c.getExperiencias().add(exp);

        Habilidade h = new Habilidade();
        h.setNome("Java");
        h.setCategoria("Backend");
        h.setNivel(NivelConhecimento.EXPERT);
        c.getHabilidadesTecnicas().add(h);

        Idioma pt = new Idioma();
        pt.setIdioma("PT-BR");
        pt.setNivel(NivelIdioma.NATIVO);
        c.getIdiomas().add(pt);

        Projeto p = new Projeto();
        p.setNome("Core Banking");
        p.setDataInicio(LocalDate.of(2023, 2, 1));
        p.setDataFim(LocalDate.of(2024, 8, 30));
        if (p.getTecnologias() == null) p.setTecnologias(new ArrayList<>());
        p.getTecnologias().add("JAVA 17");
        p.getTecnologias().add("KAFKA");
        c.getProjetos().add(p);

        return c;
    }

    private static Candidato criarCandidatoFluenteIngles() {
        Candidato c = new Candidato();
        c.setNomeCompleto("Carla Fullstack");
        c.setDataNascimento(LocalDate.of(1992, 8, 15));

        Contato contato = new Contato();
        contato.setTelefone("11-90000-0003");
        contato.setEmail(uniqueEmail("carla.fullstack@talentcore.dev")); // único
        c.setContato(contato);

        Endereco end = new Endereco();
        end.setCidade("OSASCO");
        end.setEstado("SP");
        end.setPais("BRASIL");
        c.setEndereco(end);

        ensureLists(c);

        Experiencia exp = new Experiencia();
        exp.setEmpresa("WebStart");
        exp.setCargo("Fullstack Dev");
        exp.setTipo(TipoContratacao.CLT);
        exp.setDataInicio(LocalDate.of(2021, 4, 1));
        exp.setDataFim(LocalDate.of(2024, 12, 1));
        if (exp.getTecnologias() == null) exp.setTecnologias(new ArrayList<>());
        exp.getTecnologias().add("NODEJS");
        exp.getTecnologias().add("ANGULAR");
        c.getExperiencias().add(exp);

        Habilidade h1 = new Habilidade();
        h1.setNome("Angular");
        h1.setCategoria("Frontend");
        h1.setNivel(NivelConhecimento.INTERMEDIARIO);
        c.getHabilidadesTecnicas().add(h1);

        Habilidade h2 = new Habilidade();
        h2.setNome("Java");
        h2.setCategoria("Backend");
        h2.setNivel(NivelConhecimento.AVANCADO);
        c.getHabilidadesTecnicas().add(h2);

        Idioma en = new Idioma();
        en.setIdioma("EN-US");
        en.setNivel(NivelIdioma.FLUENTE);
        c.getIdiomas().add(en);

        Idioma pt = new Idioma();
        pt.setIdioma("PT-BR");
        pt.setNivel(NivelIdioma.NATIVO);
        c.getIdiomas().add(pt);

        Projeto p = new Projeto();
        p.setNome("Marketplace");
        p.setDataInicio(LocalDate.of(2022, 6, 1));
        p.setDataFim(LocalDate.of(2023, 12, 31));
        if (p.getTecnologias() == null) p.setTecnologias(new ArrayList<>());
        p.getTecnologias().add("ANGULAR");
        p.getTecnologias().add("JAVA 8");
        c.getProjetos().add(p);

        return c;
    }

    /** Garante que todas as listas do agregado não estejam nulas antes dos add() no bootstrap. */
    private static void ensureLists(Candidato c) {
        if (c.getFormacoes() == null) c.setFormacoes(new ArrayList<>());
        if (c.getExperiencias() == null) c.setExperiencias(new ArrayList<>());
        if (c.getHabilidadesTecnicas() == null) c.setHabilidadesTecnicas(new ArrayList<>());
        if (c.getHabilidadesComportamentais() == null) c.setHabilidadesComportamentais(new ArrayList<>());
        if (c.getCertificacoes() == null) c.setCertificacoes(new ArrayList<>());
        if (c.getCursos() == null) c.setCursos(new ArrayList<>());
        if (c.getIdiomas() == null) c.setIdiomas(new ArrayList<>());
        if (c.getProjetos() == null) c.setProjetos(new ArrayList<>());
        if (c.getConquistas() == null) c.setConquistas(new ArrayList<>());
        if (c.getReferencias() == null) c.setReferencias(new ArrayList<>());
        if (c.getAnexos() == null) c.setAnexos(new ArrayList<>());
    }
}