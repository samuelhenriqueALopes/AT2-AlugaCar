package br.pucminas.alugacar.exception;

import br.pucminas.alugacar.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Traduz excecoes em respostas JSON padronizadas. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ErroResponse> credenciais(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    ResponseEntity<ErroResponse> acessoNegado(RuntimeException e) {
        String msg = e instanceof AcessoNegadoException ? e.getMessage() : "Acesso negado para o seu perfil";
        return resposta(HttpStatus.FORBIDDEN, msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        String msg = campos.isEmpty() ? "Dados invalidos" : campos.values().iterator().next();
        return ResponseEntity.badRequest()
                .body(new ErroResponse(LocalDateTime.now(), 400, msg, campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException e) {
        log.warn("Violacao de integridade", e);
        return resposta(HttpStatus.CONFLICT, "Operacao viola restricoes de integridade dos dados");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(status.value(), mensagem));
    }
}
