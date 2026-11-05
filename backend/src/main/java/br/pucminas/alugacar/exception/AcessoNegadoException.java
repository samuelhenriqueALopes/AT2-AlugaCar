package br.pucminas.alugacar.exception;

/** Usuario autenticado tentando acessar recurso de outro usuario (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
