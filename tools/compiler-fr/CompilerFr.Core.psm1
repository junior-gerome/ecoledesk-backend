Set-StrictMode -Version Latest

function Remove-CompilerFrAnsi {
    param([AllowNull()][string]$Text)
    if ($null -eq $Text) { return '' }
    return [regex]::Replace($Text, "`e\[[0-?]*[ -/]*[@-~]", '')
}

function Get-CompilerFrRules {
    $rulesPath = Join-Path $PSScriptRoot 'diagnostics.fr.json'
    return (Get-Content -LiteralPath $rulesPath -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Get-CompilerFrCodeExcerpt {
    param([string]$File, [Nullable[int]]$Line, [Nullable[int]]$Column, [string]$FallbackCode)
    $code = $FallbackCode
    if ($File -and $Line -and (Test-Path -LiteralPath $File)) {
        $lines = [System.IO.File]::ReadAllLines($File)
        if ($Line.Value -gt 0 -and $Line.Value -le $lines.Length) { $code = $lines[$Line.Value - 1] }
    }
    if (-not $code -or -not $Line) { return $null }
    $safeColumn = if ($Column -and $Column.Value -gt 0) { $Column.Value } else { 1 }
    $visibleCode = $code -replace "`t", '    '
    $prefix = (' ' * ($Line.Value.ToString().Length + 3))
    $pointer = $prefix + (' ' * ($safeColumn - 1)) + '^ ici'
    return [pscustomobject]@{ Text = ('{0} | {1}' -f $Line.Value, $visibleCode); Pointer = $pointer }
}

function ConvertTo-CompilerFrDiagnostics {
    param([Parameter(Mandatory)][string]$Output)
    $lines = @($Output -split "`r?`n" | ForEach-Object { Remove-CompilerFrAnsi $_ })
    $diagnostics = [System.Collections.Generic.List[object]]::new()
    $javacPattern = '^(?<file>.+?):(?<line>\d+):(?:(?<column>\d+):)?\s*error:\s*(?<message>.+)$'
    for ($index = 0; $index -lt $lines.Count; $index++) {
        $match = [regex]::Match($lines[$index], $javacPattern)
        if (-not $match.Success) { continue }
        $details = [System.Collections.Generic.List[string]]::new()
        $cursor = $index + 1
        while ($cursor -lt $lines.Count -and -not [regex]::IsMatch($lines[$cursor], $javacPattern) -and -not [regex]::IsMatch($lines[$cursor], '^\d+ errors?$')) {
            if ($lines[$cursor].Trim()) { $details.Add($lines[$cursor]) }
            $cursor++
        }
        $code = $null; $caret = $null
        for ($detailIndex = 0; $detailIndex -lt $details.Count; $detailIndex++) {
            if ($details[$detailIndex] -match '^\s*\^') {
                $caret = $details[$detailIndex]
                if ($detailIndex -gt 0) { $code = $details[$detailIndex - 1] }
                break
            }
        }
        $column = $null
        if ($match.Groups['column'].Success) { $column = [int]$match.Groups['column'].Value }
        elseif ($caret) { $column = $caret.IndexOf('^') + 1 }
        $diagnostics.Add([pscustomobject]@{
            Kind = 'javac'; File = $match.Groups['file'].Value; Line = [int]$match.Groups['line'].Value; Column = $column
            Message = $match.Groups['message'].Value; Details = $details.ToArray(); CodeLine = $code
            Raw = (($lines[$index..($cursor - 1)]) -join [Environment]::NewLine)
        })
        $index = $cursor - 1
    }
    $gradlePatterns = @('Could not resolve', 'Plugin .* was not found', "Task '.+' not found", 'Unsupported class file major version', 'requires Java', 'Could not compile build file', '(?:Could not|Unable to|Error).*gradle-wrapper', 'Could not download', 'Read timed out', 'ConnectException')
    foreach ($line in $lines) {
        foreach ($pattern in $gradlePatterns) {
            if ($line -match $pattern) {
                $diagnostics.Add([pscustomobject]@{ Kind = 'gradle'; File = $null; Line = $null; Column = $null; Message = $line.Trim(); Details = @(); CodeLine = $null; Raw = $line.Trim() })
                break
            }
        }
    }
    return @($diagnostics)
}

function Resolve-CompilerFrDiagnostic {
    param([Parameter(Mandatory)]$Diagnostic, [object[]]$Rules = (Get-CompilerFrRules))
    $technicalText = $Diagnostic.Message + "`n" + ($Diagnostic.Details -join "`n")
    $rule = $Rules | Where-Object { $technicalText -match $_.pattern } | Select-Object -First 1
    $symbol = $null
    if ($technicalText -match 'symbol:\s+(?:variable|method|class|interface)\s+([^\s(]+)') { $symbol = $Matches[1] }
    elseif ($technicalText -match 'cannot find symbol') { $symbol = 'utilise dans ce code' }
    if ($rule) {
        $message = $rule.message
        if ($symbol) { $message = $message.Replace('{symbol}', $symbol) } else { $message = $message.Replace('{symbol}', 'utilise dans ce code') }
        return [pscustomobject]@{ Diagnostic = $Diagnostic; Known = $true; Category = $rule.category; FrenchMessage = $message; Cause = $rule.cause; Correction = $rule.correction; Symbol = $symbol }
    }
    return [pscustomobject]@{ Diagnostic = $Diagnostic; Known = $false; Category = 'diagnostic non repertorie'; FrenchMessage = 'Ce diagnostic n''est pas encore repertorie par le dictionnaire francais.'; Cause = 'Le compilateur ou Gradle a produit un message dont aucune regle stable n''est definie.'; Correction = 'Conservez le diagnostic technique ci-dessous, puis ajoutez une regle dans tools/compiler-fr/diagnostics.fr.json et un test dans tools/compiler-fr/tests.'; Symbol = $null }
}

function Write-CompilerFrReport {
    param([Parameter(Mandatory)][AllowEmptyCollection()][object[]]$ResolvedDiagnostics, [int]$ExitCode)
    if ($ResolvedDiagnostics.Count -eq 0) {
        if ($ExitCode -eq 0) { Write-Host 'Compilation Gradle reussie : aucun diagnostic de compilation.' -ForegroundColor Green }
        else { Write-Host 'Gradle a echoue, mais aucun diagnostic localisable n''a ete reconnu. Consultez la sortie technique.' -ForegroundColor Yellow }
        return
    }
    for ($i = 0; $i -lt $ResolvedDiagnostics.Count; $i++) {
        $item = $ResolvedDiagnostics[$i]; $diagnostic = $item.Diagnostic
        Write-Host ("`nERREUR {0}/{1}" -f ($i + 1), $ResolvedDiagnostics.Count) -ForegroundColor Red
        if ($diagnostic.File) { Write-Host ('Fichier   : ' + $diagnostic.File) } else { Write-Host 'Fichier   : non localisable (Gradle)' }
        if ($diagnostic.Line) { Write-Host ('Position  : ligne {0}, colonne {1}' -f $diagnostic.Line, $(if ($diagnostic.Column) { $diagnostic.Column } else { '?' })) } else { Write-Host 'Position  : non localisable' }
        Write-Host ('Categorie : ' + $item.Category)
        Write-Host "`nErreur :"; Write-Host $item.FrenchMessage
        Write-Host "`nCause probable :"; Write-Host $item.Cause
        Write-Host "`nCorrection proposee :"; Write-Host $item.Correction
        $excerpt = Get-CompilerFrCodeExcerpt -File $diagnostic.File -Line $diagnostic.Line -Column $diagnostic.Column -FallbackCode $diagnostic.CodeLine
        if ($excerpt) { Write-Host "`nCode concerne :"; Write-Host $excerpt.Text; Write-Host $excerpt.Pointer }
        if (-not $item.Known) { Write-Host "`nDiagnostic technique original :"; Write-Host $diagnostic.Raw -ForegroundColor DarkYellow }
    }
    Write-Host ("`nResume : {0} diagnostic(s) affiche(s) en francais. Code de sortie Gradle : {1}." -f $ResolvedDiagnostics.Count, $ExitCode)
}

function Invoke-CompilerFrGradle {
    param([Parameter(Mandatory)][string]$ProjectRoot, [Parameter(Mandatory)][string]$Task)
    $isWindows = [Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT
    $wrapper = if ($isWindows) { Join-Path $ProjectRoot 'gradlew.bat' } else { Join-Path $ProjectRoot 'gradlew' }
    if (-not (Test-Path -LiteralPath $wrapper)) { throw "Gradle Wrapper introuvable : $wrapper" }
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $wrapper; $startInfo.WorkingDirectory = $ProjectRoot; $startInfo.Arguments = "$Task --console=plain"
    $startInfo.UseShellExecute = $false; $startInfo.RedirectStandardOutput = $true; $startInfo.RedirectStandardError = $true; $startInfo.CreateNoWindow = $true
    $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8; $startInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    $process = New-Object System.Diagnostics.Process; $process.StartInfo = $startInfo
    [void]$process.Start(); $stdoutTask = $process.StandardOutput.ReadToEndAsync(); $stderrTask = $process.StandardError.ReadToEndAsync(); $process.WaitForExit()
    return [pscustomobject]@{ ExitCode = $process.ExitCode; StandardOutput = $stdoutTask.Result; StandardError = $stderrTask.Result; CombinedOutput = ($stdoutTask.Result + [Environment]::NewLine + $stderrTask.Result) }
}

Export-ModuleMember -Function Remove-CompilerFrAnsi, Get-CompilerFrRules, Get-CompilerFrCodeExcerpt, ConvertTo-CompilerFrDiagnostics, Resolve-CompilerFrDiagnostic, Write-CompilerFrReport, Invoke-CompilerFrGradle