package br.pucminas.alugacar;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de integracao do fluxo principal: login, pedido, avaliacao e contrato. */
@SpringBootTest
@ActiveProfiles("h2")
class AlugacarApplicationTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String login(String usuario) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + usuario + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    @Test
    void acessoSemLoginEhNegado() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"cliente\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crudDeCliente() throws Exception {
        String novo = """
                {"nome":"Joao Teste","login":"joao","senha":"segredo1","email":"joao@x.com",
                 "rg":"MG999","cpf":"529.982.247-25","endereco":"Rua A, 1","profissao":"Analista",
                 "rendimentos":[{"entidadeEmpregadora":"Empresa X","valor":5000}]}""";
        String body = mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");
        String b2 = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"joao\",\"senha\":\"segredo1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + JsonPath.read(b2, "$.token");

        mvc.perform(put("/api/clientes/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(novo.replace("Analista", "Gerente")
                                .replace("[{", "[{\"entidadeEmpregadora\":\"A\",\"valor\":1},{")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissao").value("Gerente"))
                .andExpect(jsonPath("$.rendimentos.length()").value(2));

        // mais de 3 rendimentos e rejeitado
        String quatro = novo.replace("[{\"entidadeEmpregadora\":\"Empresa X\",\"valor\":5000}]",
                "[{\"entidadeEmpregadora\":\"A\",\"valor\":1},{\"entidadeEmpregadora\":\"B\",\"valor\":1},"
                        + "{\"entidadeEmpregadora\":\"C\",\"valor\":1},{\"entidadeEmpregadora\":\"D\",\"valor\":1}]");
        mvc.perform(put("/api/clientes/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(quatro))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/clientes/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void fluxoCompletoDoPedidoAteContratoComCredito() throws Exception {
        String cliente = login("cliente");
        String banco = login("banco");
        LocalDate inicio = LocalDate.now().plusDays(30);

        String pedido = mvc.perform(post("/api/pedidos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"automovelId\":1,\"dataInicio\":\"" + inicio + "\",\"dataFim\":\""
                                + inicio.plusDays(4) + "\",\"solicitaCredito\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.quantidadeDias").value(5))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(pedido, "$.id");

        // cliente nao pode avaliar
        mvc.perform(patch("/api/pedidos/" + id + "/avaliar").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovado\":true,\"parecer\":\"ok\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/pedidos/" + id + "/avaliar").header("Authorization", banco)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\":true,\"parecer\":\"Renda compativel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mvc.perform(post("/api/pedidos/" + id + "/contrato").header("Authorization", banco)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPropriedade\":\"BANCO\",\"credito\":{\"valor\":10000,"
                                + "\"taxaJurosMensal\":1.5,\"numeroParcelas\":12}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.credito.valorParcela").value(916.80))
                .andExpect(jsonPath("$.automovel.proprietarioNome").value("Banco Credicar"));

        mvc.perform(get("/api/pedidos/" + id).header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTRATADO"));

        // pedido contratado nao pode ser cancelado
        mvc.perform(patch("/api/pedidos/" + id + "/cancelar").header("Authorization", cliente))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/contratos").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void automoveisPossuemImagemEValidamFormato() throws Exception {
        String banco = login("banco");
        mvc.perform(get("/api/automoveis/1").header("Authorization", banco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagem").value("/carros/fiat-pulse.webp"));

        String base = "{\"matricula\":\"MAT-IMG\",\"ano\":2024,\"marca\":\"Honda\",\"modelo\":\"City\",\"placa\":\"IMG1A23\",";
        mvc.perform(post("/api/automoveis").header("Authorization", banco).contentType(MediaType.APPLICATION_JSON)
                        .content(base + "\"imagem\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/automoveis").header("Authorization", banco).contentType(MediaType.APPLICATION_JSON)
                        .content(base + "\"imagem\":\"data:image/png;base64,iVBORw0KGgo=\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagem").value("data:image/png;base64,iVBORw0KGgo="));
    }

    @Test
    void clientePodeModificarECancelarPedidoPendente() throws Exception {
        String cliente = login("cliente");
        LocalDate inicio = LocalDate.now().plusDays(60);
        String pedido = mvc.perform(post("/api/pedidos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"automovelId\":2,\"dataInicio\":\"" + inicio + "\",\"dataFim\":\""
                                + inicio.plusDays(1) + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(pedido, "$.id");

        mvc.perform(put("/api/pedidos/" + id).header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"automovelId\":3,\"dataInicio\":\"" + inicio + "\",\"dataFim\":\""
                                + inicio.plusDays(2) + "\",\"observacoes\":\"Troca de carro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automovel.id").value(3));

        mvc.perform(patch("/api/pedidos/" + id + "/cancelar").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}
