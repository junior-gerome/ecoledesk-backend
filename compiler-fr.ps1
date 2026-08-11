[CmdletBinding()]
param(
    [ValidateSet('compile', 'test', 'run', 'build', 'clean')]
    [string]$Action = 'compile',
    [switch]$MasquerSortieTechnique
)

$ErrorActionPreference = 'Stop'
# Configuration locale du démarrage de développement avec création du premier administrateur.
# Ces variables sont transmises au processus Gradle uniquement pendant -Action run.
if ($Action -eq 'run') {
    # Une variable déjà définie dans le terminal garde toujours la priorité.
    if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = 'dev' }
    if (-not $env:APP_BOOTSTRAP_ADMIN_ENABLED) { $env:APP_BOOTSTRAP_ADMIN_ENABLED = 'true' }
    if (-not $env:APP_BOOTSTRAP_ADMIN_EMAIL) { $env:APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@school.local' }
    if (-not $env:APP_BOOTSTRAP_ADMIN_PASSWORD) { $env:APP_BOOTSTRAP_ADMIN_PASSWORD = 'Admin@123456' }
}

$projectRoot = $PSScriptRoot
Import-Module (Join-Path $projectRoot 'tools/compiler-fr/CompilerFr.Core.psm1') -Force
$tasks = @{ compile = 'compileJava'; test = 'test'; run = 'bootRun'; build = 'build'; clean = 'clean' }

try {
    $result = Invoke-CompilerFrGradle -ProjectRoot $projectRoot -Task $tasks[$Action]
    if (-not $MasquerSortieTechnique -and $result.CombinedOutput.Trim()) {
        Write-Host '--- Sortie technique Gradle ---' -ForegroundColor DarkCyan
        Write-Host (Remove-CompilerFrAnsi $result.CombinedOutput)
        Write-Host '--- Diagnostic francais ---' -ForegroundColor DarkCyan
    }
    $diagnostics = ConvertTo-CompilerFrDiagnostics -Output $result.CombinedOutput
    $resolved = @($diagnostics | ForEach-Object { Resolve-CompilerFrDiagnostic $_ })
    Write-CompilerFrReport -ResolvedDiagnostics $resolved -ExitCode $result.ExitCode
    exit $result.ExitCode
}
catch {
    Write-Host "ERREUR D'EXECUTION DE L'OUTIL" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}