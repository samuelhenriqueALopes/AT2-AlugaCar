package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.AuthDTOs.LoginRequest;
import br.pucminas.alugacar.dto.AuthDTOs.LoginResponse;
import br.pucminas.alugacar.dto.AuthDTOs.UsuarioResponse;
import br.pucminas.alugacar.exception.CredenciaisInvalidasException;
import br.pucminas.alugacar.exception.RecursoNaoEncontradoException;
import br.pucminas.alugacar.model.Usuario;
import br.pucminas.alugacar.repository.UsuarioRepository;
import br.pucminas.alugacar.security.TokenService;
import br.pucminas.alugacar.security.UsuarioLogado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByLogin(req.login().trim())
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return new LoginResponse(tokenService.gerarToken(usuario), UsuarioResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuarioAtual(UsuarioLogado logado) {
        return usuarioRepository.findById(logado.id())
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", logado.id()));
    }
}
