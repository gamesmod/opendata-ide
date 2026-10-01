# OpenData IDE

[![build](https://github.com/gamesmod/opendata-ide/actions/workflows/build.yml/badge.svg)](https://github.com/gamesmod/opendata-ide/actions/workflows/build.yml)

Локальная настольная IDE для реляционных СУБД на базе **JetBrains IntelliJ Platform** с максимальным
переиспользованием **Database Tools and SQL** (DataGrip) и `intellij.grid`. Полное ТЗ лежит в [`CLAUDE.md`](CLAUDE.md).

Главный принцип: собственную DB-инфраструктуру не пишем. Data Sources, Database Explorer, SQL Console,
SQL PSI и диалекты, completion, query execution, DataGrid, редактирование, DDL и транзакции берутся
из штатного плагина `com.intellij.database` той же установки JetBrains.

## Состояние

| Этап | Что сделано в репозитории | Что нужно сделать на машине разработчика |
|---|---|---|
| 0 — Research | **выполнено на DataGrip 2026.2.6**: отчёты в `docs/`. `tools/research/Research.java` сам извлекает из установки IDE/DataGrip версии, `plugin.xml`, content modules, EP, сервисы, tool windows, классы `com.intellij.database.*`/`sql`/`grid` с пометками `@ApiStatus.Internal`, а также сигнатуры javap; затем генерирует `docs/*.md` | запустить `scripts\research.ps1` и разобрать отчёт |
| 1 — POC | **сборка и 16/16 тестов проходят**: Data Source, Test Connection, introspection и commit/rollback через штатный API Database Tools | принять JetBrains User Agreement при первом `scripts\run.ps1 -Poc` и пройти UI-пункты [`docs/acceptance.md`](docs/acceptance.md) |
| 2–4 — Console / DataGrid / Editing | собственного кода нет (всё штатное), есть тестовая БД и контрольные запросы | ручная приёмка по [`docs/acceptance.md`](docs/acceptance.md) |
| 5 — продукт OpenData IDE | план и варианты в [`opendata/product/README.md`](opendata/product/README.md) | только после успешного POC (ТЗ, раздел 41) |

## Быстрый старт (Windows 11)

Требования: JDK 21 (с `javac`; задайте `JAVA_HOME`), установленные DataGrip **или** IntelliJ IDEA с
плагином Database Tools and SQL, а также Docker Desktop для тестовой PostgreSQL (необязательно).

```powershell
# необязательно, если IDE стоит в стандартном каталоге (Toolbox / Program Files):
$env:JETBRAINS_IDE_HOME = "C:\Program Files\JetBrains\DataGrip 2025.2"

.\scripts\research.ps1               # этап 0 → docs\jetbrains-db-analysis.md и др.
.\scripts\build.ps1                  # проверки + сборка плагина + тесты
docker compose -f docker\docker-compose.yml up -d
.\scripts\test.ps1 -WithPostgres     # smoke + JDBC integration tests
.\scripts\run.ps1 -Poc               # песочница IDE с Database Tools + отчёт диагностики
```

Linux и macOS: те же шаги через `scripts/*.sh`. Подробности в [`docs/build.md`](docs/build.md).

## Структура

```text
opendata/
├── integration/        плагин интеграции (диагностика, точки расширения OpenData)
└── product/            этап 5: конфигурация продукта OpenData IDE (план)
tools/research/         инструмент этапа 0 (Java single-file, JDK 17+)
scripts/                build / run / test / research (.ps1 — Windows, .sh — Linux/macOS)
docker/                 тестовая PostgreSQL 16 + init-скрипт (opendata_test и объекты для Explorer)
docs/                   архитектура, сборка, отчёты research, матрица совместимости, roadmap
```

## Лицензирование

Database Tools and SQL — закрытый плагин JetBrains. Его использование в песочнице IntelliJ на базе
установленного у вас DataGrip или IDEA (этапы 0–4) не выходит за рамки обычной разработки плагина.
Этап 5 (отдельный продукт со своим брендингом, куда входит этот плагин) нужно до начала работ сверить
с текстом лицензии JetBrains: см. [`docs/roadmap.md`](docs/roadmap.md), риск R1.
