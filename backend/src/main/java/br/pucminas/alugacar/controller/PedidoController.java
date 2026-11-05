package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.ContratoDTOs.ContratoRequest;
import br.pucminas.alugacar.dto.ContratoDTOs.ContratoResponse;
import br.pucminas.alugacar.dto.PedidoDTOs.AvaliacaoRequest;
import br.pucminas.alugacar.dto.PedidoDTOs.PedidoRequest;
import br.pucminas.alugacar.dto.PedidoDTOs.PedidoResponse;
import br.pucminas.alugacar.security.UsuarioLogado;
import br.pucminas.alugacar.service.ContratoService;
import br.pucminas.alugacar.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ContratoService contratoService;

    public PedidoController(PedidoService pedidoService, ContratoService contratoService) {
        this.pedidoService = pedidoService;
        this.contratoService = contratoService;
    }

    /** Cliente introduz um pedido. */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest req, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.criar(req, UsuarioLogado.de(jwt));
    }

    /** Cliente ve os proprios pedidos; agente ve todos. */
    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listar(UsuarioLogado.de(jwt));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.buscar(id, UsuarioLogado.de(jwt));
    }

    /** Cliente (pedido PENDENTE) ou agente (PENDENTE/APROVADO) modificam o pedido. */
    @PutMapping("/{id}")
    public PedidoResponse modificar(@PathVariable Long id, @Valid @RequestBody PedidoRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.modificar(id, req, UsuarioLogado.de(jwt));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.cancelar(id, UsuarioLogado.de(jwt));
    }

    /** Agente emite o parecer financeiro. */
    @PatchMapping("/{id}/avaliar")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse avaliar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest req,
                                  @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.avaliar(id, req, UsuarioLogado.de(jwt));
    }

    /** Agente executa o contrato de um pedido aprovado. */
    @PostMapping("/{id}/contrato")
    @PreAuthorize("hasRole('AGENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse executarContrato(@PathVariable Long id, @Valid @RequestBody ContratoRequest req,
                                             @AuthenticationPrincipal Jwt jwt) {
        return contratoService.executar(id, req, UsuarioLogado.de(jwt));
    }
}
