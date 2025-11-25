# Script kiem tra port 3000 va cac van de ket noi
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Kiem tra Port va Ket noi" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiem tra port 3000
Write-Host "[1/4] Kiem tra port 3000..." -ForegroundColor Yellow
$port3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue
if ($port3000) {
    Write-Host "! Port 3000 dang duoc su dung boi:" -ForegroundColor Yellow
    $port3000 | Format-Table -AutoSize
    $processId = $port3000[0].OwningProcess
    Write-Host "   Process ID: $processId" -ForegroundColor Yellow
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "   Process Name: $($process.ProcessName)" -ForegroundColor Yellow
        if ($process.Path) {
            Write-Host "   Process Path: $($process.Path)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "OK Port 3000 dang trong" -ForegroundColor Green
}
Write-Host ""

# Kiem tra port 8000 (backend)
Write-Host "[2/4] Kiem tra port 8000 (backend)..." -ForegroundColor Yellow
$port8000 = Get-NetTCPConnection -LocalPort 8000 -ErrorAction SilentlyContinue
if ($port8000) {
    Write-Host "OK Backend dang chay tren port 8000" -ForegroundColor Green
} else {
    Write-Host "! Backend KHONG chay tren port 8000!" -ForegroundColor Red
    Write-Host "   Can chay: cd ..\flight-booking-api" -ForegroundColor Yellow
    Write-Host "   Sau do: php artisan serve" -ForegroundColor Yellow
}
Write-Host ""

# Kiem tra firewall
Write-Host "[3/4] Kiem tra Windows Firewall..." -ForegroundColor Yellow
$firewall = Get-NetFirewallProfile | Where-Object { $_.Enabled -eq $true }
if ($firewall) {
    Write-Host "! Firewall dang bat. Co the chan ket noi." -ForegroundColor Yellow
    Write-Host "   Neu can, tam thoi tat firewall hoac them exception cho Node.js" -ForegroundColor Yellow
} else {
    Write-Host "OK Firewall khong chan" -ForegroundColor Green
}
Write-Host ""

# Kiem tra ket noi localhost
Write-Host "[4/4] Kiem tra ket noi localhost:3000..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
    Write-Host "OK Co the ket noi den localhost:3000" -ForegroundColor Green
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "X Khong the ket noi den localhost:3000" -ForegroundColor Red
    Write-Host "   Loi: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Goi y khac phuc:" -ForegroundColor Cyan
Write-Host "1. Dam bao backend dang chay: php artisan serve" -ForegroundColor White
Write-Host "2. Thu truy cap: http://127.0.0.1:3000 thay vi localhost:3000" -ForegroundColor White
Write-Host "3. Kiem tra firewall va antivirus" -ForegroundColor White
Write-Host "4. Thu doi port: `$env:PORT=3001; npm start" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
