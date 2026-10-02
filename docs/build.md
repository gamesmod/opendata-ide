# Сборка OpenData IDE

## Требования

| Компонент | Версия | Как проверяется |
|---|---|---|
| JDK | 21 (с `javac`) | `build.*`: `JAVA_HOME`, затем `java` в PATH. Сама IDE работает на JBR 25 из дистрибутива |
| Открытая IntelliJ Platform | `ossPlatformVersion` в `gradle.properties` (2026.2.3) | загружается скриптом в `build/platform/<версия>/<ос>` или берётся из `OPENDATA_PLATFORM_HOME` |
| Gradle | wrapper 9.8.0 | `gradlew` |
| IntelliJ Platform Gradle Plugin | `intellijPlatformPluginVersion` (2.19.0) | при первой сборке |
| Kotlin | `kotlinVersion` (2.4.20) | — |
| Docker | для тестового стенда (необязательно) | `test.* --with-docker` |

Сети нужны GitHub (архив платформы), Maven Central (драйверы и зависимости тестов), Gradle Plugin Portal
и репозиторий IntelliJ Platform (test framework).

Платформа скачивается с релизов `JetBrains/intellij-community`:

| ОС | Архив |
|---|---|
| Windows | `idea-<версия>.win.zip` |
| Linux | `idea-<версия>.tar.gz` |

Если сеть ограничена, распакуйте архив сами и задайте `OPENDATA_PLATFORM_HOME` (или передайте `-PlatformHome`).

## Команды

```powershell
.\scripts\build.ps1 [-SkipTests] [-PlatformHome <dir>]   # плагин + тесты + продукт + ZIP
.\scripts\test.ps1  [-WithDocker]                        # тесты DB-слоя; с -WithDocker на стенде из docker\
.\scripts\run.ps1                                        # запуск собранной IDE
.\scripts\run.ps1 -Check                                 # проверка запуска (RESULT=OK)
.\scripts\run.ps1 -Smoke <id источника> [-Table schema.table] [-Sql "..."]
```

```bash
./scripts/build.sh                 # SKIP_TESTS=1 — без тестов
./scripts/test.sh --with-docker
./scripts/run.sh [--check | --smoke <id> [schema.table]]   # OPENDATA_SMOKE_SQL — свой запрос
```

Те же операции напрямую через Gradle:

```bash
./gradlew -PossIdePath=<платформа> :opendata:db:buildPlugin :opendata:db:test
./gradlew -PossIdePath=<платформа> -PproductOs=linux|windows assembleProduct
```

## Что делает build

1. Проверяет JDK 21+ с `javac`.
2. Находит или загружает открытую платформу и проверяет, что её build не ниже `pluginSinceBuild`.
3. Выполняет `:opendata:db:buildPlugin` и `:opendata:db:test`. Тесты на СУБД пропускаются, если не заданы `OPENDATA_*_URL`.
4. Выполняет `assembleProduct`. Это `prepareDrivers` и `tools/product/AssembleProduct.java`, результат —
   `build/product/<ос>/OpenData-IDE`.
5. Упаковывает дистрибутив в `build/distributions/OpenData-IDE-<версия>-windows-x64.zip` или `…-linux-x64.tar.gz`.

При любой ошибке сборка останавливается с понятным сообщением.

## Переменные тестов

| Переменная | Назначение |
|---|---|
| `OPENDATA_PG_URL`, `_USER`, `_PASSWORD` | PostgreSQL (по умолчанию `opendata`/`opendata`) |
| `OPENDATA_CB_URL`, `_USER`, `_PASSWORD` | Apache Cloudberry (по умолчанию учётные данные PG) |
| `OPENDATA_CH_URL`, `_USER`, `_PASSWORD` | ClickHouse (по умолчанию `default` без пароля) |
| `OPENDATA_DREMIO_URL`, `_USER`, `_PASSWORD` | Dremio (по умолчанию `dremio`/`dremio123`) |
| `OPENDATA_DRIVERS_DIR` | каталог JAR драйверов; в тестах по умолчанию `opendata/db/build/test-drivers` |

## Самопроверка собранной IDE

`run.* --smoke` запускает IDE с `-Dopendata.smoke=<id>`. Через настоящие UI-компоненты IDE раскрывает
источник в Database Explorer, выполняет запрос из консоли и проверяет строки в DataGrid, затем открывает
редактор таблицы. Отчёт пишется в `build/poc/diagnostics.txt`, успех — строка `SMOKE=OK`. Подключение
должно уже существовать в проекте по умолчанию: CI создаёт файл `~/OpenData/.idea/opendata-datasources.xml`
(см. `.github/workflows/build.yml`).
На Linux без дисплея запускайте через `xvfb-run`.

## Проверенная конфигурация

| Компонент | Версия |
|---|---|
| Платформа | IntelliJ IDEA Open Source 2026.2.3, IC-262.10968.63, JBR 25.0.4 |
| IntelliJ Platform Gradle Plugin | 2.19.0 |
| Kotlin | 2.4.20 |
| Gradle | 9.8.0 |
| JDK сборки | 21 |
| Результат | тесты 33/33 (PostgreSQL 16, режим Cloudberry, ClickHouse 26.10, Dremio OSS 26.0.5); smoke Linux-продукта OK для всех четырёх СУБД |

### Известные особенности

- **HTTP 429 от Maven Central.** Повторите сборку; при необходимости добавьте `--max-workers=1`.
- **Драйверы без сети.** IDE ищет JAR драйвера в таком порядке: `OPENDATA_DRIVERS_DIR`, `plugins/opendata-db/drivers`
  дистрибутива, `<config>/opendata/drivers`. Если загрузка не удалась, в ошибке указан точный URL и каталог,
  куда положить JAR. Можно также задать путь к JAR в поле «JAR драйвера» источника данных.
- **Прокси.** Если Java получает настройки прокси через `JAVA_TOOL_OPTIONS`, не очищайте эту переменную перед `gradlew`.
- **Значок `opendata64.exe`.** В ресурсах exe остаётся значок JetBrains: его замена требует `rcedit` и в сборку не входит.
  Окно, splash и About уже используют брендинг OpenData.

## CI и релизы

GitHub Actions (`.github/workflows/build.yml`):

| Job | Что делает |
|---|---|
| `linux` | сервисы PostgreSQL 16, ClickHouse 25.8, Dremio OSS 26.0 и их init; `scripts/build.sh` (тесты на всех СУБД, продукт, tar.gz); smoke IDE под Xvfb для PG, ClickHouse и Dremio |
| `windows` | `scripts/build.ps1 -SkipTests` (продукт и ZIP); `scripts/run.ps1 -Check` (запуск IDE на Windows) |
| `release` | после зелёных `linux` и `windows` на `main` создаёт GitHub Release `v<version>` с tar.gz, ZIP и ZIP плагина, если такого релиза ещё нет |

Чтобы выпустить релиз, поднимите `version` в `gradle.properties` и запушьте в `main`.
