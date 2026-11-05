package br.pucminas.alugacar.model;

import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.enums.StatusPedido;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Pedido de aluguel introduzido por um cliente e avaliado financeiramente por um agente. */
@Entity
@Table(name = "pedidos_aluguel")
public class PedidoAluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id")
    private Automovel automovel;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPedido status;

    @Column(length = 500)
    private String observacoes;

    /** Indica se o cliente deseja que o aluguel seja associado a um contrato de credito. */
    @Column(name = "solicita_credito", nullable = false)
    private boolean solicitaCredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_avaliador_id")
    private Agente agenteAvaliador;

    @Column(length = 500)
    private String parecer;

    @Column(name = "data_avaliacao")
    private LocalDateTime dataAvaliacao;

    protected PedidoAluguel() {
    }

    public PedidoAluguel(Cliente cliente, Automovel automovel, LocalDate dataInicio, LocalDate dataFim,
                         String observacoes, boolean solicitaCredito) {
        validarPeriodo(dataInicio, dataFim);
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.observacoes = observacoes;
        this.solicitaCredito = solicitaCredito;
        this.status = StatusPedido.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("Datas de inicio e fim sao obrigatorias");
        }
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data de fim deve ser igual ou posterior a data de inicio");
        }
    }

    /** O cliente so pode modificar pedidos ainda nao avaliados. */
    public void modificarPeloCliente(Automovel automovel, LocalDate inicio, LocalDate fim,
                                     String observacoes, boolean solicitaCredito) {
        if (status != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Somente pedidos PENDENTES podem ser modificados pelo cliente");
        }
        aplicarAlteracoes(automovel, inicio, fim, observacoes, solicitaCredito);
    }

    /** O agente pode modificar pedidos pendentes ou aprovados (antes da execucao do contrato). */
    public void modificarPeloAgente(Automovel automovel, LocalDate inicio, LocalDate fim,
                                    String observacoes, boolean solicitaCredito) {
        if (status != StatusPedido.PENDENTE && status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Somente pedidos PENDENTES ou APROVADOS podem ser modificados");
        }
        aplicarAlteracoes(automovel, inicio, fim, observacoes, solicitaCredito);
    }

    private void aplicarAlteracoes(Automovel automovel, LocalDate inicio, LocalDate fim,
                                   String observacoes, boolean solicitaCredito) {
        validarPeriodo(inicio, fim);
        this.automovel = automovel;
        this.dataInicio = inicio;
        this.dataFim = fim;
        this.observacoes = observacoes;
        this.solicitaCredito = solicitaCredito;
    }

    public void cancelar() {
        if (status != StatusPedido.PENDENTE && status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Pedido com status " + status + " nao pode ser cancelado");
        }
        this.status = StatusPedido.CANCELADO;
    }

    /** Analise financeira do pedido pelo agente. */
    public void avaliar(Agente agente, boolean parecerPositivo, String parecer) {
        if (status != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Somente pedidos PENDENTES podem ser avaliados");
        }
        this.agenteAvaliador = agente;
        this.parecer = parecer;
        this.dataAvaliacao = LocalDateTime.now();
        this.status = parecerPositivo ? StatusPedido.APROVADO : StatusPedido.REPROVADO;
    }

    void marcarContratado() {
        if (status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Somente pedidos APROVADOS podem ter contrato executado");
        }
        this.status = StatusPedido.CONTRATADO;
    }

    public long getQuantidadeDias() {
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public boolean pertenceA(Long clienteId) {
        return cliente.getId().equals(clienteId);
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Automovel getAutomovel() { return automovel; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusPedido getStatus() { return status; }
    public String getObservacoes() { return observacoes; }
    public boolean isSolicitaCredito() { return solicitaCredito; }
    public Agente getAgenteAvaliador() { return agenteAvaliador; }
    public String getParecer() { return parecer; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
}
