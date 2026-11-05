package br.pucminas.alugacar.config;

import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.Automovel;
import br.pucminas.alugacar.model.Cliente;
import br.pucminas.alugacar.model.enums.TipoAgente;
import br.pucminas.alugacar.repository.AgenteRepository;
import br.pucminas.alugacar.repository.AutomovelRepository;
import br.pucminas.alugacar.repository.ClienteRepository;
import br.pucminas.alugacar.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Popula dados de demonstracao quando o banco esta vazio (desative com APP_SEED=false). */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String SENHA_DEMO = "123456";

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AgenteRepository agenteRepository;
    private final AutomovelRepository automovelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                           AgenteRepository agenteRepository, AutomovelRepository automovelRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.agenteRepository = agenteRepository;
        this.automovelRepository = automovelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Ilustracoes dos automoveis de demonstracao (servidas pelo front-end em /carros). */
    private static final Map<String, String> IMAGENS = Map.of(
            "ABC1D23", "/carros/fiat-pulse.webp",
            "DEF4G56", "/carros/volkswagen-t-cross.webp",
            "GHI7J89", "/carros/chevrolet-onix.webp",
            "JKL0M12", "/carros/toyota-corolla.webp");

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            // Bancos ja populados por versoes anteriores: completa ou atualiza as imagens de demonstracao
            // (nunca sobrescreve uma imagem enviada por um agente).
            automovelRepository.findAll().stream()
                    .filter(a -> IMAGENS.containsKey(a.getPlaca()))
                    .filter(a -> a.getImagem() == null
                            || (a.getImagem().startsWith("/carros/") && !a.getImagem().equals(IMAGENS.get(a.getPlaca()))))
                    .forEach(a -> a.setImagem(IMAGENS.get(a.getPlaca())));
            return;
        }
        String senha = passwordEncoder.encode(SENHA_DEMO);

        agenteRepository.save(new Agente("AlugaCar Locadora", "locadora", senha, "contato@alugacar.com",
                "12345678000190", TipoAgente.EMPRESA));
        agenteRepository.save(new Agente("Banco Credicar", "banco", senha, "credito@credicar.com",
                "98765432000110", TipoAgente.BANCO));

        Cliente cliente = new Cliente("Maria Silva", "cliente", senha, "maria@email.com",
                "MG1234567", "12345678909", "Rua das Flores, 100 - Belo Horizonte/MG", "Engenheira de Software");
        cliente.adicionarRendimento("PUC Minas", new BigDecimal("8500.00"));
        cliente.adicionarRendimento("Freelancer", new BigDecimal("2000.00"));
        clienteRepository.save(cliente);

        automovelRepository.saveAll(List.of(
                new Automovel("MAT-0001", 2024, "Fiat", "Pulse", "ABC1D23", IMAGENS.get("ABC1D23")),
                new Automovel("MAT-0002", 2023, "Volkswagen", "T-Cross", "DEF4G56", IMAGENS.get("DEF4G56")),
                new Automovel("MAT-0003", 2025, "Chevrolet", "Onix", "GHI7J89", IMAGENS.get("GHI7J89")),
                new Automovel("MAT-0004", 2022, "Toyota", "Corolla", "JKL0M12", IMAGENS.get("JKL0M12"))));

        log.info("Dados de demonstracao criados. Logins: cliente / locadora / banco (senha {})", SENHA_DEMO);
    }
}
