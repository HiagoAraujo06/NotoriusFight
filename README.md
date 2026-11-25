# NotoriusFight

API REST de uma loja de artigos de luta: catálogo, estoque, clientes, equipe, vendas e segurança.

Projeto da disciplina, adaptado do projeto de referência
[suporteos2026](https://github.com/jeffersonarpasserini/suporteos2026) (Aulas 00 a 17).

**Tecnologias:** Java 21, Spring Boot 4.0, Spring Data JPA, PostgreSQL 17, Liquibase, Spring Security com JWT, OpenAPI e JaCoCo.

## Como usar

Os scripts ficam em `scripts` e podem ser abertos com dois cliques:

| Script | O que faz |
|---|---|
| `iniciar-api.cmd` | Liga o banco e a API em <http://localhost:8080> |
| `validar-api.cmd` | Testa o fluxo da loja na API ligada (cria dados de teste) |
| `testar.cmd` | Roda todos os testes e gera a cobertura |
| `iniciar-banco.cmd` / `parar-banco.cmd` | Liga ou desliga só o PostgreSQL |
| `ambiente.cmd` | Prepara Java, Git e PostgreSQL na janela atual |

Com a API ligada, a documentação interativa está em <http://localhost:8080/swagger-ui.html>.
Para entrar, use `INITIAL_ADMIN_LOGIN` e `INITIAL_ADMIN_PASSWORD` do arquivo `.env`.

> O arquivo `.env` contém senhas locais e nunca deve ir para o Git. Em outro computador, copie `.env.example` para `.env`,
> preencha os valores e, só na primeira execução, use `INITIAL_ADMIN_ENABLED=true`.

## Domínio

```text
Categoria    1 ── N Artigo        Fornecedor 1 ── N Artigo
Pessoa       1 ── 0..1 Cliente | Colaborador | ContaAcesso
Cliente      1 ── N Venda         Colaborador 1 ── N Venda
Venda        1 ── N ItemVenda N ── 1 Artigo
ContaAcesso  1 ── N SessaoRefreshToken
```

Cada artigo tem SKU, marca, modalidade (boxe, muay thai, jiu-jítsu, judô, karatê, taekwondo, MMA ou geral),
tamanho, preço, saldo e estoque mínimo. Veja [o tema](docs/tema-do-projeto.md).

## Rotas

| Rota | Conteúdo |
|---|---|
| `/api/categorias`, `/api/artigos`, `/api/fornecedores` | Catálogo e estoque |
| `/api/pessoas`, `/api/clientes`, `/api/colaboradores` | Cadastros |
| `/api/vendas` | Vendas e itens |
| `/api/autenticacao`, `/api/contas-acesso` | Login, sessões e contas |
| `/api/auditoria-seguranca` | Eventos de segurança (administrador) |

`OPERADOR` atende e vende. `ADMINISTRADOR` também exclui cadastros, altera o status de pessoas, gerencia contas e consulta a auditoria.

## Organização

```text
src/main/java/com/notoriusfight
├── api          controllers, DTOs, validações e erros
├── application  casos de uso e transações
├── domain       entidades e regras
├── repository   acesso a dados
├── security     carregamento da conta no login
└── config       segurança, JWT e OpenAPI
src/main/resources/db/changelog   evolução do banco (Liquibase)
postman                           coleção de requisições
docs                              tema, correções e situação
```

Veja também [as diferenças em relação ao projeto de referência](docs/diferencas-e-correcoes.md).

## Histórico por aula

Cada aula, da 00 à 17, tem um commit e uma tag `aula-NN-*`, organizados em 25/11/2026 a partir do projeto de referência.
O `main` traz a versão final, com os campos próprios da loja e as correções.

```bash
git tag --list "aula-*"
```
