$ErrorActionPreference = 'Stop'
if (-not $env:ORCA_ROOT_PATH -or -not $env:ORCA_WORKTREE_PATH) {
    throw 'Orcaのworktreeセットアップから実行してください。'
}

$sourceName = (Split-Path -Leaf $env:ORCA_ROOT_PATH).Replace(' ', '_')
$targetName = (Split-Path -Leaf $env:ORCA_WORKTREE_PATH).Replace(' ', '_')
$launchPath = (Join-Path $env:ORCA_WORKTREE_PATH '.gradle/loom-cache/launch.cfg').Replace(' ', '@@0020')
$configDirectory = Join-Path $env:ORCA_WORKTREE_PATH '.idea/runConfigurations'
foreach ($configFile in Get-ChildItem -LiteralPath $configDirectory -Filter '*.xml' -File) {
    [xml]$document = Get-Content -LiteralPath $configFile.FullName -Raw -Encoding UTF8
    $configuration = $document.component.configuration
    if ($configuration.module -and $configuration.module.name.StartsWith("$sourceName.")) {
        $configuration.module.SetAttribute('name', $targetName + $configuration.module.name.Substring($sourceName.Length))
    }
    $vmOption = $configuration.option | Where-Object { $_.name -eq 'VM_PARAMETERS' }
    if ($vmOption) {
        $vmOption.SetAttribute('value', [regex]::Replace(
            $vmOption.value,
            '-Dfabric\.dli\.config=(?:"[^"]*"|\S+)',
            { param($match) '-Dfabric.dli.config=' + $launchPath }
        ))
    }
    $document.Save($configFile.FullName)
}
