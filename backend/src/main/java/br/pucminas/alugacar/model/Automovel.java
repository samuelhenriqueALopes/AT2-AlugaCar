package br.pucminas.alugacar.model;

import jakarta.persistence.*;

/** Automovel disponivel para aluguel. */
@Entity
@Table(name = "automoveis")
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String matricula;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    /** Imagem do automovel: caminho publico (ex.: /carros/fiat-pulse.webp) ou data URL enviada pelo agente. */
    @Column(columnDefinition = "TEXT")
    private String imagem;

    /** Proprietario registrado (cliente, empresa ou banco), definido na execucao do contrato. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    protected Automovel() {
    }

    public Automovel(String matricula, Integer ano, String marca, String modelo, String placa, String imagem) {
        this.matricula = matricula;
        this.ano = ano;
        this.marca = marca;
        this.modelo = modelo;
        this.placa = placa;
        this.imagem = imagem;
    }

    public void registrarProprietario(Usuario proprietario) {
        this.proprietario = proprietario;
    }

    public String getDescricao() {
        return marca + " " + modelo + " (" + ano + ") - " + placa;
    }

    public Long getId() { return id; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }
    public Usuario getProprietario() { return proprietario; }
}
