package br.pucminas.alugacar.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Rendimento auferido pelo cliente em uma entidade empregadora. */
@Entity
@Table(name = "rendimentos")
public class Rendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidade_empregadora", nullable = false, length = 120)
    private String entidadeEmpregadora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    protected Rendimento() {
    }

    Rendimento(Cliente cliente, String entidadeEmpregadora, BigDecimal valor) {
        this.cliente = cliente;
        this.entidadeEmpregadora = entidadeEmpregadora;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public String getEntidadeEmpregadora() { return entidadeEmpregadora; }
    public BigDecimal getValor() { return valor; }
    public Cliente getCliente() { return cliente; }
}
