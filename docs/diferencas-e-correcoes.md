# Diferenças em relação ao projeto de referência

Base: `suporteos2026`, commit `5625f85` (Aula 17).

## Adaptação do tema

| Referência | NotoriusFight |
|---|---|
| `GrupoProduto` / `/api/grupos-produtos` | `Categoria` / `/api/categorias` |
| `Produto` / `/api/produtos` | `Artigo` / `/api/artigos` |
| código de barras | SKU |
| (não existia) | marca, modalidade e tamanho, com filtros por marca e modalidade |

## Defeitos corrigidos

| Defeito | Correção | Teste |
|---|---|---|
| Duas finalizações simultâneas da mesma venda baixavam o estoque duas vezes | A venda é bloqueada antes de qualquer alteração | `VendaServiceTest` |
| Editar um artigo durante uma retirada restaurava o saldo antigo | Bloqueio e releitura em toda alteração, mais `@DynamicUpdate` | `VendaServiceTest` |
| Operador podia inativar a pessoa de um administrador | Exige administrador e mantém ao menos um ativo | `RegressaoHttpTest` |
| Trocar a senha não encerrava as sessões | Sessões revogadas na troca de senha e no bloqueio da conta | `RegressaoHttpTest` |
| Erros 404, 405 e 415 chegavam como 401 ou 403 | Despacho de erro liberado e respostas padronizadas | `RegressaoHttpTest` |

Os sete testes novos foram executados também sem as correções e falharam, como esperado.

## Outros ajustes

- CNPJ alfanumérico aceito.
- Quantidades fracionadas recusadas.
- Operador pode remover item de uma venda aberta.
- Auditoria com a mesma paginação do restante da API.
- Índices nas chaves estrangeiras de artigos e vendas.
- Spring Boot 4.0.7 → 4.0.10.
- Coleção Postman: token de administrador nas operações que o exigem.

## Limitações conhecidas

- Muitas tentativas de login em paralelo podem passar do limite de cinco.
- O token de acesso já emitido vale até expirar (30 minutos), mesmo após logout ou bloqueio.
- Sessões e eventos antigos não são apagados automaticamente.
- A lista de fornecedores não é paginada.
- Docker, observabilidade, integração contínua e implantação (Aulas 18 a 21) ainda não foram feitos.
