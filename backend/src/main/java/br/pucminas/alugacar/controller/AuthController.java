package br.pucminas.alugacar.controller;

import br.pucminas.alugacar.dto.AuthDTOs.LoginRequest;
import br.pucminas.alugacar.dto.AuthDTOs.LoginResponse;
import br.pucminas.alugacar.dto.AuthDTOs.UsuarioResponse;
import br.pucminas.alugacar.security.UsuarioLogado;
import br.pucminas.alugacar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.usuarioAtual(UsuarioLogado.de(jwt));
    }
}
