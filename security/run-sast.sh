#!/usr/bin/env bash
set -e

mkdir -p security/reports/sast

pysemgrep --metrics off --config security/semgrep-rules.yml \
  --exclude security/sast-vulnerable-examples.java \
  --error --json --output security/reports/sast/application.json src

pysemgrep --metrics off --config security/semgrep-rules.yml \
  --json --output security/reports/sast/intentional-trigger.json \
  security/sast-vulnerable-examples.java || true

jq '{
  findings: [.results[] | {
    rule: .check_id,
    file: .path,
    line: .start.line,
    severity: .extra.severity,
    message: .extra.message
  }]
}' security/reports/sast/intentional-trigger.json \
  > security/reports/sast/intentional-trigger.json.tmp
mv security/reports/sast/intentional-trigger.json.tmp security/reports/sast/intentional-trigger.json

jq . security/reports/sast/application.json > security/reports/sast/application.json.tmp
mv security/reports/sast/application.json.tmp security/reports/sast/application.json
