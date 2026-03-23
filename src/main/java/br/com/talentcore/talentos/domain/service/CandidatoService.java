package br.com.talentcore.talentos.domain.service;

import br.com.talentcore.talentos.domain.Anexo;
import br.com.talentcore.talentos.domain.Candidato;
import br.com.talentcore.talentos.domain.Certificacao;
import br.com.talentcore.talentos.domain.Conquista;
import br.com.talentcore.talentos.domain.Curso;
import br.com.talentcore.talentos.domain.Experiencia;
import br.com.talentcore.talentos.domain.Formacao;
import br.com.talentcore.talentos.domain.Habilidade;
import br.com.talentcore.talentos.domain.Idioma;
import br.com.talentcore.talentos.domain.Projeto;
import br.com.talentcore.talentos.domain.ReferenciaProfissional;
import br.com.talentcore.talentos.domain.SoftSkill;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * [SERVICE] Regras e validações de domínio do agregado Candidato.
 *
 * Regras aplicadas em validar(Candidato):
 * - nomeCompleto obrigatório
 * - garantir listas não-nulas
 * - coerência de datas (fim >= início) em Formação, Experiência e Certificação
 * - normalização leve (trim e remoção de strings vazias) em tecnologias/realizações
 * - validações de tamanho e normalização para campos do currículo (conforme schema Oracle)
 * - dataNascimento não pode ser futura
 */
public class CandidatoService {

    // Limites (espelham o schema Oracle atual)
    private static final int MAX_OCUPACAO = 120;
    private static final int MAX_LINKS = 300;           // linkedin, github, portfolio
    private static final int MAX_FOTO_URL = 500;        // FOTO_URL
    private static final int MAX_NACIONALIDADE = 80;    // NACIONALIDADE
    private static final int MAX_ESTADO_CIVIL = 50;     // ESTADO_CIVIL
    private static final int MAX_PRETENSAO = 50;        // PRETENSAO_SALARIAL (VARCHAR)
    private static final int MAX_HORARIOS = 255;        // DISPONIBILIDADE.HORARIOS

    /** Valida e normaliza o agregado Candidato. */
    public void validar(Candidato c) {
        if (c == null) {
            throw new IllegalArgumentException("Candidato não pode ser nulo");
        }
        if (isBlank(c.getNomeCompleto())) {
            throw new IllegalArgumentException("nomeCompleto é obrigatório");
        }

        // Data de nascimento (não pode ser futura)
        if (c.getDataNascimento() != null && c.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("dataNascimento não pode ser futura");
        }

        // Normalização de campos simples do currículo
        c.setOcupacao(trimToNull(c.getOcupacao()));
        c.setResumoProfissional(trimToNullPreservandoQuebras(c.getResumoProfissional()));
        c.setLinkedin(trimToNull(c.getLinkedin()));
        c.setGithub(trimToNull(c.getGithub()));
        c.setPortfolio(trimToNull(c.getPortfolio()));
        c.setFotoUrl(trimToNull(c.getFotoUrl()));
        c.setNacionalidade(trimToNull(c.getNacionalidade()));
        c.setEstadoCivil(trimToNull(c.getEstadoCivil()));
        c.setPretensaoSalarial(trimToNull(c.getPretensaoSalarial()));

        // Garantir listas não-nulas
        c.setFormacoes(nullToEmpty(c.getFormacoes()));
        c.setExperiencias(nullToEmpty(c.getExperiencias()));
        c.setHabilidadesTecnicas(nullToEmpty(c.getHabilidadesTecnicas()));
        c.setHabilidadesComportamentais(nullToEmpty(c.getHabilidadesComportamentais()));
        c.setCertificacoes(nullToEmpty(c.getCertificacoes()));
        c.setCursos(nullToEmpty(c.getCursos()));
        c.setIdiomas(nullToEmpty(c.getIdiomas()));
        c.setProjetos(nullToEmpty(c.getProjetos()));
        c.setConquistas(nullToEmpty(c.getConquistas()));
        c.setReferencias(nullToEmpty(c.getReferencias()));
        c.setAnexos(nullToEmpty(c.getAnexos()));

        // Coerência de datas
        validarDatasFormacao(c.getFormacoes());
        validarDatasExperiencia(c.getExperiencias());
        validarDatasCertificacao(c.getCertificacoes());

        // Normalização leve (trim/filtrar vazios) em listas de strings
        normalizarListasTexto(c);

        // Validações de tamanho conforme banco
        checkLength("ocupacao", c.getOcupacao(), MAX_OCUPACAO);
        checkLength("linkedin", c.getLinkedin(), MAX_LINKS);
        checkLength("github", c.getGithub(), MAX_LINKS);
        checkLength("portfolio", c.getPortfolio(), MAX_LINKS);
        checkLength("fotoUrl", c.getFotoUrl(), MAX_FOTO_URL);
        checkLength("nacionalidade", c.getNacionalidade(), MAX_NACIONALIDADE);
        checkLength("estadoCivil", c.getEstadoCivil(), MAX_ESTADO_CIVIL);
        checkLength("pretensaoSalarial", c.getPretensaoSalarial(), MAX_PRETENSAO);

        if (c.getDisponibilidade() != null) {
            // Horários é um campo String dentro de Disponibilidade
            checkLength("disponibilidade.horarios", c.getDisponibilidade().getHorarios(), MAX_HORARIOS);
            // Se houver setters helpers S/N em Disponibilidade, eles serão utilizados em outra camada (repo/mapper)
            if (c.getDisponibilidade().getHorarios() != null) {
                c.getDisponibilidade().setHorarios(trimToNull(c.getDisponibilidade().getHorarios()));
            }
        }
    }

    // --------------------- auxiliares de validação ---------------------

    private void validarDatasFormacao(List<Formacao> lista) {
        for (Formacao f : lista) {
            if (f == null) continue;
            LocalDate ini = f.getDataInicio(); // ajuste conforme seu domínio (getInicio() se for o caso)
            LocalDate fim = f.getDataFim();    // idem
            if (ini != null && fim != null && fim.isBefore(ini)) {
                throw new IllegalArgumentException("Formação com fim antes do início");
            }
        }
    }

    private void validarDatasExperiencia(List<Experiencia> lista) {
        for (Experiencia e : lista) {
            if (e == null) continue;
            LocalDate ini = e.getDataInicio(); // ajuste conforme seu domínio
            LocalDate fim = e.getDataFim();    // ajuste conforme seu domínio
            if (ini != null && fim != null && fim.isBefore(ini)) {
                throw new IllegalArgumentException("Experiência com fim antes do início");
            }
        }
    }

    private void validarDatasCertificacao(List<Certificacao> lista) {
        for (Certificacao ce : lista) {
            if (ce == null) continue;
            LocalDate obt = ce.getDataObtencao();
            LocalDate exp = ce.getDataExpiracao();
            if (obt != null && exp != null && exp.isBefore(obt)) {
                throw new IllegalArgumentException("Certificação com expiração antes da obtenção");
            }
        }
    }

    private void normalizarListasTexto(Candidato c) {
        // tecnologias/realizações das experiências
        for (Experiencia e : c.getExperiencias()) {
            if (e == null) continue;
            e.setTecnologias(limparListaStrings(e.getTecnologias()));
            e.setRealizacoes(limparListaStrings(e.getRealizacoes()));
        }
        // tecnologias dos projetos
        for (Projeto p : c.getProjetos()) {
            if (p == null) continue;
            p.setTecnologias(limparListaStrings(p.getTecnologias()));
        }
    }

    private List<String> limparListaStrings(List<String> lista) {
        List<String> out = new ArrayList<>();
        if (lista == null) return out;
        for (String s : lista) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return (list == null) ? new ArrayList<>() : list;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void checkLength(String field, String value, int max) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(field + " excede o tamanho máximo de " + max + " caracteres");
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Apenas remove espaços das bordas, preservando quebras de linha internas do resumo */
    private String trimToNullPreservandoQuebras(String s) {
        if (s == null) return null;
        String t = s.strip(); // remove espaços nas bordas, preserva conteúdo interno
        return t.isEmpty() ? null : t;
        // Nota: não fazemos normalização de \n para não afetar a exibição com white-space: pre-line no front
    }
}