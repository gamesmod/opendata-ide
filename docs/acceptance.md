# Приёмка и Definition of Done MVP

Проверка идёт на трёх уровнях:

- **тесты** `:opendata:db:test` на реальных СУБД: `scripts/test.* --with-docker`, в CI — job `linux`;
- **smoke** собранной IDE: `scripts/run.* --smoke`. Explorer, консоль, DataGrid и редактор таблицы проверяются через настоящие UI-компоненты;
- **ручная проверка** пунктов, которые требуют действий пользователя в UI (клавиатура, мышь).

Результаты 2026-10-02 (Linux, платформа IC-262.10968.63):

- тесты: 33/33 на PostgreSQL 16, в режиме Cloudberry, на ClickHouse 26.10 и Dremio OSS 26.0.5, включая загрузку драйверов из Maven Central (в CI);
- smoke Linux-продукта: `SMOKE=OK` для PostgreSQL, Cloudberry (режим PG), ClickHouse и Dremio (в CI — PG, ClickHouse, Dremio с загрузкой драйверов);
- Windows: ZIP собирается, IDE запускается в CI (`run.ps1 -Check` → `RESULT=OK`); smoke с СУБД на Windows не выполнялся.

Стенд: `docker compose -f docker/docker-compose.yml up -d --wait`, затем `docker/dremio/init.sh`.

## Definition of Done MVP (ТЗ, раздел 40)

| # | Критерий | Тест / smoke | Вручную |
|---|---|---|---|
| 1 | Запустить OpenData IDE | smoke, `run.* --check` (`RESULT=OK`) | запуск `bin/opendata` или `opendata64.exe` |
| 2 | Создать источник PostgreSQL | `testDataSourceStateRoundTrip` | Database → «+» → тип, хост, база, пользователь, пароль |
| 3 | Проверить соединение | косвенно: подключение во всех интеграционных тестах | кнопка Test Connection в диалоге |
| 4 | Подключиться | все интеграционные тесты, smoke | — |
| 5 | Просмотреть схемы | `testIntrospectionTree`, smoke (`explorer.containers`) | — |
| 6 | Таблицы, представления, функции | `testIntrospectionTree` | раскрыть `public` |
| 7 | Структура таблицы | `testIntrospectionTree` (колонки, PK, FK, индексы, триггеры, ограничения) | раскрыть `orders` |
| 8 | SQL Console | smoke (`console.file=console.sql`) | контекстное меню источника → консоль |
| 9 | Подсветка и completion | `testSyntaxHighlightingInEditor`, `testLexerTokens`, `testCompletionMetadata`, `testAliasResolution` | `SELECT * FROM pub…`, `SELECT u. FROM users u` |
| 10 | Выполнить SELECT | `testSelectIntoDataGrid`, smoke (`console.rows=100`) | Ctrl+Enter |
| 11 | ResultSet в JetBrains DataGrid | `testSelectIntoDataGrid`, smoke | — |
| 12 | Сортировка и фильтрация | `testPagingAndCount` (ORDER BY / WHERE в редакторе таблицы) | клик по заголовку, фильтр в DataGrid |
| 13 | Копирование данных | — (штатная функция grid) | Ctrl+C из DataGrid |
| 14 | Открыть таблицу напрямую | smoke (`table.rows`) | двойной клик в Explorer |
| 15 | Изменить значение в DataGrid | `testTableEditorSubmitRevertInsertDelete`; ClickHouse: `testTableEditor` | правка ячейки |
| 16 | Submit | то же | Ctrl+Enter или кнопка Submit |
| 17 | Revert | то же | кнопка Revert |
| 18 | Commit и Rollback | `testManualTransactionCommitRollback` | отключить Auto-commit → Commit / Rollback |
| 19 | SQL error с позицией | `testErrorWithPositionAndScriptResults`, `testClickHouseErrorPosition` | `SELECT * FROM no_such_table;` подсвечивает позицию |
| 20 | Отменить долгий запрос | `testCancelLongQuery` | `SELECT pg_sleep(60)` → Ctrl+F2 |

## По СУБД

| Проверка | PostgreSQL | Cloudberry | ClickHouse | Dremio |
|---|---|---|---|---|
| Introspection | ✅ | ✅ (режим PG) | ✅ | ✅ |
| Запрос → DataGrid | ✅ | ✅ | ✅ | ✅ |
| Ошибка с позицией | ✅ | ✅ | ✅ | сообщение без позиции |
| Редактирование таблицы | ✅ | ✅ | ✅ (мутации `ALTER TABLE … UPDATE`) | только чтение (`testTableEditorIsReadOnlyWithPaging`) |
| Транзакции | ✅ | ✅ | нет (СУБД) | нет (СУБД) |
| DDL | ✅ `testDdl` | ✅ | ✅ `testIntrospectionAndDdl` | ✅ представления (`testQueryAndViewDdl`) |
| smoke IDE | ✅ | ✅ | ✅ | ✅ |

## Версия 0.4.0: JDBC URL, драйверы в поставке, Greenplum, bulk-загрузка, значок

| Проверка | Тест | Где |
|---|---|---|
| Вставленный JDBC URL заполняет тип, хост, порт, базу; URL с параметрами сохраняется; поля пересобирают URL | `DataSourceDialogUrlTest`, `JdbcUrlsTest` | локально |
| URL другого типа СУБД — понятная ошибка; адрес в дереве — из URL | `testMismatchedUrlGivesClearError`, `testMatchesAndAddress` | локально, UI |
| Все драйверы в дистрибутиве, без загрузки | `testAllDriversAvailableWithoutDownload`; smoke Dremio без каталога загрузок | локально |
| gpfdist: параллельные сегменты получают каждую запись один раз, блоки — целые записи, опоздавший сегмент — пустой ответ, ошибки 400/404/500/E, запись с SEQ/DONE | `GpfdistServerTest` (5) | локально и CI |
| COPY: заголовок, TRUNCATE, откат при ошибке, TEXT с `\N` | `BulkLoadTest.testPostgresCopy` | PostgreSQL |
| COPY с `LOG ERRORS SEGMENT REJECT LIMIT`, загрузка через gpfdist (сегменты кластера читают файл с раннера), откат без допуска ошибок, выгрузка через writable external table, DDL с `DISTRIBUTED BY`, функции | `BulkLoadTest.testGreenplum6/7`, `testCloudberry` | CI: Greenplum 6, Greenplum 7, Cloudberry 2.1 |
| Значок и сведения о версии в `opendata64.exe` | шаг `Check exe icon and version info` | CI Windows |

## Версия 0.3.0: проекты, драйверы, импорт и экспорт

| Проверка | Тест | Проверено в UI (Linux, Xvfb) |
|---|---|---|
| Подключения хранятся в проекте, id ищется среди открытых проектов | `testDataSourcesBelongToProject` | 4 подключения в `~/OpenData/.idea/opendata-datasources.xml` |
| Перенос подключений 0.2.0 в проект | — | при первом открытии: файл уровня IDE удалён, уведомление показано |
| Новый проект | — | Welcome → New Project → проект с пустым окном Database |
| Консоли в проекте | `testConsoleFilesLiveInProject` | `.idea/opendata/consoles/<id>/console.sql` |
| Ctrl+Enter выполняет запрос (а не разбивает строку) | — | ✅ после `ConsoleActionPromoter` |
| Менеджер драйверов: переопределения, версия, свои JAR | `testDriverSettingsOverrideDefaults` | Settings → Tools → OpenData: драйверы; «Проверить» для ClickHouse |
| Загрузка драйверов с проверкой контрольной суммы | `testDownloadAllDrivers` (CI) | — |
| Форматы CSV/TSV/JSON/SQL INSERT/Markdown, разбор CSV | `testWritersAndCsvRoundTrip`, `testConvertByColumnType` | — |
| Экспорт таблицы и импорт в новую таблицу, атомарность | `testPostgresExportImport` | `users` → CSV → новая таблица `users_copy` (2 строки), дерево обновилось |
| ClickHouse: экспорт JSON, импорт в существующую и новую таблицу | `testClickHouseExportImport` | — |
| Dremio: экспорт | `testDremioExport` | — |
| Экспорт результата консоли | — | Results → «Экспорт результата…» → 100 строк в CSV |
| Экспорт/импорт подключений (XML без паролей) | `testDataSourcesBelongToProject` | — |

Cloudberry проверен на PostgreSQL в режиме совместимости (`testCloudberryModeOnPostgresProtocol`). На настоящем
кластере Apache Cloudberry стоит отдельно проверить DDL с `DISTRIBUTED BY` (см. `docs/databases.md`).
