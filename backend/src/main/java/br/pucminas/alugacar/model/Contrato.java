package br.pucminas.alugacar.model;

import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.enums.TipoAgente;
import br.pucminas.alugacar.model.enums.TipoPropriedade;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Contrato de aluguel executado por um agente a partir de um pedido aprovado. */
@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", unique = true)
    private PedidoAluguel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_propriedade", nullable = false, length = 10)
    private TipoPropriedade tipoPropriedade;

    @Column(name = "data_assinatura", nullable = false)
    private LocalDateTime dataAssinatura;

    /** Contrato de credito opcional, concedido por um banco agente. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_credito_id")
    private ContratoCredito contratoCredito;

    protected Contrato() {
    }

    /**
     * Executa o contrato: marca o pedido como CONTRATADO e registra o proprietario do automovel
     * de acordo com o tipo de contrato.
     */
    public static Contrato executar(PedidoAluguel pedido, Agente agente, TipoPropriedade tipoPropriedade,
                                    ContratoCredito credito) {
        if (credito != null && !agente.isBanco()) {
            throw new RegraNegocioException("Somente bancos agentes podem conceder contrato de credito");
        }
        Usuario proprietario = switch (tipoPropriedade) {
            case CLIENTE -> pedido.getCliente();
            case EMPRESA -> exigirTipo(agente, TipoAgente.EMPRESA);
            case BANCO -> exigirTipo(agente, TipoAgente.BANCO);
        };
        pedido.marcarContratado();
        pedido.getAutomovel().registrarProprietario(proprietario);

        Contrato contrato = new Contrato();
        contrato.pedido = pedido;
        contrato.agente = agente;
        contrato.tipoPropriedade = tipoPropriedade;
        contrato.contratoCredito = credito;
        contrato.dataAssinatura = LocalDateTime.now();
        return contrato;
    }

    private static Agente exigirTipo(Agente agente, TipoAgente tipo) {
        if (agente.getTipo() != tipo) {
            throw new RegraNegocioException("Somente um agente do tipo " + tipo
                    + " pode registrar o automovel como propriedade " + tipo);
        }
        return agente;
    }

    public Long getId() { return id; }
    public PedidoAluguel getPedido() { return pedido; }
    public Agente getAgente() { return agente; }
    public TipoPropriedade getTipoPropriedade() { return tipoPropriedade; }
    public LocalDateTime getDataAssinatura() { return dataAssinatura; }
    public ContratoCredito getContratoCredito() { return contratoCredito; }
}
