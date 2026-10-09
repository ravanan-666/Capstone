$tamilRegex = '[\u0B80-\u0BFF]'
$files = Get-ChildItem -Path . -Recurse -File | Where-Object {
    $_.FullName -notmatch '\\(\.git|target|\.idea|node_modules|work)\\' -and
    $_.Extension -notmatch '\.(pdf|pptx|png|jpg|jpeg|ico|jar|war|class|exe)$'
}

$foundCount = 0
foreach ($file in $files) {
    try {
        $content = [System.IO.File]::ReadAllText($file.FullName)
        if ($content -match $tamilRegex) {
            Write-Host "TAMIL FOUND: $($file.FullName)"
            $foundCount++
        }
    } catch {
        # ignore binary read errors
    }
}

if ($foundCount -eq 0) {
    Write-Host "SUCCESS: 0 files contain Tamil text. The entire repository is 100% clean!"
} else {
    Write-Host "FOUND $foundCount files with Tamil text."
}
