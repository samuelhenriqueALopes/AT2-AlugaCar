package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.enums.TipoAgente;
import jakarta.validation.constraints.*;

/** DTOs de agentes (empresas e bancos). */
public final class AgenteDTOs {

    private AgenteDTOs() {
    }

    public record AgenteRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o usuario") @Size(min = 3, max = 60) String login,
            @NotBlank(message = "Informe a senha") @Size(min = 6, max = 100) String senha,
            @Email(message = "E-mail invalido") @Size(max = 120) String email,
            @NotBlank(message = "Informe o CNPJ")
            @Pattern(regexp = "\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}", message = "CNPJ invalido") String cnpj,
            @NotNull(message = "Informe o tipo do agente") TipoAgente tipo) {
    }

    public record AgenteResponse(Long id, String nome, String login, String email, String cnpj, TipoAgente tipo) {
        public static AgenteResponse de(Agente a) {
            return new AgenteResponse(a.getId(), a.getNome(), a.getLogin(), a.getEmail(), a.getCnpj(), a.getTipo());
        }
    }
}
