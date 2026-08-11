$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../CompilerFr.Core.psm1') -Force

$passed = 0; $failed = 0
function Assert-That {
    param([bool]$Condition, [string]$Name)
    if ($Condition) { $script:passed++; Write-Host "OK   $Name" -ForegroundColor Green }
    else { $script:failed++; Write-Host "ECHEC $Name" -ForegroundColor Red }
}
function Get-OneResolved([string]$Text) {
    $items = @(ConvertTo-CompilerFrDiagnostics -Output $Text | ForEach-Object { Resolve-CompilerFrDiagnostic $_ })
    return $items[0]
}
function Join-Fixture([string[]]$Lines) { return [string]::Join([Environment]::NewLine, $Lines) }

$symbol = Get-OneResolved (Join-Fixture @('C:\Projects\Primary School\src\main\java\Demo.java:49: error: cannot find symbol', 'List<String> values = use(academicYearId);', '                          ^', 'symbol:   variable academicYearId', 'location: class Demo'))
Assert-That ($symbol.Category -eq 'symbole introuvable' -and $symbol.Diagnostic.File -eq 'C:\Projects\Primary School\src\main\java\Demo.java' -and $symbol.Diagnostic.Line -eq 49 -and $symbol.Diagnostic.Column -eq 27 -and $symbol.FrenchMessage -match 'academicYearId') 'variable introuvable et chemin Windows avec espaces'

$package = Get-OneResolved (Join-Fixture @('src/main/java/Demo.java:3: error: package com.acme.missing does not exist', 'import com.acme.missing.Type;'))
Assert-That ($package.Category -eq 'package ou import introuvable') 'package ou import introuvable'

$types = Get-OneResolved (Join-Fixture @('src/main/java/Demo.java:8: error: incompatible types: String cannot be converted to int', 'int age = "douze";', '          ^'))
Assert-That ($types.Category -eq 'types incompatibles') 'types incompatibles'

$method = Get-OneResolved (Join-Fixture @('src/main/java/Demo.java:8: error: method save in class Service cannot be applied to given types;', 'service.save();', '       ^', 'required: String', 'found: no arguments'))
Assert-That ($method.Category -eq 'appel de methode invalide') 'methode avec mauvais parametres'

$abstract = Get-OneResolved (Join-Fixture @('src/main/java/Demo.java:1: error: Demo is not abstract and does not override abstract method run() in Runnable', 'class Demo implements Runnable {}', '^'))
Assert-That ($abstract.Category -eq 'methode abstraite non implementee') 'methode d interface non implemente'

$semicolon = Get-OneResolved (Join-Fixture @("src/main/java/Demo.java:4: error: ';' expected", 'String name = "Ada"', '                   ^'))
Assert-That ($semicolon.Category -eq 'point-virgule manquant') 'point-virgule manquant'

$gradle = Get-OneResolved 'Could not resolve org.example:missing:1.0.'
Assert-That ($gradle.Category -eq 'dependance Gradle introuvable' -and -not $gradle.Diagnostic.File) 'erreur Gradle non localisable'

$unknown = Get-OneResolved (Join-Fixture @('src/main/java/Demo.java:7: error: diagnostic experimental inconnu', 'something', '^'))
Assert-That (-not $unknown.Known -and $unknown.Category -eq 'diagnostic non repertorie') 'erreur inconnue conservee'

$multi = Join-Fixture @('src/main/java/One.java:2: error: cannot find symbol', 'missing();', '^', 'symbol: method missing()', 'src/main/java/Two.java:3: error: incompatible types: String cannot be converted to int', 'int n = "x";', '        ^')
$many = @(ConvertTo-CompilerFrDiagnostics -Output $multi)
Assert-That ($many.Count -eq 2 -and $many[0].File -match 'One.java' -and $many[1].File -match 'Two.java') 'plusieurs erreurs dans plusieurs fichiers'

$successfulWrapperOutput = "gradle-wrapper.jar executing normally"
$none = @(ConvertTo-CompilerFrDiagnostics -Output $successfulWrapperOutput)
Assert-That ($none.Count -eq 0) 'pas de faux positif Wrapper sur une sortie saine'

Write-Host ""
Write-Host "Resultat : $passed test(s) reussi(s), $failed echec(s)."
if ($failed -gt 0) { exit 1 }
