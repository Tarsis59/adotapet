<#
  Sobe o AdotaPet inteiro para a apresentacao: banco, backend e frontend.

  Uso (na pasta do projeto, em qualquer PowerShell):
    powershell -ExecutionPolicy Bypass -File .\apresentar.ps1                 # sobe tudo
    powershell -ExecutionPolicy Bypass -File .\apresentar.ps1 -ResetarDados   # sobe tudo com os 8 animais originais
    powershell -ExecutionPolicy Bypass -File .\apresentar.ps1 -Parar          # derruba backend e frontend

  O backend e o frontend abrem em janelas proprias ("AdotaPet - Backend" e "AdotaPet - Frontend").
  Na demo de persistencia (slide 13): na janela do backend, Ctrl+C, responda S, seta para cima e Enter
  (ou digite subir e Enter).
#>
param(
    [switch]$ResetarDados,
    [switch]$Parar,
    [switch]$SemNavegador
)

# 'Continue' e nao 'Stop': no Windows PowerShell 5.1, com 'Stop', qualquer linha que o docker/npm escreva
# no stderr (mesmo em sucesso) vira erro fatal. As falhas sao conferidas uma a uma pelo $LASTEXITCODE.
$ErrorActionPreference = 'Continue'
$raiz = $PSScriptRoot
$arquivoPids = Join-Path $raiz '.apresentar.pids'

function Etapa($texto) { Write-Host ''; Write-Host "==> $texto" -ForegroundColor Cyan }
function Ok($texto) { Write-Host "    OK  $texto" -ForegroundColor Green }
function Aviso($texto) { Write-Host "    !!  $texto" -ForegroundColor Yellow }
function Falha($texto) { Write-Host ''; Write-Host "FALHOU: $texto" -ForegroundColor Red; exit 1 }

function PortaEmUso($porta) {
    return [bool](Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue)
}

function DonoDaPorta($porta) {
    $conexao = Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $conexao) { return $null }
    return Get-Process -Id $conexao.OwningProcess -ErrorAction SilentlyContinue
}

function StatusHttp($url) {
    try {
        $resposta = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        return [int]$resposta.StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        return 0
    }
}

function EsperarHttp($url, $esperado, $segundos, $descricao) {
    $limite = (Get-Date).AddSeconds($segundos)
    while ((Get-Date) -lt $limite) {
        if ($esperado -contains (StatusHttp $url)) { return $true }
        Start-Sleep -Seconds 3
        Write-Host '.' -NoNewline
    }
    Write-Host ''
    Falha "$descricao nao respondeu em $segundos segundos ($url). Veja a janela correspondente."
}

function PararTudo {
    Etapa 'Parando backend e frontend de execucoes anteriores'
    if (Test-Path $arquivoPids) {
        foreach ($processoId in Get-Content $arquivoPids) {
            if ($processoId -and (Get-Process -Id $processoId -ErrorAction SilentlyContinue)) {
                & taskkill.exe /T /F /PID $processoId 2>$null | Out-Null
            }
        }
        Remove-Item $arquivoPids -Force
    }
    # Sobras sem janela: so derruba java/node que estejam ouvindo nas portas do projeto.
    foreach ($porta in 4200, 8080, 8081, 8082) {
        $dono = DonoDaPorta $porta
        if ($dono -and @('java', 'node') -contains $dono.ProcessName) {
            & taskkill.exe /T /F /PID $dono.Id 2>$null | Out-Null
            Ok "porta $porta liberada ($($dono.ProcessName))"
        }
    }
    Start-Sleep -Seconds 2
    Ok 'nada do AdotaPet rodando'
}

function AbrirJanela($titulo, $pasta, $comando, [switch]$Reiniciavel) {
    # Depois de um Ctrl+C, seta para cima + Enter roda o comando de novo. O AddToHistory do PSReadLine
    # falha antes do primeiro prompt, entao o comando vai direto para o arquivo de historico, que o
    # PSReadLine le no primeiro prompt. O historico e compartilhado entre janelas, por isso so o backend
    # (o unico que se reinicia na demo) vai para ele. Reserva: a funcao "subir" na janela faz a mesma coisa.
    $historico = Join-Path $env:APPDATA 'Microsoft\Windows\PowerShell\PSReadLine\ConsoleHost_history.txt'
    if ($Reiniciavel) { try {
        New-Item -ItemType Directory -Force (Split-Path $historico) | Out-Null
        $quebra = ''
        if ((Test-Path $historico) -and (Get-Item $historico).Length -gt 0 -and
            -not (Get-Content $historico -Raw).EndsWith("`n")) { $quebra = "`r`n" }
        [IO.File]::AppendAllText($historico, "$quebra$comando`r`n")
    } catch { } }
    # -EncodedCommand evita que as aspas do comando se percam ao passar de um processo para o outro.
    $script = "`$Host.UI.RawUI.WindowTitle = '$titulo'; Set-Location '$pasta'; " +
              "function global:subir { $comando }; " +
              $comando
    $codificado = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($script))
    $processo = Start-Process powershell.exe -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $codificado -PassThru
    Add-Content -Path $arquivoPids -Value $processo.Id
}

if ($Parar) {
    PararTudo
    exit 0
}

Write-Host 'AdotaPet - preparando a apresentacao' -ForegroundColor Magenta

# 1. Pre-requisitos -------------------------------------------------------------------------------
Etapa 'Conferindo Java, Node e memoria'
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { Falha 'Java nao encontrado. Instale o Java 21.' }
if (-not (Get-Command node -ErrorAction SilentlyContinue)) { Falha 'Node.js nao encontrado. Instale o Node 22.22 ou superior.' }
Ok "Node $(node -v)"

$so = Get-CimInstance Win32_OperatingSystem
$memoriaLivreGb = [math]::Round($so.FreeVirtualMemory / 1MB, 1)
if ($memoriaLivreGb -lt 3) {
    Aviso "So $memoriaLivreGb GB de memoria livre. Feche VS Code, outras sessoes do Claude e abas do Chrome."
} else {
    Ok "$memoriaLivreGb GB de memoria livre"
}

PararTudo

# 2. Banco ----------------------------------------------------------------------------------------
Etapa 'Subindo o PostgreSQL (Docker)'
$usarH2 = $false
& docker info *> $null
if ($LASTEXITCODE -ne 0) {
    $dockerDesktop = Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'
    if (Test-Path $dockerDesktop) {
        Aviso 'Docker parado; abrindo o Docker Desktop (pode levar 1 minuto)'
        Start-Process $dockerDesktop
        $limite = (Get-Date).AddSeconds(150)
        do { Start-Sleep -Seconds 5; & docker info *> $null } while ($LASTEXITCODE -ne 0 -and (Get-Date) -lt $limite)
    }
}
& docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Aviso 'Docker indisponivel: usando o Plano B (H2 em arquivo). Na fala do slide 13 diga "banco H2 em arquivo".'
    $usarH2 = $true
} else {
    Push-Location $raiz
    & docker compose up -d *> $null
    $codigoCompose = $LASTEXITCODE
    Pop-Location
    if ($codigoCompose -ne 0) { Falha 'docker compose up falhou. Rode "docker compose up -d" para ver o erro.' }
    $limite = (Get-Date).AddSeconds(60)
    do {
        Start-Sleep -Seconds 2
        $saude = (& docker inspect -f '{{.State.Health.Status}}' adotapet-postgres 2>$null)
    } while ($saude -ne 'healthy' -and (Get-Date) -lt $limite)
    if ($saude -ne 'healthy') { Falha 'O PostgreSQL nao ficou pronto. Rode "docker compose logs postgres".' }
    Ok 'PostgreSQL pronto na porta 5434'
}

# 3. Dados de demonstracao ------------------------------------------------------------------------
if ($ResetarDados) {
    Etapa 'Voltando os animais para a carga inicial (8 animais)'
    if ($usarH2) {
        Aviso 'No H2 nao ha reset automatico; seguindo com os dados atuais.'
    } else {
        $tabela = "$(& docker exec adotapet-postgres psql -U adotapet -d adotapet -tAc "select to_regclass('public.animais')" 2>$null)".Trim()
        if ($tabela -eq 'animais') {
            & docker exec adotapet-postgres psql -U adotapet -d adotapet -c 'TRUNCATE animais RESTART IDENTITY;' *> $null
            if ($LASTEXITCODE -ne 0) { Falha 'nao consegui apagar os animais no PostgreSQL.' }
            Ok 'animais apagados; o backend recria os 8 de exemplo ao subir (o admin continua)'
        } else {
            Ok 'banco novo; o backend cria tudo ao subir'
        }
    }
}

# 4. Backend --------------------------------------------------------------------------------------
Etapa 'Subindo o backend (Spring Boot)'
$portaApi = $null
foreach ($porta in 8080, 8081, 8082) {
    if (-not (PortaEmUso $porta)) { $portaApi = $porta; break }
    $dono = DonoDaPorta $porta
    Aviso "porta $porta ocupada por $($dono.ProcessName)"
}
if (-not $portaApi) { Falha 'As portas 8080, 8081 e 8082 estao ocupadas.' }

$argumentos = "-Dspring-boot.run.arguments=--server.port=$portaApi"
if ($usarH2) { $argumentos = "-Dspring-boot.run.profiles=h2"" ""$argumentos" }
AbrirJanela 'AdotaPet - Backend' (Join-Path $raiz 'backend') ".\mvnw.cmd spring-boot:run ""$argumentos""" -Reiniciavel
Write-Host "    aguardando a API na porta $portaApi" -NoNewline
EsperarHttp "http://localhost:$portaApi/api/animais" @(401) 300 'O backend' | Out-Null
Write-Host ''
Ok "API no ar: http://localhost:$portaApi"

# 5. Frontend -------------------------------------------------------------------------------------
Etapa 'Subindo o frontend (Angular)'
$pastaFront = Join-Path $raiz 'frontend'
if (-not (Test-Path (Join-Path $pastaFront 'node_modules'))) {
    Aviso 'primeira execucao: rodando npm install (alguns minutos)'
    Push-Location $pastaFront
    & npm install --no-audit --no-fund
    $codigoNpm = $LASTEXITCODE
    Pop-Location
    if ($codigoNpm -ne 0) { Falha 'npm install falhou. Veja as mensagens acima.' }
}
if (PortaEmUso 4200) { Falha "A porta 4200 esta ocupada por $((DonoDaPorta 4200).ProcessName)." }
# NG_FORCE_AUTOCOMPLETE: numa janela de console o Angular CLI pergunta se quer ativar o autocompletar
# e fica parado esperando resposta, sem nunca subir o servidor. NG_CLI_ANALYTICS faz o mesmo para a pergunta de telemetria.
AbrirJanela 'AdotaPet - Frontend' $pastaFront "`$env:API_PORT = '$portaApi'; `$env:NG_FORCE_AUTOCOMPLETE = 'false'; `$env:NG_CLI_ANALYTICS = 'false'; npm start"
Write-Host '    aguardando o Angular na porta 4200' -NoNewline
EsperarHttp 'http://localhost:4200/' @(200) 240 'O frontend' | Out-Null
EsperarHttp 'http://localhost:4200/api/animais' @(401) 60 'O proxy do frontend para a API' | Out-Null
Write-Host ''
Ok 'frontend no ar: http://localhost:4200'

# 6. Teste de fumaca pelo mesmo caminho que o navegador usa --------------------------------------
Etapa 'Conferindo login e dados pela interface'
try {
    $corpo = '{"email":"admin@adotapet.com","senha":"Admin@123"}'
    $login = Invoke-RestMethod -Method Post -Uri 'http://localhost:4200/api/auth/login' -ContentType 'application/json' -Body $corpo -ErrorAction Stop
    $cabecalho = @{ Authorization = "Bearer $($login.token)" }
    $totais = Invoke-RestMethod -Uri 'http://localhost:4200/api/animais/estatisticas' -Headers $cabecalho -ErrorAction Stop
} catch {
    Falha "o login de demonstracao nao funcionou: $($_.Exception.Message)"
}
Ok "login admin@adotapet.com funcionando (token expira em $($login.expiraEm))"
Ok "animais: $($totais.total) no total, $($totais.disponiveis) disponiveis, $($totais.emProcesso) em processo, $($totais.adotados) adotados"
if ($totais.total -ne 8 -or $totais.adotados -ne 1) {
    Aviso 'os dados nao estao na carga inicial (8 / 5 / 2 / 1). Para a demo seguir o roteiro, rode de novo com -ResetarDados.'
}

# 7. Navegador ------------------------------------------------------------------------------------
if (-not $SemNavegador) {
    Etapa 'Abrindo o navegador'
    $chrome = @(
        (Join-Path $env:ProgramFiles 'Google\Chrome\Application\chrome.exe'),
        (Join-Path ${env:ProgramFiles(x86)} 'Google\Chrome\Application\chrome.exe'),
        (Join-Path $env:LOCALAPPDATA 'Google\Chrome\Application\chrome.exe')
    ) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
    $urls = @('http://localhost:4200/', "http://localhost:$portaApi/swagger-ui.html")
    if ($chrome) {
        Start-Process $chrome -ArgumentList (@('--incognito', '--new-window') + $urls)
        Ok 'janela anonima do Chrome com o sistema e o Swagger'
    } else {
        foreach ($url in $urls) { Start-Process $url }
        Ok 'abri no navegador padrao (use uma aba anonima para comecar sem sessao)'
    }
}

Write-Host ''
Write-Host '============================================================' -ForegroundColor Green
Write-Host ' TUDO NO AR' -ForegroundColor Green
Write-Host "   Sistema:  http://localhost:4200" -ForegroundColor Green
Write-Host "   Swagger:  http://localhost:$portaApi/swagger-ui.html" -ForegroundColor Green
Write-Host '   Login:    admin@adotapet.com / Admin@123' -ForegroundColor Green
Write-Host '   Slide 13: janela "AdotaPet - Backend" > Ctrl+C > S > seta para cima > Enter' -ForegroundColor Green
Write-Host '============================================================' -ForegroundColor Green
