package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelResponse;
import br.pucminas.alugacar.model.Contrato;
import br.pucminas.alugacar.model.ContratoCredito;
import br.pucminas.alugacar.model.enums.TipoAgente;
import br.pucminas.alugacar.model.enums.TipoPropriedade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTOs de contratos e contratos de credito. */
public final class ContratoDTOs {

    private ContratoDTOs() {
    }

    public record CreditoRequest(
            @NotNull(message = "Informe o valor do credito") @Positive BigDecimal valor,
            @NotNull(message = "Informe a taxa de juros") @PositiveOrZero @DecimalMax("100") BigDecimal taxaJurosMensal,
            @NotNull(message = "Informe o numero de parcelas") @Min(1) @Max(120) Integer numeroParcelas) {
    }

    /** Execucao do contrato. "credito" e opcional e so pode ser informado por bancos. */
    public record ContratoRequest(
            @NotNull(message = "Informe o tipo de propriedade") TipoPropriedade tipoPropriedade,
            @Valid CreditoRequest credito) {
    }

    public record CreditoResponse(Long id, String banco, BigDecimal valor, BigDecimal taxaJurosMensal,
                                  Integer numeroParcelas, BigDecimal valorParcela) {
        static CreditoResponse de(ContratoCredito c) {
            if (c == null) return null;
            return new CreditoResponse(c.getId(), c.getBanco().getNome(), c.getValor(), c.getTaxaJurosMensal(),
                    c.getNumeroParcelas(), c.getValorParcela());
        }
    }

    public record ContratoResponse(Long id, Long pedidoId, String cliente, String clienteCpf,
                                   AutomovelResponse automovel, LocalDate dataInicio, LocalDate dataFim,
                                   String agente, TipoAgente tipoAgente, TipoPropriedade tipoPropriedade,
                                   LocalDateTime dataAssinatura, CreditoResponse credito) {
        public static ContratoResponse de(Contrato c) {
            var p = c.getPedido();
            return new ContratoResponse(c.getId(), p.getId(), p.getCliente().getNome(), p.getCliente().getCpf(),
                    AutomovelResponse.de(p.getAutomovel()), p.getDataInicio(), p.getDataFim(),
                    c.getAgente().getNome(), c.getAgente().getTipo(), c.getTipoPropriedade(),
                    c.getDataAssinatura(), CreditoResponse.de(c.getContratoCredito()));
        }
    }
}
