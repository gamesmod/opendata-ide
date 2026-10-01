# Архитектура OpenData IDE

## Принцип

Порядок принятия решений (ТЗ, раздел 2):

1. готовый API или модуль Database Tools and SQL / DataGrip;
2. IntelliJ Platform API;
3. `intellij.grid` (Data Editor and Viewer);
4. стандартный JDBC;
5. собственная реализация, только если подходящего API нет.

## Слои

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ IntelliJ Platform (из локальной установки IDE/DataGrip — одна build-линия)│
│   Editor · PSI · VFS · Project model · Actions · Tool Windows · Settings  │
├──────────────────────────────────────────────────────────────────────────┤
│ Database Tools and SQL  (bundledPlugin "com.intellij.database")           │
│   Data Sources · Driver Manager · Database Explorer · SQL Console         │
│   Introspection/metadata · SQL PSI/dialects · Completion · Navigation     │
│   Query execution · Transactions · DDL · Query history                    │
│   intellij.grid.* (content modules) — DataGrid, editing, table editor     │
├──────────────────────────────────────────────────────────────────────────┤
│ OpenData                                                                  │
│   integration  — диагностика интеграции, будущие адаптеры                 │
│   product      — branding, product code, launcher (этап 5)                │
│   drivers / extensions-api / extensions-ui / plugins — по мере появления  │
│                  реально отсутствующих функций (ТЗ, раздел 28)            │
└──────────────────────────────────────────────────────────────────────────┘
```

## Как подключается JetBrains DB stack

| Способ (ТЗ, раздел 8) | Статус | Реализация |
|---|---|---|
| 8.1 Bundled plugin dependency | **основной** | `bundledPlugin("com.intellij.database")` в `opendata/integration/build.gradle.kts` и `<depends>com.intellij.database</depends>` в `plugin.xml`. Транзитивные content modules (SQL, grid) резолвит IntelliJ Platform Gradle Plugin |
| 8.2 Локальные JAR | основной источник платформы | `local(JETBRAINS_IDE_HOME / DATAGRIP_HOME / -PlocalIdePath)`: и платформа, и плагин берутся из одной установки, поэтому build-линии совпадают |
| 8.3 Исходные модули intellij-community | не требуется для этапов 0–4 | нужны для анализа исходников `grid/` (`--community`) и для варианта B этапа 5 |
| 8.4 Собственная реализация | не используется | — |

## Собственный код (этапы 0–4)

| Компонент | Назначение | Почему не переиспользование |
|---|---|---|
| `OpenDataDiagnostics` | проверка: плагин загружен, SQL/PostgreSQL зарегистрированы, есть окно Database | интеграционная проверка самого OpenData, у JetBrains её нет |
| `OpenDataStartupActivity` | диагностика при открытии проекта и отчёт в файл для POC | то же |
| `OpenDataDiagnosticsAction` | Tools → OpenData: Integration Diagnostics | то же |
| `tools/research/Research.java` | автоматизация этапа 0 | инструмент разработки, в продукт не входит |

Диагностика обращается только к стабильному публичному API платформы (`PluginManagerCore`, `Language`,
`ToolWindowManager`) и не трогает внутренние классы `com.intellij.database.*`. Внутренние API подключаются
только после того, как их нашёл research (`docs/jetbrains-db-analysis.md`, раздел 5), и каждое использование
документируется по шаблону ТЗ, раздел 6 (класс, JAR, build, назначение, точка интеграции, риск).

## Асинхронность

Собственный код не выполняет DB-операции. Диагностика читает список tool windows на EDT (быстрая операция
в памяти), всё остальное работает в `ProjectActivity` (корутина, не EDT). DB-операции Database Tools
выполняет на собственных фоновых задачах.

## Реестр используемых API Database Tools

Используются в сценарных тестах (`DatabaseToolsPostgresScenarioTest`). Все найдены research'ем в DataGrip 2026.2.6
(build 262.10968.148). Ни один из классов не помечен `@ApiStatus.Internal`, но это не публичный SDK:
JetBrains не гарантирует их совместимость между версиями.

| Класс / метод | Модуль/JAR (`plugins/DatabaseTools/lib/modules/`) | Назначение | Точка интеграции | Риск |
|---|---|---|---|---|
| `DatabaseDriverManager.getInstance().getDrivers()`, `updateDriver()` | `intellij.database.core.impl.jar` | штатный драйвер PostgreSQL (id `postgresql`) | Driver Manager | средний |
| `DatabaseDriver.setAdditionalClasspathElements()` + `SimpleClasspathElementFactory.createElements()` | `intellij.database.core.impl.jar` / платформа | файл JDBC-драйвера без загрузки из сети | Driver Manager | средний |
| `LocalDataSource.fromDriver(driver, url, temporary)`, `setName/setUsername/setPasswordStorage` | `intellij.database.core.impl.jar` | создание PostgreSQL Data Source | Data Sources | средний |
| `LocalDataSourceManager.getInstance(project).addDataSource/removeDataSource/getDataSources` | `intellij.database.impl.jar` | регистрация Data Source в проекте (EDT) | Data Sources | средний |
| `DatabaseCredentials.getInstance().storePassword(config, OneTimeString)` | `intellij.database.core.impl.jar` | пароль через штатное хранилище | PasswordSafe | низкий |
| `DatabaseConnectionManager.getInstance().build(project, ds).setAskPassword(false).create()` (suspend) → `GuardedRef<DatabaseConnection>` | `intellij.database.connectivity.jar` | Test Connection и сессия | Connectivity | средний; `createBlocking()` вне корутины/прогресса падает |
| `DatabaseConnection.getRemoteConnection()` → `RemoteConnection` / `RemoteStatement` | `intellij.database.connectivity.jar`, `intellij.database.jdbcConsole.jar` | SELECT, UPDATE, commit/rollback через удалённый JDBC-процесс Database Tools | Query execution | средний |
| `DataSourceUtil.performManualSyncTask(LoaderContext.selectGeneralTask(project, ds))` → `AsyncTask.toFuture()` | `intellij.database.connectivity.jar`, `intellij.database.core.impl.jar` | schema introspection | Introspection | средний |
| `DasUtil.getTables(ds)`, `DasUtil.getColumns(table)` | `intellij.database.jar` | чтение модели метаданных | Metadata | низкий |

Наблюдения при интеграции:

- JDBC-драйвер Database Tools работает во **внешнем процессе** (`RemoteProcessSupport`), рабочим каталогом служит `basePath`
  проекта. Если каталога нет, подключение падает с ошибкой `WorkingDirectoryNotFoundException`.
- DB-операции нельзя выполнять в EDT; регистрацию Data Source в `LocalDataSourceManager` нужно делать в EDT.
- В тестах исполнитель синхронизации (`DataSourceSyncManager`) по умолчанию штатный: `NEW_CONNECTION_EXECUTOR`.
