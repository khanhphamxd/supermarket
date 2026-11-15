# Batch benchmark for nested inventory and cart reads.
# Run from the project root with:
#   .\benchmark\run-nested-benchmarks.ps1
# Existing valid output pairs are reused; incomplete/invalid pairs stop the batch.

$ErrorActionPreference = 'Stop'

$runner = Join-Path $PSScriptRoot 'runner.js'
$resultsDir = Join-Path $PSScriptRoot 'results'
$pauseSeconds = 5

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    throw "Could not find 'k6' on PATH. Open the terminal where k6 works and rerun this script."
}
if (-not (Test-Path $runner)) {
    throw "Could not find runner.js at: $runner"
}
New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null

function Get-SummaryAssessment {
    param(
        [Parameter(Mandatory = $true)][string]$SummaryPath,
        [Parameter(Mandatory = $true)][int]$ExitCode
    )

    $issues = @()
    $checkFailures = $null
    $httpFailedRate = $null
    $grpcFailedRate = $null

    if ($ExitCode -ne 0) {
        $issues += "k6 exit code $ExitCode"
    }
    if (-not (Test-Path $SummaryPath)) {
        $issues += 'summary JSON missing'
        return [pscustomobject]@{
            Status = 'REVIEW: ' + ($issues -join '; ')
            CheckFailures = $checkFailures
            HttpFailedRate = $httpFailedRate
            GrpcFailedRate = $grpcFailedRate
        }
    }

    try {
        $data = Get-Content -LiteralPath $SummaryPath -Raw | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        $issues += 'summary JSON could not be parsed'
        return [pscustomobject]@{
            Status = 'REVIEW: ' + ($issues -join '; ')
            CheckFailures = $checkFailures
            HttpFailedRate = $httpFailedRate
            GrpcFailedRate = $grpcFailedRate
        }
    }

    $metrics = $data.metrics
    if ($null -eq $metrics.operation_duration) {
        $issues += 'operation_duration metric missing'
    }
    if ($null -eq $metrics.checks -or $null -eq $metrics.checks.values.fails) {
        $issues += 'checks metric missing'
    }
    else {
        $checkFailures = [int]$metrics.checks.values.fails
        if ($checkFailures -gt 0) {
            $issues += "$checkFailures failed check(s)"
        }
    }

    if ($null -ne $metrics.http_req_failed -and $null -ne $metrics.http_req_failed.values.rate) {
        $httpFailedRate = [double]$metrics.http_req_failed.values.rate
        if ($httpFailedRate -gt 0) {
            $issues += "HTTP failure rate is $httpFailedRate"
        }
    }
    if ($null -ne $metrics.grpc_req_failed -and $null -ne $metrics.grpc_req_failed.values.rate) {
        $grpcFailedRate = [double]$metrics.grpc_req_failed.values.rate
        if ($grpcFailedRate -gt 0) {
            $issues += "gRPC failure rate is $grpcFailedRate"
        }
    }

    $status = 'PASS'
    if ($issues.Count -gt 0) {
        $status = 'REVIEW: ' + ($issues -join '; ')
    }
    return [pscustomobject]@{
        Status = $status
        CheckFailures = $checkFailures
        HttpFailedRate = $httpFailedRate
        GrpcFailedRate = $grpcFailedRate
    }
}

# Rotate the order each round so one protocol/entity combination does not
# always run first or last. Five rounds x six combinations = 30 measurements.
$tests = @(
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'rest' },
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'graphql' },
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'grpc' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'rest' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'graphql' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'grpc' }
)

$runRecords = New-Object 'System.Collections.Generic.List[object]'

for ($rep = 1; $rep -le 5; $rep++) {
    Write-Host "`n========== REPETITION ROUND $rep/5 ==========" -ForegroundColor Cyan
    $offset = ($rep - 1) % $tests.Count

$ErrorActionPreference = 'Stop'

$runner = Join-Path $PSScriptRoot 'runner.js'
$resultsDir = Join-Path $PSScriptRoot 'results'
$pauseSeconds = 5

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    throw "k6 was not found on PATH."
}

if (-not (Test-Path -LiteralPath $runner)) {
    throw ("Could not find runner.js: {0}" -f $runner)
}

New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null

function Get-RunStatus {
    param(
        [string]$SummaryPath,
        [int]$ExitCode
    )

    $issues = @()

    if ($ExitCode -ne 0) {
        $issues += ("k6 exit code: {0}" -f $ExitCode)
    }

    if (-not (Test-Path -LiteralPath $SummaryPath)) {
        $issues += 'summary JSON missing'
        return ("REVIEW: " + ($issues -join '; '))
    }

    try {
        $data = Get-Content -LiteralPath $SummaryPath -Raw |
            ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        return 'REVIEW: summary JSON could not be parsed'
    }

    if ($null -eq $data.metrics) {
        return 'REVIEW: metrics object missing'
    }

    if ($null -eq $data.metrics.operation_duration) {
        $issues += 'operation_duration missing'
    }

    $checkProperty = $data.metrics.PSObject.Properties['checks']

    if ($null -eq $checkProperty) {
        $issues += 'checks metric missing'
    }
    else {
        $fails = $checkProperty.Value.values.fails

        if ($null -eq $fails) {
            $issues += 'check failure count missing'
        }
        elseif ([double]$fails -gt 0) {
            $issues += ("{0} failed check(s)" -f $fails)
        }
    }

    foreach ($metricName in @('http_req_failed', 'grpc_req_failed')) {
        $metricProperty = $data.metrics.PSObject.Properties[$metricName]

        if ($null -ne $metricProperty) {
            $rate = $metricProperty.Value.values.rate

            if ($null -ne $rate -and [double]$rate -gt 0) {
                $issues += ("{0} rate: {1}" -f $metricName, $rate)
            }
        }
    }

    if ($issues.Count -eq 0) {
        return 'PASS'
    }

    return ("REVIEW: " + ($issues -join '; '))
}

$tests = @(
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'rest' },
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'graphql' },
    [pscustomobject]@{ Entity = 'inventory'; Protocol = 'grpc' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'rest' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'graphql' },
    [pscustomobject]@{ Entity = 'cart'; Protocol = 'grpc' }
)

$records = New-Object 'System.Collections.Generic.List[object]'

for ($rep = 1; $rep -le 5; $rep++) {
    Write-Host ("`n===== ROUND {0}/5 =====" -f $rep) -ForegroundColor Cyan

    # Rotate the order between rounds to reduce ordering bias.
    $offset = ($rep - 1) % $tests.Count

    for ($i = 0; $i -lt $tests.Count; $i++) {
        $case = $tests[($i + $offset) % $tests.Count]
        $entity = $case.Entity
        $protocol = $case.Protocol

        $baseName = "{0}-{1}-rep-{2}" -f $entity, $protocol, $rep
        $logPath = Join-Path $resultsDir ("{0}.log" -f $baseName)
        $summaryPath = Join-Path $resultsDir ("{0}-summary.json" -f $baseName)

        $logExists = Test-Path -LiteralPath $logPath
        $summaryExists = Test-Path -LiteralPath $summaryPath

        if ($logExists -and $summaryExists) {
            if ((Get-Item -LiteralPath $logPath).Length -eq 0 -or
                (Get-Item -LiteralPath $summaryPath).Length -eq 0) {
                throw ("Existing output for {0} is empty. Inspect or rename its files first." -f $baseName)
            }

            $status = Get-RunStatus -SummaryPath $summaryPath -ExitCode 0

            if ($status -ne 'PASS') {
                throw ("Existing result {0} is not a clean run: {1}. Inspect or rename its files first." -f $baseName, $status)
            }

            Write-Host ("SKIP {0}: existing result validated." -f $baseName) -ForegroundColor Yellow

            $records.Add([pscustomobject]@{
                Entity = $entity
                Protocol = $protocol
                Repetition = $rep
                Action = 'Reused existing result'
                Status = 'PASS'
                ExitCode = 0
                LogFile = [IO.Path]::GetFileName($logPath)
                SummaryFile = [IO.Path]::GetFileName($summaryPath)
            })

            continue
        }

        if ($logExists -or $summaryExists) {
            throw ("Only one output file exists for {0}. Inspect or rename the partial files before continuing." -f $baseName)
        }

        Write-Host ("`nSTART {0} / {1} / repetition {2}/5" -f $entity, $protocol, $rep) -ForegroundColor Green

        $k6Args = @(
            'run',
            '--summary-export', $summaryPath,
            '--summary-trend-stats=avg,min,med,max,p(75),p(90),p(95)',
            '-e', 'MODE=load',
            '-e', ("ENTITY={0}" -f $entity),
            '-e', ("PROTOCOL={0}" -f $protocol),
            '-e', 'VUS=1',
            '-e', 'DURATION=60s',
            $runner
        )

        & k6 @k6Args 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE

        $status = Get-RunStatus -SummaryPath $summaryPath -ExitCode $exitCode

        if ($status -eq 'PASS') {
            Write-Host ("RESULT {0}: PASS" -f $baseName) -ForegroundColor Green
        }
        else {
            Write-Host ("RESULT {0}: {1}" -f $baseName, $status) -ForegroundColor Red
        }

        $records.Add([pscustomobject]@{
            Entity = $entity
            Protocol = $protocol
            Repetition = $rep
            Action = 'Executed'
            Status = $status
            ExitCode = $exitCode
            LogFile = [IO.Path]::GetFileName($logPath)
            SummaryFile = [IO.Path]::GetFileName($summaryPath)
        })

        if (($rep -lt 5) -or ($i -lt ($tests.Count - 1))) {
            Start-Sleep -Seconds $pauseSeconds
        }
    }
}

$reportPath = Join-Path $resultsDir 'nested-benchmark-batch-status.csv'

$records |
    Sort-Object Entity, Protocol, Repetition |
    Export-Csv -LiteralPath $reportPath -NoTypeInformation -Encoding UTF8

Write-Host "`n===== BATCH STATUS =====" -ForegroundColor Cyan

$records |
    Sort-Object Entity, Protocol, Repetition |
    Format-Table Entity, Protocol, Repetition, Action, Status -AutoSize

Write-Host ("`nStatus report: {0}" -f $reportPath)

$reviewRuns = @($records | Where-Object { $_.Status -ne 'PASS' })

if ($records.Count -ne 30) {
    Write-Warning ("Expected 30 results; accounted for {0}." -f $records.Count)
}

if ($reviewRuns.Count -gt 0) {
    Write-Warning ("{0} result(s) need review. Keep their files for investigation." -f $reviewRuns.Count)
}
else {
    Write-Host 'All 30 planned results have valid saved summaries.' -ForegroundColor Green
}

