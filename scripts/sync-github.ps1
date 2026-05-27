param(
    [string]$MessagePrefix = "Auto update"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

git add -A
$status = git status --porcelain
if ([string]::IsNullOrWhiteSpace($status)) {
    "[$timestamp] No changes to sync." | Tee-Object -FilePath "sync-github.log" -Append
    exit 0
}

$commitMessage = "$MessagePrefix: $timestamp"
git commit -m $commitMessage
git push

"[$timestamp] Synced: $commitMessage" | Tee-Object -FilePath "sync-github.log" -Append
