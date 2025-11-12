# Script kiểm tra port 3000 và các vấn đề kết nối
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Kiểm tra Port và Kết nối" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra port 3000
Write-Host "[1/4] Kiểm tra port 3000..." -ForegroundColor Yellow
$port3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue
if ($port3000) {
    Write-Host "⚠ Port 3000 đang được sử dụng bởi:" -ForegroundColor Yellow
    $port3000 | Format-Table -AutoSize
    Write-Host "   Process ID: $($port3000.OwningProcess)" -ForegroundColor Yellow
    $process = Get-Process -Id $port3000.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "   Process Name: $($process.ProcessName)" -ForegroundColor Yellow
        Write-Host "   Process Path: $($process.Path)" -ForegroundColor Yellow
    }
} else {
    Write-Host "✓ Port 3000 đang trống" -ForegroundColor Green
}
Write-Host ""

# Kiểm tra port 8000 (backend)
Write-Host "[2/4] Kiểm tra port 8000 (backend)..." -ForegroundColor Yellow
$port8000 = Get-NetTCPConnection -LocalPort 8000 -ErrorAction SilentlyContinue
if ($port8000) {
    Write-Host "✓ Backend đang chạy trên port 8000" -ForegroundColor Green
} else {
    Write-Host "⚠ Backend KHÔNG chạy trên port 8000!" -ForegroundColor Red
    Write-Host "   Cần chạy: cd ..\flight-booking-api && php artisan serve" -ForegroundColor Yellow
}
Write-Host ""

# Kiểm tra firewall
Write-Host "[3/4] Kiểm tra Windows Firewall..." -ForegroundColor Yellow
$firewall = Get-NetFirewallProfile | Where-Object { $_.Enabled -eq $true }
if ($firewall) {
    Write-Host "⚠ Firewall đang bật. Có thể chặn kết nối." -ForegroundColor Yellow
    Write-Host "   Nếu cần, tạm thời tắt firewall hoặc thêm exception cho Node.js" -ForegroundColor Yellow
} else {
    Write-Host "✓ Firewall không chặn" -ForegroundColor Green
}
Write-Host ""

# Kiểm tra kết nối localhost
Write-Host "[4/4] Kiểm tra kết nối localhost:3000..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Có thể kết nối đến localhost:3000" -ForegroundColor Green
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Không thể kết nối đến localhost:3000" -ForegroundColor Red
    Write-Host "   Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Gợi ý khắc phục:" -ForegroundColor Cyan
Write-Host "1. Đảm bảo backend đang chạy: php artisan serve" -ForegroundColor White
Write-Host "2. Thử truy cập: http://127.0.0.1:3000 thay vì localhost:3000" -ForegroundColor White
Write-Host "3. Kiểm tra firewall và antivirus" -ForegroundColor White
Write-Host "4. Thử đổi port: set PORT=3001 && npm start" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

