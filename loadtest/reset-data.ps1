# Reset seckill load-test data.
#
# Usage:
#   1. Stop the application first.
#   2. Run: powershell -ExecutionPolicy Bypass -File .\loadtest\reset-data.ps1
#   3. Start the application (it re-warms Redis stock from MySQL).
#   4. Run UserUtils to generate new tickets.

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$redis = "F:\Cangqiong\Redis-x64-3.0.504\redis-cli.exe"

if (-not (Test-Path $mysql)) {
    Write-Host "[ERROR] mysql client not found: $mysql" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $redis)) {
    Write-Host "[ERROR] redis-cli not found: $redis" -ForegroundColor Red
    exit 1
}

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Write-Host "[ERROR] Application is still running on port 8080. Stop it first." -ForegroundColor Red
    exit 1
}

$env:MYSQL_PWD = "123456"

Write-Host "1/3 Cleaning MySQL..."
& $mysql -uroot -e "DELETE FROM seckill.t_seckill_order WHERE goods_id = 1;" 2>$null
& $mysql -uroot -e "DELETE FROM seckill.t_order WHERE goods_id = 1;" 2>$null
& $mysql -uroot -e "UPDATE seckill.t_seckill_goods SET stock_count = 3000 WHERE id = 1;" 2>$null

Write-Host "2/3 Flushing Redis DB 1..."
& $redis -n 1 FLUSHDB | Out-Null

Write-Host "3/3 Verifying..."
$stock = & $mysql -uroot -N -e "SELECT stock_count FROM seckill.t_seckill_goods WHERE id = 1;" 2>$null
$orders = & $mysql -uroot -N -e "SELECT COUNT(*) FROM seckill.t_order WHERE goods_id = 1;" 2>$null
$keys = & $redis -n 1 DBSIZE

Write-Host ""
Write-Host "MySQL stock : $stock   (expected 3000)"
Write-Host "MySQL orders: $orders   (expected 0)"
Write-Host "Redis keys  : $keys   (expected 0)"
Write-Host ""
Write-Host "Next: start the application, then run UserUtils to generate tickets."
