# Modelos UML — AlugaCar

Os fontes estão em [PlantUML](https://plantuml.com) (`.puml`), e as imagens (`.png` / `.svg`) foram geradas a partir deles.

| Versão | Casos de Uso | Classes | Pacotes (Visão Lógica) |
|---|---|---|---|
| **v1** (inicial, a partir dos requisitos) | [png](v1/casos-de-uso-v1.png) | [png](v1/classes-v1.png) | [png](v1/pacotes-v1.png) |
| **v2** (final, alinhada ao código) | [png](v2/casos-de-uso.png) | [png](v2/classes.png) | [png](v2/pacotes.png) |

As histórias de usuário estão em [../historias-de-usuario.md](../historias-de-usuario.md).

## Como regenerar as imagens

```bash
java -jar plantuml.jar -tpng docs/diagramas/v2/*.puml
java -jar plantuml.jar -tsvg docs/diagramas/v2/*.puml
```

Os diagramas usam `!pragma layout smetana`, então não é preciso instalar o Graphviz.

## Modificações da v1 para a v2

Estas são as alterações feitas no modelo inicial para que o software funcionasse corretamente:

| # | v1 (inicial) | v2 (implementado) | Motivo |
|---|---|---|---|
| 1 | `Empresa` e `Banco` como subclasses de `Agente` | Uma classe `Agente` com o atributo `tipo : TipoAgente {EMPRESA, BANCO}` | As duas classes não tinham atributos nem comportamento próprios. O enum simplifica o mapeamento JPA e as regras de autorização (roles `AGENTE` + `EMPRESA`/`BANCO`). |
| 2 | `EntidadeEmpregadora` com o rendimento | `Rendimento` (entidadeEmpregadora, valor) em composição com `Cliente`, multiplicidade `0..3` | Torna explícito que cada rendimento pertence ao cliente e aplica o limite de 3 no próprio modelo (`Cliente.adicionarRendimento`). |
| 3 | `nome` em `Cliente` | `nome`, `login`, `senha` e `email` em `Usuario` | Agentes também têm nome. O login é obrigatório para todos os usuários. |
| 4 | `Pedido.status : String` | `PedidoAluguel.status : StatusPedido {PENDENTE, APROVADO, REPROVADO, CANCELADO, CONTRATADO}` | Máquina de estados explícita, com transições validadas nos métodos `modificarPeloCliente`, `modificarPeloAgente`, `cancelar`, `avaliar` e `marcarContratado`. |
| 5 | — | Atributos `dataInicio`, `dataFim`, `dataCriacao`, `observacoes`, `solicitaCredito`, `parecer`, `dataAvaliacao` e a associação `agenteAvaliador` | Necessários para a análise financeira e para mostrar o status ao cliente. |
| 6 | `Contrato.tipo : String` | `Contrato.tipoPropriedade : TipoPropriedade {CLIENTE, EMPRESA, BANCO}` e o método `executar(...)` | O tipo de contrato define o proprietário do automóvel (`Automovel.registrarProprietario`). |
| 7 | `Banco.concederCredito()` | `ContratoCredito` com `banco`, `valor`, `taxaJurosMensal`, `numeroParcelas` e `getValorParcela()` (Tabela Price), em composição com `Contrato` | O crédito é concedido por um banco agente no momento da execução do contrato. |
| 8 | Métodos de caso de uso em `Cliente` e `Agente` (`introduzirPedido`, `avaliarPedido`...) | Operações nas classes de serviço (`PedidoService`, `ContratoService`). As regras de domínio ficam nas entidades. | Separação MVC: os controllers recebem as requisições, os services orquestram e as entidades garantem as invariantes. |
| 9 | Pacotes `view` / `controller` / `model` / `dao` | Back-end: `controller`, `dto`, `service`, `repository`, `model`, `security`, `exception`, `config`. Front-end: `pages`, `components`, `context`, `services`. | Stack exigida: Spring Boot (MVC + Spring Data JPA) e React + Vite. Inclui autenticação JWT e DTOs para não expor as entidades. |
| 10 | Casos de uso apenas de pedidos | Novos casos: Realizar cadastro, Gerenciar cadastro (CRUD de cliente), Executar contrato, Gerenciar automóveis, Consultar clientes e Consultar contratos | Requisitos de cadastro prévio e CRUD de cliente, além do fluxo completo até o contrato. |
| 11 | `Automovel` sem imagem | Atributo `imagem : String` (caminho público, URL ou data URL de até 2 MB) | Exibir o carro na escolha do pedido, na análise, nos contratos e na frota. O agente pode enviar uma foto ao cadastrar o automóvel. |
