package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelRequest;
import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelResponse;
import br.pucminas.alugacar.service.AutomovelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelService automovelService;

    public AutomovelController(AutomovelService automovelService) {
        this.automovelService = automovelService;
    }

    @GetMapping
    public List<AutomovelResponse> listar() {
        return automovelService.listar();
    }

    @GetMapping("/{id}")
    public AutomovelResponse buscar(@PathVariable Long id) {
        return automovelService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse criar(@Valid @RequestBody AutomovelRequest req) {
        return automovelService.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENTE')")
    public AutomovelResponse atualizar(@PathVariable Long id, @Valid @RequestBody AutomovelRequest req) {
        return automovelService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        automovelService.excluir(id);
    }
}
