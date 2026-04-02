param(
    [Parameter(Mandatory=$true)]
    [string]$BaseUrl,
    [string]$AdminEmail = "admin@zorvyn.local",
    [string]$AdminPassword = "admin123",
    [string]$AnalystEmail = "analyst@zorvyn.local",
    [string]$AnalystPassword = "analyst123",
    [string]$ViewerEmail = "viewer@zorvyn.local",
    [string]$ViewerPassword = "viewer123"
)

$creds = @{
    admin = "$AdminEmail`:$AdminPassword"
    analyst = "$AnalystEmail`:$AnalystPassword"
    viewer = "$ViewerEmail`:$ViewerPassword"
}

function Get-Headers([string]$userKey) {
    $pair = $creds[$userKey]
    $bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
    $token = [Convert]::ToBase64String($bytes)
    return @{ Authorization = "Basic $token"; 'Content-Type' = 'application/json' }
}

function Invoke-Test([string]$name, [string]$userKey, [string]$method, [string]$path, $body, [int]$expected) {
    $url = "$BaseUrl$path"
    $headers = Get-Headers $userKey
    try {
        if ($null -ne $body) {
            $json = $body | ConvertTo-Json -Depth 6
            $resp = Invoke-WebRequest -Uri $url -Headers $headers -Method $method -Body $json -UseBasicParsing -TimeoutSec 30
        } else {
            $resp = Invoke-WebRequest -Uri $url -Headers $headers -Method $method -UseBasicParsing -TimeoutSec 30
        }
        $status = [int]$resp.StatusCode
        $content = $resp.Content
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $content = $reader.ReadToEnd()
            } catch {
                $content = $_.Exception.Message
            }
        } else {
            $status = -1
            $content = $_.Exception.Message
        }
    }

    return [PSCustomObject]@{
        test = $name
        expected = $expected
        actual = $status
        pass = ($status -eq $expected)
        body = $content
    }
}

$results = @()
$results += Invoke-Test 'auth me admin' 'admin' 'GET' '/api/auth/me' $null 200
$results += Invoke-Test 'auth me analyst' 'analyst' 'GET' '/api/auth/me' $null 200
$results += Invoke-Test 'users list viewer denied' 'viewer' 'GET' '/api/users' $null 403
$results += Invoke-Test 'records list analyst' 'analyst' 'GET' '/api/records' $null 200
$results += Invoke-Test 'dashboard summary analyst' 'analyst' 'GET' '/api/dashboard/summary' $null 200
$results += Invoke-Test 'analyst insights analyst' 'analyst' 'GET' '/api/analyst/insights' $null 200
$results += Invoke-Test 'analyst insights viewer denied' 'viewer' 'GET' '/api/analyst/insights' $null 403

try {
    $tokenReq = @{ email = $AnalystEmail; password = $AnalystPassword } | ConvertTo-Json
    $tokenResp = Invoke-WebRequest -Uri "$BaseUrl/api/auth/token" -Method POST -ContentType 'application/json' -Body $tokenReq -UseBasicParsing -TimeoutSec 30
    $tokenStatus = [int]$tokenResp.StatusCode
    $tokenBody = $tokenResp.Content

    if ($tokenStatus -eq 200) {
        $tokenObj = $tokenBody | ConvertFrom-Json
        $bearerHeaders = @{ Authorization = "Bearer $($tokenObj.token)"; 'Content-Type' = 'application/json' }
        $bearerMe = Invoke-WebRequest -Uri "$BaseUrl/api/auth/me" -Headers $bearerHeaders -Method GET -UseBasicParsing -TimeoutSec 30
        $results += [PSCustomObject]@{ test = 'jwt issue token'; expected = 200; actual = $tokenStatus; pass = $true; body = $tokenBody }
        $results += [PSCustomObject]@{ test = 'jwt bearer auth me'; expected = 200; actual = [int]$bearerMe.StatusCode; pass = ([int]$bearerMe.StatusCode -eq 200); body = $bearerMe.Content }
    } else {
        $results += [PSCustomObject]@{ test = 'jwt issue token'; expected = 200; actual = $tokenStatus; pass = $false; body = $tokenBody }
    }
} catch {
    $results += [PSCustomObject]@{ test = 'jwt token flow'; expected = 200; actual = -1; pass = $false; body = $_.Exception.Message }
}

$summary = [PSCustomObject]@{
    total = $results.Count
    passed = ($results | Where-Object { $_.pass }).Count
    failed = ($results | Where-Object { -not $_.pass }).Count
}

$summary | ConvertTo-Json -Depth 4
'---'
$results | Select-Object test, expected, actual, pass | Format-Table -AutoSize | Out-String
