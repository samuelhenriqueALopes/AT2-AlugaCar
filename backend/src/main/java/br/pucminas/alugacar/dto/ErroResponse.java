package br.pucminas.alugacar.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Corpo padrao das respostas de erro da API. */
public record ErroResponse(LocalDateTime timestamp, int status, String mensagem, Map<String, String> campos) {
    public static ErroResponse de(int status, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, Map.of());
    }
}
