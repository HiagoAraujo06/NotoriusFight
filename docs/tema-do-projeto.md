# Tema do projeto

- **Nome:** NotoriusFight
- **Tema:** loja de artigos de luta
- **Objetivo:** controlar catálogo, estoque, fornecedores, clientes, equipe e vendas

## Entidade de classificação: Categoria

Organiza os artigos da loja. Exemplos: Luvas, Kimonos, Proteções, Vestuário e Equipamentos de treino.

## Entidade principal: Artigo

| Exigência do tema | Atributo |
|---|---|
| Código único | SKU |
| Descrição | nome comercial |
| Medida quantitativa | saldo em estoque, em unidades inteiras |
| Valor monetário | preço unitário |
| Valor calculado | saldo × preço |
| Data | data de cadastro |
| Status | ativo ou inativo |
| Relacionamento | pertence a uma categoria; fornecedor opcional |
| Próprios do tema | marca, modalidade e tamanho |

## Regras da loja

- O SKU é único e o saldo nunca fica negativo.
- Estoque e vendas usam unidades inteiras.
- Artigos novos exigem categoria ativa e, se houver fornecedor, que ele esteja ativo.
- A venda guarda o preço do momento em que o item foi incluído.
- O estoque só baixa ao finalizar a venda, e uma venda é finalizada uma única vez.
- Um artigo já vendido não pode ser excluído, apenas inativado.

## Exemplos

| Categoria | SKU | Artigo | Modalidade | Tamanho | Preço |
|---|---|---|---|---|---:|
| Luvas | `NF-LUV-BX-12` | Luva de boxe em couro | Boxe | 12 OZ | 289,90 |
| Kimonos | `NF-KIM-JJ-A2` | Kimono de jiu-jitsu trançado | Jiu-jítsu | A2 | 449,90 |
| Proteções | `NF-CAN-MT-G` | Caneleira anatômica | Muay thai | G | 239,90 |
