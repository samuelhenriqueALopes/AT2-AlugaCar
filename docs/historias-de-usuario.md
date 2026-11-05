# Histórias de Usuário — AlugaCar

Formato: *Como [papel], quero [ação] para [benefício].* Cada história traz critérios de aceitação verificáveis e o caso de uso relacionado (ver [diagrama de casos de uso](diagramas/v2/casos-de-uso.png)).

---

## Acesso

### US01 — Cadastro prévio (UC01)
**Como** visitante, **quero** me cadastrar como cliente ou como agente (empresa ou banco) **para** poder usar o sistema.

- [x] O cliente informa usuário, senha, nome, RG, CPF, endereço, profissão e até **3** rendimentos (entidade empregadora + valor).
- [x] O agente informa usuário, senha, razão social, CNPJ e tipo (EMPRESA ou BANCO).
- [x] Usuário, CPF, RG e CNPJ são únicos. A senha tem no mínimo 6 caracteres.

### US02 — Login (UC02)
**Como** usuário cadastrado, **quero** entrar com usuário e senha **para** acessar as funcionalidades do meu perfil.

- [x] Credenciais inválidas retornam erro (HTTP 401).
- [x] Qualquer rota da API, fora login e cadastro, exige token JWT válido.
- [x] O menu mostra apenas as opções permitidas para o papel logado.

## Cliente

### US03 — Gerenciar meu cadastro (UC03, CRUD de cliente)
**Como** cliente, **quero** consultar, alterar e excluir meus dados **para** mantê-los atualizados.

- [x] Posso alterar dados de identificação, profissão, rendimentos (máx. 3) e a senha (opcional).
- [x] Não consigo acessar nem alterar o cadastro de outro cliente (HTTP 403).
- [x] A exclusão é bloqueada se eu já tiver contratos executados.

### US04 — Introduzir pedido (UC04)
**Como** cliente, **quero** criar um pedido de aluguel escolhendo automóvel e período **para** alugar um carro.

- [x] O pedido nasce com status **PENDENTE**.
- [x] A data de início não pode estar no passado, e a de fim não pode ser anterior à de início.
- [x] Posso indicar que desejo um contrato de crédito.
- [x] O pedido é rejeitado se o automóvel já estiver contratado no período.

### US05 — Modificar pedido (UC05)
**Como** cliente, **quero** alterar automóvel, datas e observações de um pedido **para** corrigir ou ajustar minha solicitação.

- [x] Só posso modificar pedidos **PENDENTES**.

### US06 — Consultar pedidos e status (UC06)
**Como** cliente, **quero** ver meus pedidos com status, parecer e agente avaliador **para** acompanhar a análise.

- [x] Vejo apenas os meus pedidos e posso filtrá-los por status.

### US07 — Cancelar pedido (UC07)
**Como** cliente, **quero** cancelar um pedido **para** desistir do aluguel.

- [x] Só posso cancelar pedidos **PENDENTES** ou **APROVADOS**, nunca os já contratados.

## Agente (empresa ou banco)

### US08 — Avaliar pedido (UC08)
**Como** agente, **quero** analisar financeiramente um pedido, com base na renda e na profissão do cliente, e registrar um parecer **para** aprová-lo ou reprová-lo.

- [x] Só pedidos **PENDENTES** podem ser avaliados.
- [x] Parecer positivo leva o pedido a **APROVADO**; negativo, a **REPROVADO**.
- [x] Ficam registrados o agente, o parecer e a data da avaliação.

### US09 — Modificar pedido (UC05)
**Como** agente, **quero** ajustar pedidos **PENDENTES** ou **APROVADOS** **para** adequá-los antes do contrato.

### US10 — Executar contrato (UC09)
**Como** agente, **quero** executar o contrato de um pedido aprovado **para** formalizar o aluguel.

- [x] Só pedidos **APROVADOS** podem ser contratados. O pedido passa a **CONTRATADO**.
- [x] Escolho como o automóvel é registrado: propriedade do **cliente**, da **empresa** (somente agente empresa) ou do **banco** (somente agente banco).

### US11 — Conceder crédito (UC10)
**Como** banco agente, **quero** associar um contrato de crédito (valor, juros mensais, parcelas) ao aluguel **para** financiar o cliente.

- [x] Somente agentes do tipo BANCO podem conceder crédito.
- [x] O sistema calcula o valor da parcela pela Tabela Price.

### US12 — Gerenciar automóveis (UC11)
**Como** agente, **quero** cadastrar, editar e excluir automóveis (matrícula, ano, marca, modelo, placa) **para** disponibilizá-los para aluguel.

- [x] Placa e matrícula são únicas.
- [x] Um automóvel com pedidos não pode ser excluído.

### US13 — Consultar clientes (UC12)
**Como** agente, **quero** consultar os clientes e seus rendimentos **para** embasar a análise financeira.

### US14 — Consultar contratos (UC13)
**Como** usuário, **quero** ver os contratos executados (e o crédito associado) **para** acompanhar os aluguéis formalizados.

- [x] O cliente vê apenas os próprios contratos. O agente vê todos.
