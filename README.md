# OpenData IDE

[![build](https://github.com/gamesmod/opendata-ide/actions/workflows/build.yml/badge.svg)](https://github.com/gamesmod/opendata-ide/actions/workflows/build.yml)

Лёгкая настольная IDE для работы с СУБД, собранная как **самостоятельный продукт** на открытой
**IntelliJ Platform** (IntelliJ IDEA Open Source 2026.2.3, Apache 2.0). Поддерживаются
**PostgreSQL**, **Apache Cloudberry**, **ClickHouse** и **Dremio**.

В дистрибутиве только ядро платформы, открытый модуль `intellij.grid` (DataGrid) и собственный DB-слой
`opendata-db`. Плагинов Java, Kotlin, Git, Markdown и других нет, советы при запуске, onboarding и проверка
обновлений отключены.
Закрытый плагин JetBrains *Database Tools and SQL* в продукт не входит и не требуется.

## Возможности

| Функция | Реализация |
|---|---|
| Источники данных: Add / Edit / Duplicate / Delete, Test Connection, драйвер, host/port/db/user, JDBC URL, свойства | `opendata-db`, пароли в `PasswordSafe` |
| Драйверы JDBC | PostgreSQL входит в комплект; ClickHouse и Arrow Flight SQL (Dremio) загружаются из Maven Central с проверкой SHA-256 или берутся из локального каталога |
| Database Explorer | схемы, таблицы, представления, функции, последовательности, типы; колонки, ключи, индексы, триггеры, ограничения |
| SQL Console | подсветка, completion по метаданным (таблицы, колонки, алиасы), Ctrl+Enter, скрипт, отмена, ошибки с позицией, Auto-commit / Commit / Rollback, история запросов |
| Результаты | открытый JetBrains **DataGrid**: выделение, копирование, сортировка, фильтр, ширина колонок, NULL, даты, JSON, LOB |
| Редактор таблицы | двойной клик в Explorer: WHERE / ORDER BY, постраничная загрузка, редактирование, вставка и удаление строк, Submit / Revert |
| DDL | таблицы, представления, индексы, последовательности, функции, схемы |

Различия по СУБД (транзакции, редактирование, DDL) описаны в [`docs/databases.md`](docs/databases.md).

## Установка

Готовые сборки лежат в [Releases](https://github.com/gamesmod/opendata-ide/releases):

- Windows x64: `OpenData-IDE-<версия>-windows-x64.zip` → распаковать → `bin\opendata64.exe`;
- Linux x64: `OpenData-IDE-<версия>-linux-x64.tar.gz` → распаковать → `bin/opendata`.

Java ставить не нужно: JetBrains Runtime 25 входит в дистрибутив.

## Сборка из исходников

Нужен JDK 21 (с `javac`). Открытая платформа загрузится автоматически с GitHub JetBrains/intellij-community.

```powershell
.\scripts\build.ps1                 # Windows: плагин, тесты, продукт и ZIP
.\scripts\test.ps1 -WithDocker      # тесты на PostgreSQL, ClickHouse и Dremio из docker\docker-compose.yml
.\scripts\run.ps1                   # запуск собранной IDE
.\scripts\run.ps1 -Check            # проверка запуска без UI-действий
```

На Linux и macOS используются `scripts/*.sh` с теми же шагами. Подробности: [`docs/build.md`](docs/build.md).

## Структура

```text
opendata/db/            DB-слой и UI: источники данных, драйверы, сессии, метаданные, SQL, DataGrid, Explorer
tools/product/          AssembleProduct.java: сборка продукта из открытой платформы (брендинг, состав плагинов)
tools/research/         инструмент этапа 0 (анализ установленного DataGrip, только для исследования)
scripts/                build / run / test (.ps1 для Windows, .sh для Linux и macOS)
docker/                 тестовый стенд: PostgreSQL 16, ClickHouse 25.8, Dremio OSS 26
docs/                   архитектура, сборка, СУБД, приёмка, roadmap, совместимость
```

## Лицензии

Лицензия собственного кода OpenData пока не выбрана: файла `LICENSE` в репозитории нет. Платформа IntelliJ и модуль `intellij.grid` взяты из
открытой сборки JetBrains/intellij-community (Apache 2.0), см. `NOTICE-OpenData.txt` в дистрибутиве.
Драйверы распространяются под своими лицензиями: PostgreSQL JDBC — BSD-2, ClickHouse JDBC и Apache Arrow — Apache 2.0.

Версия 0.1.0 была плагином к DataGrip поверх закрытого *Database Tools and SQL*. Начиная с 0.2.0 IDE
самостоятельная и закрытые компоненты JetBrains не использует. Причины изложены в [`docs/architecture.md`](docs/architecture.md).
