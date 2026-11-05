package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelResponse;
import br.pucminas.alugacar.model.Cliente;
import br.pucminas.alugacar.model.PedidoAluguel;
import br.pucminas.alugacar.model.enums.StatusPedido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTOs de pedidos de aluguel. */
public final class PedidoDTOs {

    private PedidoDTOs() {
    }

    public record PedidoRequest(
            @NotNull(message = "Selecione o automovel") Long automovelId,
            @NotNull(message = "Informe a data de inicio") LocalDate dataInicio,
            @NotNull(message = "Informe a data de fim") LocalDate dataFim,
            @Size(max = 500) String observacoes,
            Boolean solicitaCredito) {

        public boolean querCredito() {
            return Boolean.TRUE.equals(solicitaCredito);
        }
    }

    /** Parecer financeiro do agente. */
    public record AvaliacaoRequest(
            @NotNull(message = "Informe se o parecer e positivo") Boolean aprovado,
            @NotBlank(message = "Informe o parecer") @Size(max = 500) String parecer) {
    }

    public record ClienteResumo(Long id, String nome, String cpf, String profissao, BigDecimal rendaTotal) {
        static ClienteResumo de(Cliente c) {
            return new ClienteResumo(c.getId(), c.getNome(), c.getCpf(), c.getProfissao(), c.getRendaTotal());
        }
    }

    public record PedidoResponse(Long id, ClienteResumo cliente, AutomovelResponse automovel,
                                 LocalDate dataInicio, LocalDate dataFim, long quantidadeDias,
                                 LocalDateTime dataCriacao, StatusPedido status, String observacoes,
                                 boolean solicitaCredito, String agenteAvaliador, String parecer,
                                 LocalDateTime dataAvaliacao, Long contratoId) {
        public static PedidoResponse de(PedidoAluguel p, Long contratoId) {
            return new PedidoResponse(p.getId(), ClienteResumo.de(p.getCliente()),
                    AutomovelResponse.de(p.getAutomovel()), p.getDataInicio(), p.getDataFim(),
                    p.getQuantidadeDias(), p.getDataCriacao(), p.getStatus(), p.getObservacoes(),
                    p.isSolicitaCredito(),
                    p.getAgenteAvaliador() == null ? null : p.getAgenteAvaliador().getNome(),
                    p.getParecer(), p.getDataAvaliacao(), contratoId);
        }
    }
}
