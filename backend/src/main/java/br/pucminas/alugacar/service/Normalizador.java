package br.pucminas.alugacar.service;

import java.util.Locale;

/** Normaliza documentos e placas para garantir unicidade independente da formatacao digitada. */
final class Normalizador {

    private Normalizador() {
    }

    static String apenasDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    static String placa(String valor) {
        return valor == null ? null : valor.replace("-", "").trim().toUpperCase(Locale.ROOT);
    }

    static String texto(String valor) {
        if (valor == null) return null;
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }
}
