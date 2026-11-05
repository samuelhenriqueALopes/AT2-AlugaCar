package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.AgenteDTOs.AgenteRequest;
import br.pucminas.alugacar.dto.AgenteDTOs.AgenteResponse;
import br.pucminas.alugacar.model.enums.TipoAgente;
import br.pucminas.alugacar.service.AgenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cadastro (publico) e consulta de agentes. */
@RestController
@RequestMapping("/api/agentes")
public class AgenteController {

    private final AgenteService agenteService;

    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgenteResponse criar(@Valid @RequestBody AgenteRequest req) {
        return agenteService.criar(req);
    }

    @GetMapping
    public List<AgenteResponse> listar(@RequestParam(required = false) TipoAgente tipo) {
        return agenteService.listar(tipo);
    }
}
