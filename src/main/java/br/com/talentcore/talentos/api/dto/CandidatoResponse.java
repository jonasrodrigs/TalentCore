package br.com.talentcore.talentos.api.dto;

import java.util.List;

public class CandidatoResponse {
    public String id;
    public String nomeCompleto;
    public String email;
    public String cidade;
    public String estado;
    public String pais;
    public List<String> habilidades;
    public List<String> tecnologias;
    public List<String> idiomas;
}