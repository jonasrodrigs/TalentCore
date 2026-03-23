package br.com.talentcore.talentos.api.mapper;

import br.com.talentcore.talentos.api.dto.CandidatoRequest;
import br.com.talentcore.talentos.api.dto.CandidatoResponse;
import br.com.talentcore.talentos.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CandidatoMapper {

    // ----------------------
    // Request → Domain
    // (mantido: não adiciona campos novos que não existam no request atual)
    // ----------------------
    public static Candidato fromRequest(CandidatoRequest req) {
        if (req == null) return null;

        Candidato c = new Candidato();
        c.setNomeCompleto(req.nomeCompleto);

        // Contato
        if (req.contato != null) {
            Contato contato = new Contato();
            contato.setTelefone(req.contato.telefone);
            contato.setEmail(req.contato.email);
            c.setContato(contato);
        }

        // Endereço
        if (req.endereco != null) {
            Endereco end = new Endereco();
            end.setLogradouro(req.endereco.logradouro);
            end.setNumero(req.endereco.numero);
            end.setComplemento(req.endereco.complemento);
            end.setBairro(req.endereco.bairro);
            end.setCidade(req.endereco.cidade);
            end.setEstado(req.endereco.estado);
            end.setPais(req.endereco.pais);
            end.setCep(req.endereco.cep);
            c.setEndereco(end);
        }

        // Idiomas
        if (req.idiomas != null) {
            List<Idioma> idiomas = new ArrayList<>();
            for (CandidatoRequest.IdiomaDTO dto : req.idiomas) {
                Idioma id = new Idioma();
                id.setIdioma(dto.idioma);
                if (dto.nivel != null) id.setNivel(NivelIdioma.valueOf(dto.nivel));
                idiomas.add(id);
            }
            c.setIdiomas(idiomas);
        }

        // Habilidades Técnicas
        if (req.habilidadesTecnicas != null) {
            List<Habilidade> habs = new ArrayList<>();
            for (CandidatoRequest.HabilidadeDTO dto : req.habilidadesTecnicas) {
                Habilidade h = new Habilidade();
                h.setNome(dto.nome);
                h.setCategoria(dto.categoria);
                if (dto.nivel != null) h.setNivel(NivelConhecimento.valueOf(dto.nivel));
                habs.add(h);
            }
            c.setHabilidadesTecnicas(habs);
        }

        // Experiências
        if (req.experiencias != null) {
            List<Experiencia> exps = new ArrayList<>();
            for (CandidatoRequest.ExperienciaDTO dto : req.experiencias) {
                Experiencia e = new Experiencia();
                e.setEmpresa(dto.empresa);
                e.setCargo(dto.cargo);
                if (dto.tipo != null) e.setTipo(TipoContratacao.valueOf(dto.tipo));
                if (dto.dataInicio != null) e.setDataInicio(LocalDate.parse(dto.dataInicio));
                if (dto.dataFim != null) e.setDataFim(LocalDate.parse(dto.dataFim));
                e.setTecnologias(dto.tecnologias);
                exps.add(e);
            }
            c.setExperiencias(exps);
        }

        // Projetos
        if (req.projetos != null) {
            List<Projeto> ps = new ArrayList<>();
            for (CandidatoRequest.ProjetoDTO dto : req.projetos) {
                Projeto p = new Projeto();
                p.setNome(dto.nome);
                if (dto.dataInicio != null) p.setDataInicio(LocalDate.parse(dto.dataInicio));
                if (dto.dataFim != null) p.setDataFim(LocalDate.parse(dto.dataFim));
                p.setTecnologias(dto.tecnologias);
                ps.add(p);
            }
            c.setProjetos(ps);
        }

        return c;
    }

    // ----------------------
    // Domain → Response
    // (ATUALIZADO com campos de currículo e disponibilidade)
    // ----------------------
    public static CandidatoResponse toResponse(Candidato c) {
        if (c == null) return null;

        CandidatoResponse r = new CandidatoResponse();

        // Identificação
        r.id = c.getId();
        r.nomeCompleto = c.getNomeCompleto();
        r.dataNascimento = (c.getDataNascimento() != null) ? c.getDataNascimento().toString() : null; // ISO yyyy-MM-dd

        // Contato
        r.email = (c.getContato() != null) ? c.getContato().getEmail() : null;
        r.telefone = (c.getContato() != null) ? c.getContato().getTelefone() : null;

        // Endereço (flat)
        r.bairro = (c.getEndereco() != null) ? c.getEndereco().getBairro() : null;
        r.cidade = (c.getEndereco() != null) ? c.getEndereco().getCidade() : null;
        r.estado = (c.getEndereco() != null) ? c.getEndereco().getEstado() : null;
        r.pais   = (c.getEndereco() != null) ? c.getEndereco().getPais() : null;

        // Currículo (universais)
        r.ocupacao = c.getOcupacao();
        r.resumoProfissional = c.getResumoProfissional();

        // Links / Mídia
        r.linkedin = c.getLinkedin();
        r.github   = c.getGithub();
        r.portfolio= c.getPortfolio();
        r.fotoUrl  = c.getFotoUrl();

        // Dados pessoais
        r.nacionalidade = c.getNacionalidade();
        r.estadoCivil   = c.getEstadoCivil();

        // Disponibilidade / objetivo
        r.pretensaoSalarial = c.getPretensaoSalarial();
        if (c.getDisponibilidade() != null) {
            if (c.getDisponibilidade() instanceof Disponibilidade) {
                Disponibilidade d = c.getDisponibilidade();
                try {
                    r.aceitaViagens = d.getAceitaViagensNullable();
                    r.aceitaMudanca = d.getAceitaMudancaNullable();
                } catch (Throwable ignore) {
                    r.aceitaViagens = d.isAceitaViagens();
                    r.aceitaMudanca = d.isAceitaMudanca();
                }
                r.horarios = d.getHorarios();
            }
        }

        // Habilidades (nomes)
        r.habilidades = (c.getHabilidadesTecnicas() != null)
                ? c.getHabilidadesTecnicas().stream().map(Habilidade::getNome).collect(Collectors.toList())
                : List.of();

        // Tecnologias (agregadas de experiências e projetos)
        List<String> tecnologias = new ArrayList<>();
        if (c.getExperiencias() != null) {
            c.getExperiencias().forEach(e -> {
                if (e.getTecnologias() != null) tecnologias.addAll(e.getTecnologias());
            });
        }
        if (c.getProjetos() != null) {
            c.getProjetos().forEach(p -> {
                if (p.getTecnologias() != null) tecnologias.addAll(p.getTecnologias());
            });
        }
        r.tecnologias = tecnologias;

        // Idiomas (string)
        r.idiomas = (c.getIdiomas() != null)
                ? c.getIdiomas().stream().map(Idioma::getIdioma).collect(Collectors.toList())
                : List.of();

        // Experiências / Projetos (detalhe)
        r.experiencias = c.getExperiencias();
        r.projetos     = c.getProjetos();

        return r;
    }

    // ----------------------
    // merge() – atualização parcial
    // (mantido: não referencia campos que não existam no request atual)
    // ----------------------
    public static void merge(Candidato atual, CandidatoRequest req) {
        if (req == null) return;

        if (req.nomeCompleto != null) atual.setNomeCompleto(req.nomeCompleto);

        // contato
        if (req.contato != null) {
            if (atual.getContato() == null) atual.setContato(new Contato());
            if (req.contato.telefone != null)
                atual.getContato().setTelefone(req.contato.telefone);
            if (req.contato.email != null)
                atual.getContato().setEmail(req.contato.email);
        }

        // endereço
        if (req.endereco != null) {
            if (atual.getEndereco() == null) atual.setEndereco(new Endereco());
            if (req.endereco.logradouro != null)
                atual.getEndereco().setLogradouro(req.endereco.logradouro);
            if (req.endereco.numero != null)
                atual.getEndereco().setNumero(req.endereco.numero);
            if (req.endereco.complemento != null)
                atual.getEndereco().setComplemento(req.endereco.complemento);
            if (req.endereco.bairro != null)
                atual.getEndereco().setBairro(req.endereco.bairro);
            if (req.endereco.cidade != null)
                atual.getEndereco().setCidade(req.endereco.cidade);
            if (req.endereco.estado != null)
                atual.getEndereco().setEstado(req.endereco.estado);
            if (req.endereco.pais != null)
                atual.getEndereco().setPais(req.endereco.pais);
            if (req.endereco.cep != null)
                atual.getEndereco().setCep(req.endereco.cep);
        }

        // >>> NOVO: foto
        if (req.fotoUrl != null) {
            atual.setFotoUrl(req.fotoUrl);
        }

        // substituições completas (mantidas)
        if (req.idiomas != null) atual.setIdiomas(fromRequest(req).getIdiomas());
        if (req.habilidadesTecnicas != null) atual.setHabilidadesTecnicas(fromRequest(req).getHabilidadesTecnicas());
        if (req.experiencias != null) atual.setExperiencias(fromRequest(req).getExperiencias());
        if (req.projetos != null) atual.setProjetos(fromRequest(req).getProjetos());
    }
}