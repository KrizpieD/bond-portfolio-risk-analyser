# --- CONFIGURATION ---
$BaseUrl = "http://localhost:8080"
$IngestEndpoint = "$BaseUrl/bond/create-portfolio"

# --- 1. PREPARE SAMPLE DATA ---
$PortfolioData = @{
    portfolio_name = "PowerShell Test Portfolio"
    bonds = @(
        @{
            isin = "US912828Z946"
            maturity_date = "2028-05-15T00:00:00Z"
            coupon_rate = 4.25
            face_value = 1000.00
            market_price = 995.50
            coupon_dates = @("2024-05-15T00:00:00Z", "2024-11-15T00:00:00Z")
        }
    )
}

# Convert the object to a JSON string
$JsonPayload = $PortfolioData | ConvertTo-Json -Depth 5

Write-Host "🚀 Starting Portfolio Ingestion..." -ForegroundColor Cyan

try {
    # --- 2. CREATE PORTFOLIO ---
    # Captures the returned Portfolio ID
    $PortfolioId = Invoke-RestMethod -Uri $IngestEndpoint -Method Post -Body $JsonPayload -ContentType "application/json"
    Write-Host "✅ Ingested Successfully. Portfolio ID: $PortfolioId" -ForegroundColor Green

    # --- 3. FETCH METRICS ---
    Write-Host "Fetching Metrics..." -ForegroundColor Cyan

    # Get Portfolio Duration
    $DurationUrl = "$BaseUrl/metrics/get-portfolio-weighted-avg-duration/$PortfolioId"
    $PortfolioDuration = Invoke-RestMethod -Uri $DurationUrl -Method Get

    # Get Portfolio details to find the specific Bond ID
    $GetPortfolioUrl = "$BaseUrl/bond/get-portfolio/$PortfolioId"
    $PortfolioDetails = Invoke-RestMethod -Uri $GetPortfolioUrl -Method Get
    $FirstBondId = $PortfolioDetails.bonds[0].id

    # Get YTM for the specific bond
    $YtmUrl = "$BaseUrl/metrics/get-ytm-by-bond-id/$FirstBondId"
    $YTM = (Invoke-RestMethod -Uri $YtmUrl -Method Get) * 100

    # --- 4. VALIDATE RESULTS ---
    Write-Host "`n--- VALIDATION RESULTS ---" -ForegroundColor Yellow

    # Validate Duration (Expected 0-5 range)
    if ($PortfolioDuration -gt 0 -and $PortfolioDuration -lt 5.0) {
        Write-Host "✅ DURATION: $PortfolioDuration (Within expected range)" -ForegroundColor Green
    } else {
        Write-Host "❌ DURATION: $PortfolioDuration (Out of expected range!)" -ForegroundColor Red
    }

    # Validate YTM (Discount bond logic: Yield must be > Coupon Rate of 0.0425)
    if (($YTM) -gt 0.0425) {
        Write-Host "✅ YTM: $YTM (Correct: Yield > Coupon for discount bond)" -ForegroundColor Green
    } else {
        Write-Host "❌ YTM: $YTM (Incorrect: Yield should be > 0.0425)" -ForegroundColor Red
    }

} catch {
    Write-Host "❌ Script Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 Test Complete." -ForegroundColor Cyan