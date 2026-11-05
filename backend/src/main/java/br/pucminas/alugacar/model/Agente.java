package br.pucminas.alugacar.model;

import br.pucminas.alugacar.model.enums.Papel;
import br.pucminas.alugacar.model.enums.TipoAgente;
import jakarta.persistence.*;

import java.util.List;

/** Agente aderente (empresa ou banco): modifica e avalia pedidos, executa contratos. */
@Entity
@Table(name = "agentes")
public class Agente extends Usuario {

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAgente tipo;

    protected Agente() {
    }

    public Agente(String nome, String login, String senha, String email, String cnpj, TipoAgente tipo) {
        super(nome, login, senha, email);
        this.cnpj = cnpj;
        this.tipo = tipo;
    }

    @Override
    public Papel getPapel() {
        return Papel.AGENTE;
    }

    @Override
    public List<String> getRoles() {
        return List.of(Papel.AGENTE.name(), tipo.name());
    }

    public boolean isBanco() {
        return tipo == TipoAgente.BANCO;
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public TipoAgente getTipo() { return tipo; }
    public void setTipo(TipoAgente tipo) { this.tipo = tipo; }
}
