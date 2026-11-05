package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.ClienteDTOs.ClienteRequest;
import br.pucminas.alugacar.dto.ClienteDTOs.ClienteResponse;
import br.pucminas.alugacar.security.UsuarioLogado;
import br.pucminas.alugacar.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de cliente. POST e publico (cadastro previo); demais operacoes exigem login. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest req) {
        return clienteService.criar(req);
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENTE')")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clienteService.buscar(id, UsuarioLogado.de(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req,
                                     @AuthenticationPrincipal Jwt jwt) {
        return clienteService.atualizar(id, req, UsuarioLogado.de(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        clienteService.excluir(id, UsuarioLogado.de(jwt));
    }
}
