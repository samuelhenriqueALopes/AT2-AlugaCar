package br.pucminas.alugacar.exception;

/** Recurso inexistente (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " nao encontrado(a)");
    }
}
