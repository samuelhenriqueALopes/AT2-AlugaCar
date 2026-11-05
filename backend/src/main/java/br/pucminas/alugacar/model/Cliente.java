package br.pucminas.alugacar.model;

import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.enums.Papel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Contratante do aluguel (usuario individual). */
@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    public static final int MAX_RENDIMENTOS = 3;

    @Column(nullable = false, unique = true, length = 20)
    private String rg;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private String endereco;

    @Column(length = 80)
    private String profissao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Rendimento> rendimentos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nome, String login, String senha, String email,
                   String rg, String cpf, String endereco, String profissao) {
        super(nome, login, senha, email);
        this.rg = rg;
        this.cpf = cpf;
        this.endereco = endereco;
        this.profissao = profissao;
    }

    @Override
    public Papel getPapel() {
        return Papel.CLIENTE;
    }

    /** Adiciona um rendimento auferido (entidade empregadora + valor). Maximo de 3. */
    public void adicionarRendimento(String entidadeEmpregadora, BigDecimal valor) {
        if (rendimentos.size() >= MAX_RENDIMENTOS) {
            throw new RegraNegocioException("Um cliente pode ter no maximo " + MAX_RENDIMENTOS + " rendimentos");
        }
        rendimentos.add(new Rendimento(this, entidadeEmpregadora, valor));
    }

    public void limparRendimentos() {
        rendimentos.clear();
    }

    public BigDecimal getRendaTotal() {
        return rendimentos.stream().map(Rendimento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Rendimento> getRendimentos() { return Collections.unmodifiableList(rendimentos); }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }
}
