package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.Usuario;
import br.pucminas.alugacar.model.enums.Papel;
import br.pucminas.alugacar.model.enums.TipoAgente;
import jakarta.validation.constraints.NotBlank;

/** DTOs de autenticacao. */
public final class AuthDTOs {

    private AuthDTOs() {
    }

    public record LoginRequest(@NotBlank(message = "Informe o usuario") String login,
                               @NotBlank(message = "Informe a senha") String senha) {
    }

    public record LoginResponse(String token, UsuarioResponse usuario) {
    }

    public record UsuarioResponse(Long id, String nome, String login, String email,
                                  Papel papel, TipoAgente tipoAgente) {
        public static UsuarioResponse de(Usuario u) {
            TipoAgente tipo = (u instanceof Agente a) ? a.getTipo() : null;
            return new UsuarioResponse(u.getId(), u.getNome(), u.getLogin(), u.getEmail(), u.getPapel(), tipo);
        }
    }
}
