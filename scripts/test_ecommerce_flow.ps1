# scripts/test_ecommerce_flow.ps1
$baseUrl = "http://localhost:8081"
$cookieFile = "cookies_test.txt"

if (Test-Path $cookieFile) { Remove-Item $cookieFile }

Write-Host "=== 1. BUYER LOGIN ==="
$loginOutput = curl.exe -s -i -c $cookieFile -X POST "$baseUrl/auth/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "email=buyer.john@djmart.com&password=Password@123"

$loginHeader = $loginOutput -join "`n"
if ($loginHeader -match "302" -or $loginHeader -match "Location:") {
    Write-Host "Login SUCCESS (Redirect received)"
} else {
    Write-Host "Login response: $loginHeader"
}

Write-Host "`n=== 2. GET CART AND CSRF TOKEN ==="
$cartHeadersAndBody = curl.exe -s -i -b $cookieFile "$baseUrl/api/v1/cart"
$csrfToken = ""
foreach ($line in ($cartHeadersAndBody -split "`r?`n")) {
    if ($line -match "^X-CSRF-Token:\s*(.+)$") {
        $csrfToken = $matches[1].Trim()
        break
    }
}
Write-Host "Obtained CSRF Token: $csrfToken"

Write-Host "`n=== 3. ADD PRODUCT TO CART (Product ID: 19 - Parker Pen) ==="
$addOutput = curl.exe -s -b $cookieFile -c $cookieFile -X POST "$baseUrl/api/v1/cart/items" `
  -H "Content-Type: application/json" `
  -H "X-CSRF-Token: $csrfToken" `
  -d '{"productId": 19, "quantity": 1}'
Write-Host "Add Response: $addOutput"

Write-Host "`n=== 4. GET CART AFTER ADDING ==="
$cartAfter = curl.exe -s -b $cookieFile "$baseUrl/api/v1/cart"
Write-Host "Cart: $cartAfter"

Write-Host "`n=== 5. CHECKOUT / PLACE ORDER ==="
$checkoutOutput = curl.exe -s -b $cookieFile -c $cookieFile -X POST "$baseUrl/api/v1/checkout" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -H "X-CSRF-Token: $csrfToken" `
  -d "shippingAddress=42+MG+Road,+Bengaluru,+560001&paymentMethod=COD"
Write-Host "Checkout Response: $checkoutOutput"

Write-Host "`n=== 6. ADMIN LOGIN AND ANALYTICS ACCESS ==="
$adminCookie = "admin_cookie.txt"
if (Test-Path $adminCookie) { Remove-Item $adminCookie }

$adminLogin = curl.exe -s -i -c $adminCookie -X POST "$baseUrl/auth/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "email=admin@djmart.com&password=Password@123"

Write-Host "Admin Login HTTP Status:"
$adminLogin | Select-String "HTTP"

$analyticsPage = curl.exe -s -o NUL -w "%{http_code}" -b $adminCookie "$baseUrl/admin/analytics"
Write-Host "Admin Analytics HTTP Code: $analyticsPage"

$dashboardPage = curl.exe -s -o NUL -w "%{http_code}" -b $adminCookie "$baseUrl/admin/dashboard"
Write-Host "Admin Dashboard HTTP Code: $dashboardPage"
