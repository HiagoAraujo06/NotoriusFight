# Percorre o fluxo da loja contra a API local. Cria dados novos a cada execucao.
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$cfg = @{}
Get-Content (Join-Path $PSScriptRoot '..\.env') | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    $cfg[$k] = $v
}
$script:falhas = 0

function Invoke-Api($metodo, $rota, $token, $corpo) {
    $h = @{}
    if ($token) { $h.Authorization = "Bearer $token" }
    $p = @{ Uri = "$base$rota"; Method = $metodo; Headers = $h; UseBasicParsing = $true }
    if ($corpo) { $p.Body = ($corpo | ConvertTo-Json); $p.ContentType = 'application/json' }
    try {
        $r = Invoke-WebRequest @p
        $json = $null
        if ($r.Content -and "$($r.Headers['Content-Type'])" -match 'json') { $json = $r.Content | ConvertFrom-Json }
        return @{ Status = [int]$r.StatusCode; Json = $json }
    } catch [System.Net.WebException] {
        if (-not $_.Exception.Response) { throw 'API indisponivel. Execute scripts\iniciar-api.cmd primeiro.' }
        return @{ Status = [int]$_.Exception.Response.StatusCode; Json = $null }
    }
}

function Test-Passo($nome, $ok, $detalhe) {
    if ($ok) { Write-Host "[OK]    $nome" } else { $script:falhas++; Write-Host "[FALHA] $nome ($detalhe)" }
}

function New-Cpf {
    $n = @(1..9 | ForEach-Object { Get-Random -Maximum 10 })
    foreach ($peso in 10, 11) {
        $soma = 0
        for ($i = 0; $i -lt $n.Count; $i++) { $soma += $n[$i] * ($peso - $i) }
        $resto = $soma % 11
        if ($resto -lt 2) { $n += 0 } else { $n += 11 - $resto }
    }
    return -join $n
}

$u = [DateTime]::Now.ToString('yyyyMMddHHmmssfff')
$r = Invoke-Api GET '/api/health'; Test-Passo 'Health publico' ($r.Status -eq 200) $r.Status
$r = Invoke-Api GET '/api/artigos'; Test-Passo 'Rota protegida exige login' ($r.Status -eq 401) $r.Status
$r = Invoke-Api POST '/api/autenticacao/login' $null @{ login = $cfg.INITIAL_ADMIN_LOGIN; senha = $cfg.INITIAL_ADMIN_PASSWORD }
Test-Passo 'Login do administrador' ($r.Status -eq 200) $r.Status
if ($r.Status -ne 200) { exit 1 }
$adm = $r.Json.accessToken

$r = Invoke-Api GET '/api/artigos?modalidade=BOXE' $adm; Test-Passo 'Catalogo filtrado por modalidade' ($r.Status -eq 200 -and $r.Json.totalElementos -ge 2) $r.Status
$r = Invoke-Api GET '/api/artigos?abaixoEstoqueMinimo=true' $adm; Test-Passo 'Alerta de estoque minimo' ($r.Status -eq 200 -and $r.Json.totalElementos -ge 1) $r.Status
$r = Invoke-Api POST '/api/categorias' $adm @{ nome = "Validacao $u" }; Test-Passo 'Cadastrar categoria' ($r.Status -eq 201) $r.Status
$cat = $r.Json.id
$r = Invoke-Api POST '/api/artigos' $adm @{ sku = "VAL-$u"; descricao = 'Luva de validacao'; saldoEstoque = 10; valorUnitario = 100; estoqueMinimo = 2; marca = 'NotoriusFight'; modalidade = 'MMA'; tamanho = 'M'; categoriaId = $cat }
Test-Passo 'Cadastrar artigo' ($r.Status -eq 201) $r.Status
$art = $r.Json.id
$r = Invoke-Api PUT "/api/artigos/$art" $adm @{ descricao = 'Luva editada'; valorUnitario = 120; estoqueMinimo = 2; marca = 'NotoriusFight'; modalidade = 'BOXE'; tamanho = '14 oz'; categoriaId = $cat }
Test-Passo 'Editar artigo' ($r.Status -eq 200 -and $r.Json.tamanho -eq '14 OZ') $r.Status
$r = Invoke-Api POST "/api/artigos/$art/estoque/entradas" $adm @{ quantidade = 5 }; Test-Passo 'Entrada de estoque' ($r.Status -eq 200 -and $r.Json.saldoEstoque -eq 15) $r.Status
$r = Invoke-Api POST "/api/artigos/$art/estoque/saidas" $adm @{ quantidade = 0.5 }; Test-Passo 'Quantidade fracionada recusada' ($r.Status -eq 400) $r.Status

$r = Invoke-Api POST '/api/pessoas' $adm @{ nome = 'Cliente Validacao'; email = "cli.$u@example.com"; cpf = (New-Cpf) }
$r = Invoke-Api POST '/api/clientes' $adm @{ pessoaId = $r.Json.id; telefone = '17999990000' }; Test-Passo 'Cadastrar cliente' ($r.Status -eq 201) $r.Status
$cli = $r.Json.id
$r = Invoke-Api POST '/api/pessoas' $adm @{ nome = 'Vendedor Validacao'; email = "ven.$u@example.com"; cpf = (New-Cpf) }
$pes = $r.Json.id
$r = Invoke-Api POST '/api/colaboradores' $adm @{ pessoaId = $pes; matricula = "V$u"; funcao = 'VENDEDOR'; dataAdmissao = '2026-01-05' }; Test-Passo 'Cadastrar vendedor' ($r.Status -eq 201) $r.Status
$ven = $r.Json.id
$senha = [guid]::NewGuid().ToString('N')
$r = Invoke-Api POST '/api/contas-acesso' $adm @{ pessoaId = $pes; login = "val$u"; senha = $senha }; Test-Passo 'Criar conta de operador' ($r.Status -eq 201) $r.Status
$r = Invoke-Api POST '/api/autenticacao/login' $null @{ login = "val$u"; senha = $senha }; Test-Passo 'Login do operador' ($r.Status -eq 200) $r.Status
$op = $r.Json.accessToken
$refresh = $r.Json.refreshToken

$r = Invoke-Api POST '/api/vendas' $op @{ clienteId = $cli; vendedorId = $ven }; Test-Passo 'Abrir venda' ($r.Status -eq 201) $r.Status
$v = $r.Json.id
$r = Invoke-Api POST "/api/vendas/$v/itens" $op @{ artigoId = $art; quantidade = 1 }; Test-Passo 'Adicionar item' ($r.Status -eq 200) $r.Status
$item = $r.Json.itens[0].id
$r = Invoke-Api DELETE "/api/vendas/$v/itens/$item" $op; Test-Passo 'Operador remove item de venda aberta' ($r.Status -eq 200) $r.Status
$r = Invoke-Api POST "/api/vendas/$v/itens" $op @{ artigoId = $art; quantidade = 3 }; Test-Passo 'Adicionar item novamente' ($r.Status -eq 200) $r.Status
$r = Invoke-Api POST "/api/vendas/$v/finalizacao" $op; Test-Passo 'Finalizar venda' ($r.Status -eq 200 -and $r.Json.status -eq 'FINALIZADA') $r.Status
$r = Invoke-Api POST "/api/vendas/$v/finalizacao" $op; Test-Passo 'Segunda finalizacao recusada' ($r.Status -eq 400) $r.Status
$r = Invoke-Api GET "/api/artigos/$art" $op; Test-Passo 'Estoque baixou uma unica vez (15 - 3 = 12)' ($r.Json.saldoEstoque -eq 12) $r.Json.saldoEstoque
$r = Invoke-Api PUT "/api/pessoas/$pes/status" $op @{ status = 'INATIVO' }; Test-Passo 'Operador nao altera status de pessoa' ($r.Status -eq 403) $r.Status
$r = Invoke-Api DELETE "/api/artigos/$art" $op; Test-Passo 'Operador nao exclui artigo' ($r.Status -eq 403) $r.Status
$r = Invoke-Api DELETE "/api/artigos/$art" $adm; Test-Passo 'Artigo vendido nao pode ser excluido' ($r.Status -eq 409) $r.Status
$r = Invoke-Api GET '/api/rota-inexistente' $op; Test-Passo 'Rota inexistente devolve 404' ($r.Status -eq 404) $r.Status
$r = Invoke-Api POST '/api/autenticacao/refresh' $null @{ refreshToken = $refresh }; Test-Passo 'Renovar sessao' ($r.Status -eq 200) $r.Status
$r = Invoke-Api GET '/api/auditoria-seguranca' $adm; Test-Passo 'Consultar auditoria' ($r.Status -eq 200 -and $r.Json.totalElementos -ge 1) $r.Status
$r = Invoke-Api GET '/v3/api-docs'; Test-Passo 'Contrato OpenAPI' ($r.Status -eq 200 -and $r.Json.info.title -eq 'NotoriusFight API') $r.Status

if ($script:falhas -gt 0) { Write-Host "$($script:falhas) passo(s) falharam."; exit 1 }
Write-Host 'Fluxo completo validado.'