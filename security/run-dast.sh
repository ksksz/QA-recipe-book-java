#!/usr/bin/env bash
set -e

TARGET="${1:-http://127.0.0.1:3000}"
mkdir -p security/reports/dast

if ! curl -fsS "$TARGET/api/meta" >/dev/null 2>&1; then
  echo "Приложение не запущено. Запускаю его для проверки."
  mvn -q -DskipTests package
  JAVA_HOME="${JAVA_HOME:-$(mvn -v | sed -n 's/.*runtime: //p')}"
  SPRING_DATASOURCE_URL="jdbc:h2:mem:recipebook;DB_CLOSE_DELAY=-1;MODE=PostgreSQL" \
    "$JAVA_HOME/bin/java" -jar target/recipe-book-java-1.0.0.jar > security/reports/dast/application.log 2>&1 &
  PID=$!
  trap 'kill "$PID" 2>/dev/null || true' EXIT
  for i in {1..30}; do
    curl -fsS "$TARGET/api/meta" >/dev/null 2>&1 && break
    sleep 2
  done
  curl -fsS "$TARGET/api/meta" >/dev/null
fi

rm -f security/reports/dast/nikto-report.json
nikto -host "$TARGET" -nointeractive -nocheck \
  -Plugins "headers;httpoptions;cookies;springboot;report_json" \
  -Format json -output security/reports/dast/nikto-report

echo "DAST завершен. Отчет: security/reports/dast/nikto-report.json"
