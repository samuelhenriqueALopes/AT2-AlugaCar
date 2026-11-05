package br.pucminas.alugacar.dto;

import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.Automovel;
import br.pucminas.alugacar.model.enums.TipoPropriedade;
import org.hibernate.Hibernate;
import br.pucminas.alugacar.model.Usuario;
import br.pucminas.alugacar.model.enums.Papel;
import jakarta.validation.constraints.*;

/** DTOs de automoveis. */
public final class AutomovelDTOs {

    private AutomovelDTOs() {
    }

    public record AutomovelRequest(
            @NotBlank(message = "Informe a matricula") @Size(max = 30) String matricula,
            @NotNull(message = "Informe o ano") @Min(value = 1950, message = "Ano invalido")
            @Max(value = 2100, message = "Ano invalido") Integer ano,
            @NotBlank(message = "Informe a marca") @Size(max = 60) String marca,
            @NotBlank(message = "Informe o modelo") @Size(max = 60) String modelo,
            @NotBlank(message = "Informe a placa")
            @Pattern(regexp = "[A-Za-z]{3}-?\\d[A-Za-z0-9]\\d{2}", message = "Placa invalida (ex.: ABC1D23)") String placa,
            /** Caminho publico ("/carros/x.webp"), URL http(s) ou data URL de imagem (ate ~2 MB). */
            @Size(max = 2_800_000, message = "Imagem muito grande (maximo 2 MB)")
            @Pattern(regexp = "(?s)(/|https?://|data:image/(png|jpeg|webp|gif|svg\\+xml);base64,).*",
                    message = "Imagem invalida") String imagem) {
    }

    public record AutomovelResponse(Long id, String matricula, Integer ano, String marca, String modelo,
                                    String placa, Long proprietarioId, String proprietarioNome,
                                    Papel proprietarioPapel, String proprietarioTipo, String imagem) {
        public static AutomovelResponse de(Automovel a) {
            Usuario p = (Usuario) Hibernate.unproxy(a.getProprietario());
            String tipo = p == null ? null : (p instanceof Agente ag ? ag.getTipo().name() : TipoPropriedade.CLIENTE.name());
            return new AutomovelResponse(a.getId(), a.getMatricula(), a.getAno(), a.getMarca(), a.getModelo(),
                    a.getPlaca(), p == null ? null : p.getId(), p == null ? null : p.getNome(),
                    p == null ? null : p.getPapel(), tipo, a.getImagem());
        }
    }
}
