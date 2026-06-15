# тестирование безопасности

Три вида анализа:
1. SAST, Semgrep, исходный код 
2. DAST, Nikto, запущенное приложение
3. SCA, OWASP Dependency-Check, Maven-зависимости


```bash
./security/run-sast.sh
./security/run-dast.sh
./security/run-sca.sh
```

## 1. SAST: Semgrep

Конфигурация в `security/semgrep-rules.yml`

Настроены четыре проверки:
1. `java-runtime-exec` — запуск команд операционной системы
2. `java-sql-string-concatenation` — SQL-запрос из конкатенации строк
3. `java-user-controlled-upload-extension` — сохранение файла под именем пользователя
4. `java-hardcoded-password` — пароль, заданный в исходном коде

```bash
./security/run-sast.sh
```

Два JSON-отчета:

- `security/reports/sast/application.json` — рабочий код, найдено 0 проблем
- `security/reports/sast/intentional-trigger.json` — краткий отчет для 4 намеренных проблем

Файл `security/sast-vulnerable-examples.java` содержит четыре намеренные уязвимости.

Показан пример исправления проблемы загрузки файлов в `FileStorageService`:
- разрешены только JPEG, PNG и WebP;
- имя файла создается сервером через `UUID`;
- исходное имя пользователя не используется.


## 2. DAST: Nikto

Nikto проверяет запущенное приложение реальными HTTP-запросами.


```bash
./security/run-dast.sh
```

Настроены четыре проверки:

- `headers` — проверка защитных HTTP-заголовков;
- `httpoptions` — проверка разрешенных HTTP-методов;
- `cookies` — проверка безопасности cookies;
- `springboot` — поиск открытых служебных точек Spring Boot.

Технический плагин `report_json` сохраняет результаты проверок в JSON-отчет

Результат: несколько десятков запросов, 0 ошибок. Отчет:

```text
security/reports/dast/nikto-report.json
```

Nikto сообщил об отсутствующих защитных заголовках. Для исправления добавлен
`SecurityHeadersFilter`, который устанавливает:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Content-Security-Policy`
- `Referrer-Policy`
- `Permissions-Policy`

Сообщение об отсутствии HSTS на локальном HTTP ожидаемо: HSTS применяется только вместе с HTTPS.
Разрешены только безопасные методы `GET`, `HEAD` и `OPTIONS`. Находка `/trace.axd` является ложным
срабатыванием: такой служебной точки в приложении нет.

## 3. SCA: OWASP Dependency-Check

Dependency-Check сравнивает версии Maven-зависимостей с базой известных CVE.

Настройка находится в `pom.xml`:

- полный технический отчет — JSON;
- порог ошибки проверки — CVSS 7 и выше.

```bash
./security/run-sca.sh
```

Два отчета:
```text
security/reports/sca/sca-summary.json
security/reports/sca/dependency-check-report.json
```

`sca-summary.json` — основной краткий отчет. Он содержит только названия уязвимых зависимостей,
количество найденных CVE и максимальный CVSS. В отчет попадает максимум четыре зависимости.

Из полного отчета выбраны четыре проверяемых результата:

1. `spring-boot-3.3.5` — найдено 6 CVE, максимальный CVSS 9.8, критическая уязвимость.
   Высокий риск для приложения, потому что это основной фреймворк.
2. `tomcat-embed-core-10.1.31` — найдено 35 CVE, максимальный CVSS 9.8, критическая уязвимость.
   Высокий риск для приложения, потому что зависимость обрабатывает HTTP-запросы.
3. `spring-core-6.1.14` — найдено 5 CVE, максимальный CVSS 6.5, средняя уязвимость.
   Нужно проверить применимость и обновить зависимость вместе со Spring Boot.
4. `log4j-api-2.23.1` — найдено 2 CVE, максимальный CVSS 6.9, средняя уязвимость.
   Риск ниже, но зависимость нужно обновить.

Для устранения проблем нужно обновить Spring Boot, выполнить `mvn test` и повторить SCA. Результаты
также проверяются вручную, потому что не каждая найденная CVE обязательно применима к приложению.

Локальный скрипт использует порог CVSS 11, чтобы всегда создать отчет. При обычном запуске Maven
используется порог CVSS 7, поэтому высокая или критическая уязвимость завершает проверку с ошибкой.
