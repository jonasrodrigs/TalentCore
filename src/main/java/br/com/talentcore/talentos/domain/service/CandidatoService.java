package br.com.talentcore.talentos.domain.service;

import br.com.talentcore.talentos.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class CandidatoService {

    /**
     * Valida um candidato completo antes de salvar.
     * Regras principais:
     * - nome obrigatório
     * - datas de início/fim coerentes em experiências e formações
     * - listas não nulas
     * - normalização simples de tecnologias (trim)
     */
    public void validarAntesDeSalvar(Candidato c) {
        Objects.requireNonNull(c, "Candidato não pode ser nulo");

        // Nome obrigatório
        if (isBlank(c.getNomeCompleto())) {
            throw new IllegalArgumentException("Nome do candidato é obrigatório.");
        }

        // Garante que listas não sejam nulas
        garantirListas(c);

        // Valida Experiências
        if (c.getExperiencias() != null) {
            for (Experiencia e : c.getExperiencias()) {
                if (e == null) continue;

                LocalDate ini = e.getDataInicio();
                LocalDate fim = e.getDataFim();
                if (ini != null && fim != null && fim.isBefore(ini)) {
                    throw new IllegalArgumentException("Experiência '" + safe(e.getEmpresa())
                            + "' com dataFim antes de dataInicio.");
                }

                // Normaliza tecnologias e realizações
                if (e.getTecnologias() != null) {
                    e.getTecnologias().replaceAll(this::normalize);
                }
                if (e.getRealizacoes() != null) {
                    e.getRealizacoes().replaceAll(this::normalize);
                }
            }
        }

        // Valida Formações
        if (c.getFormacoes() != null) {
            for (Formacao f : c.getFormacoes()) {
                if (f == null) continue;

                LocalDate ini = f.getDataInicio();
                LocalDate fim = f.getDataFim();
                if (ini != null && fim != null && fim.isBefore(ini)) {
                    throw new IllegalArgumentException("Formação '" + safe(f.getCurso())
                            + "' com dataFim antes de dataInicio.");
                }
            }
        }

        // Valida Certificações (expiração >= obtenção, quando houver)
        if (c.getCertificacoes() != null) {
            for (Certificacao cert : c.getCertificacoes()) {
                if (cert == null) continue;
                LocalDate obt = cert.getDataObtencao();
                LocalDate exp = cert.getDataExpiracao();
                if (obt != null && exp != null && exp.isBefore(obt)) {
                    throw new IllegalArgumentException("Certificação '" + safe(cert.getNome())
                            + "' com dataExpiracao antes de dataObtencao.");
                }
            }
        }

        // (Opcional) Regras adicionais:
        // - E-mail válido (se desejar)
        // - Pretensão salarial num formato específico
        // - Limites de tamanho em campos de descrição
    }

    private void garantirListas(Candidato c) {
        if (c.getFormacoes() == null) c.setFormacoes(List.of());
        if (c.getExperiencias() == null) c.setExperiencias(List.of());
        if (c.getHabilidadesTecnicas() == null) c.setHabilidadesTecnicas(List.of());
        if (c.getHabilidadesComportamentais() == null) c.setHabilidadesComportamentais(List.of());
        if (c.getCertificacoes() == null) c.setCertificacoes(List.of());
        if (c.getCursos() == null) c.setCursos(List.of());
        if (c.getIdiomas() == null) c.setIdiomas(List.of());
        if (c.getProjetos() == null) c.setProjetos(List.of());
        if (c.getConquistas() == null) c.setConquistas(List.of());
        if (c.getReferencias() == null) c.setReferencias(List.of());
        if (c.getAnexos() == null) c.setAnexos(List.of());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }
}