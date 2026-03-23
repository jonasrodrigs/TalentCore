package br.com.talentcore.talentos.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de login.
 * Aceita sinônimos para evitar erros no cliente:
 *  - usuário: "email" ou "username"
 *  - senha:   "password" ou "senha"
 */
public record LoginRequest(
        @NotBlank @Email
        @JsonAlias({"username"}) String email,

        @NotBlank
        @JsonAlias({"senha"}) String password
) { }