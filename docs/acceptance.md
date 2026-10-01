# Приёмка этапов 1–5 и Definition of Done MVP

Автоматическая часть проверяется тестами (`scripts\test.ps1 -WithPostgres`) и отчётом `run.ps1 -Poc`.

**Проверено автоматически на DataGrip 2026.2.6 (262.10968.148) + PostgreSQL 16:** 16 из 16 тестов, включая сценарий этапа 1 через штатный API Database Tools.
Песочница IDE (`runIde`) при первом запуске показывает JetBrains User Agreement, который принимает пользователь. Поэтому пункты 1.1 и 1.3 через UI пока не подтверждены; через тестовую платформу они подтверждены.
Всё, что требует UI, проверяется вручную по этому чек-листу. Автоматизация UI запланирована в roadmap (R-7).

Перед приёмкой: запустите `docker compose -f docker\docker-compose.yml up -d --wait`, затем `.\scripts\run.ps1 -Poc`.
В песочнице откроется проект `build/poc/project` с файлом `poc.sql`.

## Этап 1 — POC

| # | Проверка | Как | Авто |
|---|---|---|---|
| 1.1 | IDE запускается | `run.ps1 -Poc` открыл окно IDE | — |
| 1.2 | Database Tools загружен | `diagnostics.txt`: `[OK] Database Tools and SQL` | ✅ `testDatabaseToolsLoaded` |
| 1.3 | Database Tool Window присутствует | `diagnostics.txt`: `[OK] Database Tool Window` | ✅ |
| 1.4 | PostgreSQL Data Source создаётся | Database → + → Data Source → PostgreSQL (docs/postgresql.md) | ✅ `testDataSourceIsRegistered` (штатный API) |
| 1.5 | Test Connection проходит | кнопка Test Connection | ✅ `testConnectAndSelectVersion` (DatabaseConnectionManager) |
| 1.6 | schemas и tables загружаются | в дереве `opendata → schemas → public → tables` видны `opendata_test`, `users`, `orders`, `big_table` | ✅ `testIntrospectionLoadsTables` (штатная introspection) |
| 1.7 | структура таблицы | у `orders`: columns, PK, FK → users, index `orders_user_idx`, trigger `orders_touch`, CHECK | — |
| 1.8 | views/functions/sequences/types | `user_order_totals`, `touch_created_at`, `opendata_seq`, `order_status` | — |

## Этап 2 — SQL Console

| # | Проверка | Авто |
|---|---|---|
| 2.1 | Console открывается (контекстное меню data source → New → Query Console) | — |
| 2.2 | выбран диалект PostgreSQL | ✅ `testPostgresDialectParsesValidSql` |
| 2.3 | подсветка синтаксиса работает | — |
| 2.4 | `SELECT version();` + Ctrl+Enter возвращает результат | частично ✅: запрос через соединение Database Tools; Ctrl+Enter в UI — вручную |
| 2.5 | ошибка отображается и позиция подсвечивается (`SELECT * FROM no_such_table;`) | — (JDBC: `sqlErrorHasStateAndPosition`) |
| 2.6 | синтаксическая ошибка подсвечивается в редакторе | ✅ `testPostgresDialectReportsSyntaxError` |
| 2.7 | completion: `FROM pub` предлагает схему и объекты, `u.` — колонки `users` | — |
| 2.8 | Ctrl+Click по `public.users` переходит к объекту | — |
| 2.9 | `SELECT pg_sleep(60)` отменяется, консоль после этого работает | — (JDBC: `longQueryCanBeCancelled`) |
| 2.10 | видны duration и row count | — |

## Этап 3 — DataGrid

Запрос `generate_series(1,100)` из `poc.sql`.

| # | Проверка | Авто |
|---|---|---|
| 3.1 | результат открывается в штатном DataGrid (100 строк, колонки `id`, `value`) | — (JDBC: `generateSeriesReturns100Rows`) |
| 3.2 | scrolling и selection | — |
| 3.3 | copy (Ctrl+C) вставляется в текстовый редактор как TSV | — |
| 3.4 | column resize | — |
| 3.5 | sorting по клику на заголовок | — |
| 3.6 | filtering (строка фильтра `WHERE`) | — |
| 3.7 | NULL, числа, даты, JSON и BYTEA отображаются корректно (последний запрос `poc.sql`) | — |
| 3.8 | `big_table`: постраничная загрузка и fetch more | — |

## Этап 4 — Editing

| # | Проверка | Авто |
|---|---|---|
| 4.1 | двойной клик по `opendata_test` в Explorer открывает табличный редактор | — |
| 4.2 | изменение `name` в ячейке → Submit (Ctrl+Enter) → значение сохранено (проверить `SELECT` из другой консоли) | — (JDBC: `updateCommitAndRollback`) |
| 4.3 | Revert отменяет несохранённое изменение | — |
| 4.4 | insert, delete и duplicate row; `id` генерируется (BIGSERIAL), `created_at` берёт значение по умолчанию | — |
| 4.5 | Set NULL и Set DEFAULT | — |
| 4.6 | ручной режим транзакций (Tx: Manual): Submit → Rollback откатывает, Commit фиксирует | частично ✅ `testCommitAndRollbackThroughDatabaseTools`; Tx-режим в UI — вручную |
| 4.7 | DDL: правый клик по таблице → SQL Scripts → Generate DDL (table, view, index, sequence, function, schema) | — |
| 4.8 | Query History содержит выполненные запросы | — |

## Этап 5 — OpenData product

См. `opendata/product/README.md`. Критерии берутся из ТЗ, раздел 35: отдельный launcher, работающие
Explorer, Console, DataGrid и editing.

## Definition of Done MVP (ТЗ, раздел 40)

| # | Критерий | Пункт чек-листа |
|---|---|---|
| 1 | Запустить OpenData IDE | 1.1 (POC), этап 5 (продукт) |
| 2–4 | Создать PostgreSQL Data Source, проверить, подключиться | 1.4, 1.5 |
| 5–7 | schemas, tables/views/functions, структура таблицы | 1.6–1.8 |
| 8–9 | SQL Console, highlighting и completion | 2.1–2.3, 2.7 |
| 10–11 | SELECT → DataGrid | 2.4, 3.1 |
| 12–13 | Сортировка/фильтрация, копирование | 3.3, 3.5, 3.6 |
| 14 | Открыть таблицу напрямую | 4.1 |
| 15–17 | Изменить, Submit, Revert | 4.2, 4.3 |
| 18 | Commit/Rollback | 4.6 |
| 19 | SQL error с позицией | 2.5 |
| 20 | Отменить долгий запрос | 2.9 |
