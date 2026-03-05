package br.com.talentcore.talentos.api.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CandidatoRequest {

    @NotBlank(message = "nomeCompleto é obrigatório")
    @Size(min = 3, max = 120, message = "nomeCompleto deve ter entre 3 e 120 caracteres")
    public String nomeCompleto;

    @NotNull(message = "contato é obrigatório")
    public ContatoDTO contato;

    @NotNull(message = "endereco é obrigatório")
    public EnderecoDTO endereco;

    public List<IdiomaDTO> idiomas;
    public List<HabilidadeDTO> habilidadesTecnicas;
    public List<ExperienciaDTO> experiencias;
    public List<ProjetoDTO> projetos;

    public static class ContatoDTO {
        @Pattern(regexp = "^[0-9\\-\\s+()]{8,20}$", message = "telefone inválido")
        public String telefone;

        @NotBlank @Email
        public String email;
    }

    public static class EnderecoDTO {
        @Size(max = 120) public String logradouro;
        @Size(max = 20)  public String numero;
        @Size(max = 80)  public String complemento;
        @Size(max = 80)  public String bairro;

        @NotBlank public String cidade;
        @NotBlank public String estado;
        @NotBlank public String pais;

        @Size(max = 12) public String cep;
    }

    public static class IdiomaDTO {
        @NotBlank public String idioma; // PT-BR, EN-US...
        @NotBlank public String nivel;  // BASICO, INTERMEDIARIO, AVANCADO, FLUENTE, NATIVO
    }

    public static class HabilidadeDTO {
        @NotBlank public String nome;
        @NotBlank public String categoria;
        @NotBlank public String nivel; // BASICO|INTERMEDIARIO|AVANCADO|EXPERT
    }

    public static class ExperienciaDTO {
        @NotBlank public String empresa;
        @NotBlank public String cargo;
        @NotBlank public String tipo; // CLT|PJ|ESTAGIO|FREELANCE

        // ISO-8601: YYYY-MM-DD
        public String dataInicio;
        public String dataFim;

        public List<String> tecnologias;
    }

    public static class ProjetoDTO {
        @NotBlank public String nome;
        public String dataInicio;
        public String dataFim;
        public List<String> tecnologias;
    }
}
