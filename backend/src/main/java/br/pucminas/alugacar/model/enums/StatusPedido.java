package br.pucminas.alugacar.model.enums;

/**
 * Ciclo de vida do pedido de aluguel:
 * PENDENTE -> (avaliacao financeira do agente) -> APROVADO | REPROVADO
 * APROVADO -> (execucao do contrato) -> CONTRATADO
 * PENDENTE | APROVADO -> (cliente) -> CANCELADO
 */
public enum StatusPedido {
    PENDENTE,
    APROVADO,
    REPROVADO,
    CANCELADO,
    CONTRATADO
}
