package br.pucminas.alugacar.model;

import br.pucminas.alugacar.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Contrato de credito concedido por um banco agente e associado ao aluguel. */
@Entity
@Table(name = "contratos_credito")
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id")
    private Agente banco;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Taxa de juros mensal em percentual (ex.: 1.5 = 1,5% a.m.). */
    @Column(name = "taxa_juros_mensal", nullable = false, precision = 6, scale = 3)
    private BigDecimal taxaJurosMensal;

    @Column(name = "numero_parcelas", nullable = false)
    private Integer numeroParcelas;

    protected ContratoCredito() {
    }

    public ContratoCredito(Agente banco, BigDecimal valor, BigDecimal taxaJurosMensal, Integer numeroParcelas) {
        if (!banco.isBanco()) {
            throw new RegraNegocioException("Contrato de credito deve ser concedido por um banco");
        }
        this.banco = banco;
        this.valor = valor;
        this.taxaJurosMensal = taxaJurosMensal;
        this.numeroParcelas = numeroParcelas;
    }

    /** Valor da parcela pela Tabela Price: PMT = PV * i / (1 - (1 + i)^-n). */
    public BigDecimal getValorParcela() {
        MathContext mc = MathContext.DECIMAL64;
        if (taxaJurosMensal.signum() == 0) {
            return valor.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);
        }
        BigDecimal i = taxaJurosMensal.divide(BigDecimal.valueOf(100), mc);
        BigDecimal fator = BigDecimal.ONE.add(i).pow(numeroParcelas, mc);
        BigDecimal pmt = valor.multiply(i.multiply(fator, mc), mc).divide(fator.subtract(BigDecimal.ONE), mc);
        return pmt.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Agente getBanco() { return banco; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getTaxaJurosMensal() { return taxaJurosMensal; }
    public Integer getNumeroParcelas() { return numeroParcelas; }
}
