package br.pucminas.alugacar.exception;

/** Violacao de uma regra de negocio (HTTP 400). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
