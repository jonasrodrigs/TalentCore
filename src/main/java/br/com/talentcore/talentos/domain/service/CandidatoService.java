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
 */
public class CandidatoService {

    /** Valida e normaliza o agregado Candidato. */
    public void validar(Candidato c) {
        if (c == null) {
            throw new IllegalArgumentException("Candidato não pode ser nulo");
        }
        if (isBlank(c.getNomeCompleto())) {
            throw new IllegalArgumentException("nomeCompleto é obrigatório");
        }

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

        // Normalização leve (trim/filtrar vazios)
        normalizarListasTexto(c);
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
}