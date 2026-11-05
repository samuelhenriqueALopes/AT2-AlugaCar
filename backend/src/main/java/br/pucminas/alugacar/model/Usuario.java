package br.pucminas.alugacar.model;

import br.pucminas.alugacar.model.enums.Papel;
import jakarta.persistence.*;

import java.util.List;

/** Usuario do sistema (cadastro previo + login obrigatorio). Especializado em Cliente e Agente. */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    /** Hash BCrypt da senha. */
    @Column(nullable = false)
    private String senha;

    @Column(length = 120)
    private String email;

    protected Usuario() {
    }

    protected Usuario(String nome, String login, String senha, String email) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.email = email;
    }

    public abstract Papel getPapel();

    /** Roles usadas na autorizacao (claim "roles" do JWT). */
    public List<String> getRoles() {
        return List.of(getPapel().name());
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
