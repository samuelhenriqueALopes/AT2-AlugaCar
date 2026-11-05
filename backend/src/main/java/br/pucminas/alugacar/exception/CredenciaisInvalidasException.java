package br.pucminas.alugacar.exception;

/** Login ou senha incorretos (HTTP 401). */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuario ou senha invalidos");
    }
}
