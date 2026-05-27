param(
    [string]$MessagePrefix = "Auto update",
    [string]$Repo = "nmklio/AdinCosL",
    [string]$Branch = "main"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$logFile = Join-Path $repoRoot "sync-github.log"

function Write-SyncLog {
    param([string]$Message)
    "[$timestamp] $Message" | Tee-Object -FilePath $logFile -Append
}

function Publish-WithGitHubApi {
    param(
        [string]$Repository,
        [string]$TargetBranch,
        [string]$CommitMessage
    )

    $baseRef = gh api "repos/$Repository/git/ref/heads/$TargetBranch" | ConvertFrom-Json
    $baseCommit = $baseRef.object.sha
    $files = git ls-files
    $treeItems = @()

    foreach ($file in $files) {
        $bytes = [System.IO.File]::ReadAllBytes((Join-Path (Get-Location) $file))
        $content = [Convert]::ToBase64String($bytes)
        $blobPayload = @{ content = $content; encoding = "base64" } | ConvertTo-Json -Compress
        $blob = $blobPayload | gh api "repos/$Repository/git/blobs" --method POST --input - | ConvertFrom-Json
        $treeItems += @{
            path = $file.Replace("\", "/")
            mode = "100644"
            type = "blob"
            sha = $blob.sha
        }
    }

    $treePayload = @{ base_tree = $baseCommit; tree = $treeItems } | ConvertTo-Json -Depth 5 -Compress
    $tree = $treePayload | gh api "repos/$Repository/git/trees" --method POST --input - | ConvertFrom-Json
    $commitPayload = @{
        message = $CommitMessage
        tree = $tree.sha
        parents = @($baseCommit)
    } | ConvertTo-Json -Depth 4 -Compress
    $commit = $commitPayload | gh api "repos/$Repository/git/commits" --method POST --input - | ConvertFrom-Json
    $updatePayload = @{ sha = $commit.sha; force = $false } | ConvertTo-Json -Compress
    $updatePayload | gh api "repos/$Repository/git/refs/heads/$TargetBranch" --method PATCH --input - | Out-Null
}

git add -A
$status = git status --porcelain
if ([string]::IsNullOrWhiteSpace($status)) {
    Write-SyncLog "No changes to sync."
    exit 0
}

$commitMessage = "${MessagePrefix}: $timestamp"
git commit -m $commitMessage

try {
    git push
    if ($LASTEXITCODE -ne 0) {
        throw "git push exited with code $LASTEXITCODE"
    }
    Write-SyncLog "Synced with git push: $commitMessage"
} catch {
    Write-SyncLog "git push failed, falling back to GitHub API: $($_.Exception.Message)"
    Publish-WithGitHubApi -Repository $Repo -TargetBranch $Branch -CommitMessage $commitMessage
    Write-SyncLog "Synced with GitHub API: $commitMessage"
}
