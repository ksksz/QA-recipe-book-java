#!/usr/bin/env bash
set -e

echo "Запускается проверка зависимостей"
mvn org.owasp:dependency-check-maven:check -Ddependency-check.fail-build-on-cvss=11

jq '{
  scannedDependencies: (.dependencies | length),
  vulnerableDependencies: ([
    .dependencies[]
    | select((.vulnerabilities // []) | length > 0)
    | {
        dependency: .fileName,
        vulnerabilityCount: (.vulnerabilities | length),
        maximumCvss: (
          [.vulnerabilities[] | (.cvssv4.baseScore // .cvssv3.baseScore // .cvssv2.score // 0)]
          | max
        )
      }
  ] | sort_by(-.maximumCvss) | .[:4])
}' security/reports/sca/dependency-check-report.json \
  > security/reports/sca/sca-summary.json

echo "SCA завершен. Краткий отчет: security/reports/sca/sca-summary.json"
