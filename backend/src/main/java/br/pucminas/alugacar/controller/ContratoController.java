package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.ContratoDTOs.ContratoResponse;
import br.pucminas.alugacar.security.UsuarioLogado;
import br.pucminas.alugacar.service.ContratoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    public List<ContratoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return contratoService.listar(UsuarioLogado.de(jwt));
    }

    @GetMapping("/{id}")
    public ContratoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return contratoService.buscar(id, UsuarioLogado.de(jwt));
    }
}
