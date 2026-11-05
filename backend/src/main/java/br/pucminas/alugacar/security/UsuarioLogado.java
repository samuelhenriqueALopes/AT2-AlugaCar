package br.pucminas.alugacar.security;

import br.pucminas.alugacar.model.enums.Papel;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/** Dados do usuario autenticado extraidos do token JWT. */
public record UsuarioLogado(Long id, String login, List<String> roles) {

    public static UsuarioLogado de(Jwt jwt) {
        Object uid = jwt.getClaim("uid");
        Long id = (uid instanceof Number n) ? n.longValue() : Long.valueOf(String.valueOf(uid));
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new UsuarioLogado(id, jwt.getSubject(), roles == null ? List.of() : roles);
    }

    public boolean isAgente() {
        return roles.contains(Papel.AGENTE.name());
    }

    public boolean isCliente() {
        return roles.contains(Papel.CLIENTE.name());
    }
}
