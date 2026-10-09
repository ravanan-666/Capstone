# scripts/verify_all_features_live.ps1
$baseUrl = "http://localhost:8081"
$cookieBuyer = "cookie_buyer.txt"
$cookieAdmin = "cookie_admin.txt"
$cookieSeller = "cookie_seller.txt"

if (Test-Path $cookieBuyer) { Remove-Item $cookieBuyer }
if (Test-Path $cookieAdmin) { Remove-Item $cookieAdmin }
if (Test-Path $cookieSeller) { Remove-Item $cookieSeller }

$passCount = 0
$failCount = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Url,
        [string]$CookieFile = "",
        [string]$Body = "",
        [string]$ContentType = "application/x-www-form-urlencoded",
        [string]$ExtraHeaders = "",
        [int[]]$ExpectedCodes = @(200, 302),
        [string]$ContentCheck = ""
    )

    $tempFile = ""
    $cmd = "curl.exe -s -i -X $Method `"$Url`""
    if ($CookieFile -ne "") {
        $cmd += " -b `"$CookieFile`" -c `"$CookieFile`""
    }
    if ($ExtraHeaders -ne "") {
        $cmd += " -H `"$ExtraHeaders`""
    }
    if ($Body -ne "") {
        $tempFile = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($tempFile, $Body, [System.Text.Encoding]::UTF8)
        $cmd += " -H `"Content-Type: $ContentType`" -d `"@$tempFile`""
    }

    $raw = Invoke-Expression $cmd
    if ($tempFile -ne "" -and (Test-Path $tempFile)) { Remove-Item $tempFile }

    $statusLine = ($raw | Select-String "HTTP/1.1 (\d{3})").Matches.Groups[1].Value
    $statusCode = 0
    if ($statusLine) { $statusCode = [int]$statusLine }

    $passed = $ExpectedCodes -contains $statusCode
    if ($ContentCheck -ne "" -and $passed) {
        $passed = ($raw -join "`n") -like "*$ContentCheck*"
    }

    if ($passed) {
        Write-Host " [PASS] $Name (HTTP $statusCode)" -ForegroundColor Green
        $script:passCount++
    } else {
        Write-Host " [FAIL] $Name (HTTP $statusCode, expected $($ExpectedCodes -join ','))" -ForegroundColor Red
        $script:failCount++
    }

    return $raw
}

Write-Host "=========================================================="
Write-Host "   DJ MART - COMPREHENSIVE BUTTONS & FEATURES LIVE TEST   "
Write-Host "==========================================================`n"

# 1. PUBLIC NAVIGATION & PRODUCT BROWSING
Test-Endpoint -Name "Homepage Navigation" -Url "$baseUrl/" -ExpectedCodes @(200) -ContentCheck "DJ Mart"
Test-Endpoint -Name "Health Check API" -Url "$baseUrl/api/v1/health" -ExpectedCodes @(200) -ContentCheck "UP"
Test-Endpoint -Name "Product Catalog Grid" -Url "$baseUrl/products" -ExpectedCodes @(200) -ContentCheck "Products"
Test-Endpoint -Name "Category Filter: Electronics" -Url "$baseUrl/products?category=Electronics" -ExpectedCodes @(200)
Test-Endpoint -Name "Category Filter: Fashion" -Url "$baseUrl/products?category=Fashion" -ExpectedCodes @(200)
Test-Endpoint -Name "Search Input: 'Sony'" -Url "$baseUrl/products?q=Sony" -ExpectedCodes @(200)
Test-Endpoint -Name "Price Range Filter: 1000 to 10000" -Url "$baseUrl/products?minPrice=1000&maxPrice=10000" -ExpectedCodes @(200)
Test-Endpoint -Name "Product Details: Sony Headphones (#1)" -Url "$baseUrl/products/1" -ExpectedCodes @(200) -ContentCheck "Sony"
Test-Endpoint -Name "Product Details: ASUS ROG Laptop (#25)" -Url "$baseUrl/products/25" -ExpectedCodes @(200) -ContentCheck "ASUS"
Test-Endpoint -Name "Product Details: Sony Alpha Camera (#26)" -Url "$baseUrl/products/26" -ExpectedCodes @(200) -ContentCheck "Alpha"

# 2. AUTHENTICATION PAGES
Test-Endpoint -Name "Login Page (/auth/login)" -Url "$baseUrl/auth/login" -ExpectedCodes @(200) -ContentCheck "Login"
Test-Endpoint -Name "Login Page Alias (/login)" -Url "$baseUrl/login" -ExpectedCodes @(200) -ContentCheck "Login"
Test-Endpoint -Name "Registration Page (/auth/register)" -Url "$baseUrl/auth/register" -ExpectedCodes @(200) -ContentCheck "Register"
Test-Endpoint -Name "Registration Page Alias (/register)" -Url "$baseUrl/register" -ExpectedCodes @(200) -ContentCheck "Register"

# 3. BUYER AUTHENTICATION (REST API LOGIN)
$buyerPayload = '{"email":"buyer.john@djmart.com","password":"Password@123"}'
$buyerLoginResp = Test-Endpoint -Name "Buyer Login (REST API)" -Method "POST" -Url "$baseUrl/api/v1/auth/login" -CookieFile $cookieBuyer `
    -Body $buyerPayload -ContentType "application/json" -ExpectedCodes @(200) -ContentCheck "John Doe"

# 4. BUYER CART & ORDER OPERATIONS
$cartPageResp = Test-Endpoint -Name "Buyer Cart Page (GET /cart)" -Url "$baseUrl/cart" -CookieFile $cookieBuyer -ExpectedCodes @(200) -ContentCheck "Cart"

$buyerCsrf = ""
if (($cartPageResp -join "`n") -match 'X-CSRF-Token:\s*([^\r\n]+)') {
    $buyerCsrf = $matches[1].Trim()
}

Test-Endpoint -Name "Buyer Cart API (GET /api/v1/cart)" -Url "$baseUrl/api/v1/cart" -CookieFile $cookieBuyer -ExpectedCodes @(200)
Test-Endpoint -Name "Buyer Order History (GET /orders)" -Url "$baseUrl/orders" -CookieFile $cookieBuyer -ExpectedCodes @(200) -ContentCheck "Orders"
Test-Endpoint -Name "Buyer Order Details (GET /orders/1)" -Url "$baseUrl/orders/1" -CookieFile $cookieBuyer -ExpectedCodes @(200) -ContentCheck "Order #"

# Add item 19 (Parker pen) to cart via REST API with authenticated CSRF token
$cartAddPayload = '{"productId":19,"quantity":1}'
Test-Endpoint -Name "Add to Cart (REST API with CSRF token)" -Method "POST" -Url "$baseUrl/api/v1/cart/items" -CookieFile $cookieBuyer `
    -Body $cartAddPayload -ContentType "application/json" -ExtraHeaders "X-CSRF-Token: $buyerCsrf" -ExpectedCodes @(200, 201)

Test-Endpoint -Name "Buyer Cart after Add (GET /cart)" -Url "$baseUrl/cart" -CookieFile $cookieBuyer -ExpectedCodes @(200)

# 5. ADMIN AUTHENTICATION & ACCESS CONTROL
$adminPayload = '{"email":"admin@djmart.com","password":"Password@123"}'
Test-Endpoint -Name "Admin Login (REST API)" -Method "POST" -Url "$baseUrl/api/v1/auth/login" -CookieFile $cookieAdmin `
    -Body $adminPayload -ContentType "application/json" -ExpectedCodes @(200) -ContentCheck "ADMIN"

Test-Endpoint -Name "Admin Dashboard (GET /admin/dashboard)" -Url "$baseUrl/admin/dashboard" -CookieFile $cookieAdmin -ExpectedCodes @(200) -ContentCheck "Admin"
Test-Endpoint -Name "Admin Products (GET /admin/products)" -Url "$baseUrl/admin/products" -CookieFile $cookieAdmin -ExpectedCodes @(200)
Test-Endpoint -Name "Admin Orders (GET /admin/orders)" -Url "$baseUrl/admin/orders" -CookieFile $cookieAdmin -ExpectedCodes @(200)
Test-Endpoint -Name "Admin Users (GET /admin/users)" -Url "$baseUrl/admin/users" -CookieFile $cookieAdmin -ExpectedCodes @(200)

# 6. SELLER AUTHENTICATION & ACCESS CONTROL
$sellerPayload = '{"email":"seller.tech@djmart.com","password":"Password@123"}'
Test-Endpoint -Name "Seller Login (REST API)" -Method "POST" -Url "$baseUrl/api/v1/auth/login" -CookieFile $cookieSeller `
    -Body $sellerPayload -ContentType "application/json" -ExpectedCodes @(200) -ContentCheck "SELLER"

Test-Endpoint -Name "Seller Dashboard (GET /seller/dashboard)" -Url "$baseUrl/seller/dashboard" -CookieFile $cookieSeller -ExpectedCodes @(200) -ContentCheck "Seller"
Test-Endpoint -Name "Seller Products Listing (GET /seller/products)" -Url "$baseUrl/seller/products" -CookieFile $cookieSeller -ExpectedCodes @(200)
Test-Endpoint -Name "Seller Orders (GET /seller/orders)" -Url "$baseUrl/seller/orders" -CookieFile $cookieSeller -ExpectedCodes @(200)

Write-Host "`n=========================================================="
Write-Host " TEST SUMMARY: $passCount PASSED, $failCount FAILED"
Write-Host "=========================================================="
