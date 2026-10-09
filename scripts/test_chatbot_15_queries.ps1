# scripts/test_chatbot_15_queries.ps1
$baseUrl = "http://localhost:8081"
$cookieFile = "cookies_chat_test.txt"
if (Test-Path $cookieFile) { Remove-Item $cookieFile }

$queries = @(
    "Hello there! What can you help me with?",
    "Do you sell any wireless headphones?",
    "What is the exact price of the Sony WH-1000XM5?",
    "Is the Keychron K2 keyboard currently in stock?",
    "Compare the Sony headphones with boAt earbuds",
    "What products do you have under 2000 rupees?",
    "Show me items in Books & Stationery",
    "What are your top laptop and camera options over 1 lakh?",
    "Can you tell me more about the first item you mentioned?",
    "What is your return and cancellation policy?",
    "How does shipping and order delivery work?",
    "What is the weather in Paris today?",
    "How do I track my order?",
    "",
    "Show me everything from Nike"
)

Write-Host "=========================================================="
Write-Host "       TESTING AI CHATBOT WITH 15 DIVERSE QUESTIONS       "
Write-Host "==========================================================`n"

$index = 1
foreach ($q in $queries) {
    Write-Host "[$index/15] USER QUESTION: '$q'"
    $jsonPayload = [PSCustomObject]@{ message = $q } | ConvertTo-Json -Compress
    $tempFile = [System.IO.Path]::GetTempFileName()
    [System.IO.File]::WriteAllText($tempFile, $jsonPayload, [System.Text.Encoding]::UTF8)

    $response = curl.exe -s -b $cookieFile -c $cookieFile -X POST "$baseUrl/api/v1/chat" `
        -H "Content-Type: application/json; charset=UTF-8" `
        -d "@$tempFile"

    if (Test-Path $tempFile) { Remove-Item $tempFile }

    try {
        $parsed = $response | ConvertFrom-Json
        if ($parsed.success -eq $true) {
            $reply = $parsed.data.reply
            Write-Host "BOT REPLY:`n$reply`n"
        } else {
            Write-Host "VALIDATION/ERROR: $($parsed.message) (Code: $($parsed.errorCode))`n"
        }
    } catch {
        Write-Host "RAW RESPONSE: $response`n"
    }

    $index++
}
