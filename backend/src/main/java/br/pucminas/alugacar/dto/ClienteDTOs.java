package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.model.Cliente;
import br.pucminas.alugacar.model.Rendimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** DTOs do CRUD de cliente. */
public final class ClienteDTOs {

    private ClienteDTOs() {
    }

    public record RendimentoDTO(
            @NotBlank(message = "Informe a entidade empregadora") @Size(max = 120) String entidadeEmpregadora,
            @NotNull(message = "Informe o valor do rendimento") @PositiveOrZero BigDecimal valor) {

        public static RendimentoDTO de(Rendimento r) {
            return new RendimentoDTO(r.getEntidadeEmpregadora(), r.getValor());
        }
    }

    /**
     * Dados de cadastro/atualizacao do cliente.
     * Na atualizacao, "login" e ignorado e "senha" e opcional (em branco = mantem a atual).
     */
    public record ClienteRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o usuario") @Size(min = 3, max = 60) String login,
            @Size(max = 100) String senha,
            @Email(message = "E-mail invalido") @Size(max = 120) String email,
            @NotBlank(message = "Informe o RG") @Size(max = 20) String rg,
            @NotBlank(message = "Informe o CPF")
            @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF invalido") String cpf,
            @NotBlank(message = "Informe o endereco") @Size(max = 255) String endereco,
            @Size(max = 80) String profissao,
            @Size(max = Cliente.MAX_RENDIMENTOS, message = "Maximo de 3 rendimentos")
            List<@Valid RendimentoDTO> rendimentos) {
    }

    public record ClienteResponse(Long id, String nome, String login, String email, String rg, String cpf,
                                  String endereco, String profissao, List<RendimentoDTO> rendimentos,
                                  BigDecimal rendaTotal) {
        public static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNome(), c.getLogin(), c.getEmail(), c.getRg(), c.getCpf(),
                    c.getEndereco(), c.getProfissao(),
                    c.getRendimentos().stream().map(RendimentoDTO::de).toList(), c.getRendaTotal());
        }
    }
}
