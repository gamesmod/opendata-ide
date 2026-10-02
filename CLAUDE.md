# ТЗ для Claude Code: OpenData IDE

## 1. Цель проекта

Разработать локальную настольную IDE для работы с реляционными СУБД на базе **JetBrains IntelliJ Platform** с максимальным переиспользованием существующей инфраструктуры JetBrains, включая доступные компоненты **DataGrip / Database Tools and SQL** и `intellij.grid`.

Проект предназначен для некоммерческого локального/внутреннего использования. Допускается использовать доступные закрытые API, модули и бинарные компоненты JetBrains при наличии законного доступа к ним и в рамках применимой лицензии.

Главный принцип:

> **Не переписывать то, что уже реализовано JetBrains. Сначала найти и переиспользовать готовый API/модуль, и только затем писать собственную реализацию.**

Рабочее название:

```text
OpenData IDE
```

Основной сценарий MVP:

```text
Запустить IDE
→ создать/выбрать Data Source
→ подключиться к PostgreSQL
→ увидеть Database Explorer
→ открыть SQL Console
→ выполнить SELECT
→ получить результат в JetBrains DataGrid
→ отредактировать данные
→ применить изменения
```

---

## 2. Приоритет переиспользования

Порядок принятия архитектурных решений:

```text
1. Готовый API / модуль DataGrip или Database Tools and SQL
2. IntelliJ Platform API
3. intellij.grid / Data Editor and Viewer
4. Стандартный JDBC
5. Собственная реализация — только если подходящего API нет
```

В первую очередь исследовать и по возможности использовать:

```text
com.intellij.database.*
Database Tools and SQL
Data Source API
Database Explorer
DB consoles
SQL PSI
SQL dialects
SQL completion
query execution
database metadata
driver management
DataGrid
data editing
DDL support
navigation
transaction management
```

Не проектировать собственный аналог перечисленных подсистем, пока не проверено, можно ли использовать существующую реализацию JetBrains.

---

## 3. Целевая архитектура

```text
IntelliJ Platform
        │
        ├── Editor / PSI / VFS / Project model
        ├── Actions / Tool Windows / Settings
        │
        ├── DataGrip / Database Tools
        │       ├── Data Sources
        │       ├── Database Explorer
        │       ├── SQL Console
        │       ├── Metadata / Introspection
        │       ├── SQL PSI / Dialects
        │       ├── Completion
        │       ├── Query execution
        │       └── Transactions
        │
        ├── intellij.grid
        │       └── Data Editor and Viewer
        │
        └── OpenData
                ├── product configuration
                ├── adapters/integration
                ├── custom drivers
                ├── custom actions
                └── дополнительный функционал
```

Собственный DB-layer создавать только там, где готовый API DataGrip/Database Tools отсутствует или технически не подходит.

---

## 4. СУБД первого этапа

MVP обязательно:

```text
PostgreSQL
```

Архитектура должна позволять далее использовать:

```text
PostgreSQL
MySQL
MariaDB
Microsoft SQL Server
Oracle
ClickHouse
Dremio
SQLite
```

Если поддержка конкретной СУБД уже присутствует в JetBrains Database Tools, использовать её вместо самостоятельной реализации dialect/metadata/driver logic.

---

## 5. Этап 0 — обязательное исследование JetBrains DB stack

Claude должен начать **не с написания собственной DB IDE**, а с анализа реально доступных исходников, JAR, bundled plugins и API.

Исследовать:

```text
JetBrains/intellij-community
локальную установленную IntelliJ IDEA / DataGrip, если доступна
Database Tools and SQL plugin
bundled plugins
intellij.grid
product configuration
module descriptors
plugin.xml
Gradle/build scripts
JAR dependencies
```

Зафиксировать:

```text
IntelliJ commit/tag
IntelliJ build number
DataGrip build number
Database Tools build number
JDK version
Kotlin version
Gradle version
совместимость build numbers
```

Определить:

```text
какие DB-модули доступны исходниками
какие доступны только бинарно
какие API public
какие API internal
какие extension points доступны
какие services можно переиспользовать напрямую
какие зависимости нужны compile/runtime
```

Результат сохранить:

```text
docs/jetbrains-db-analysis.md
```

---

## 6. Анализ DataGrip / Database Tools API

Найти реальные классы, сервисы и extension points для следующих функций:

```text
Data Sources
Driver Manager
Database Connection
Metadata / Introspection
Database Explorer
SQL Console
SQL execution
SQL PSI
SQL dialects
Completion
Navigation
DataGrid
Data modification
DDL
Transactions
Query history
```

Особенно исследовать namespace:

```text
com.intellij.database.*
com.intellij.database.dataSource.*
com.intellij.database.console.*
com.intellij.database.datagrid.*
com.intellij.database.psi.*
com.intellij.database.dialects.*
```

Эти имена являются направлениями поиска. **Не считать класс существующим, пока он не найден в текущей версии.**

Для каждого используемого internal API документировать:

```text
полное имя класса
модуль/JAR
build/version
назначение
точку интеграции
риск изменения между версиями
```

---

## 7. Анализ `intellij.grid`

Исследовать:

```text
grid/
```

Особенно:

```text
grid/api
grid/core-impl
grid/impl
grid/plugin
```

Найти реальные API для:

```text
создания DataGrid
модели строк и колонок
передачи данных
редактирования
sorting
filtering
copy/paste
paging/fetch more
renderers/editors
data hooks
```

Искать классы, связанные с:

```text
DataGrid
GridModel
GridColumn
GridRow
GridDataHookUp
GridDataSupport
DataGridUtil
```

Названия предварительные. Использовать только реально найденные классы.

Результат:

```text
docs/grid-analysis.md
```

---

## 8. Способ подключения Database Tools / DataGrip

Claude должен проверить способы подключения в следующем порядке.

### 8.1. Bundled plugin dependency

Попытаться использовать установленный `Database Tools and SQL` как plugin dependency.

Определить:

```text
plugin ID
plugin path
required modules
compatible build
classloader behavior
services
extension points
```

### 8.2. Локальные JAR dependencies

Если plugin dependency недостаточно, определить минимальный набор JAR из локальной установки JetBrains IDE/DataGrip.

Сборка должна уметь находить их через локальный параметр, например:

```text
JETBRAINS_IDE_HOME
DATAGRIP_HOME
```

Пути конкретного пользователя не хардкодить.

### 8.3. Исходные модули

Если нужная часть доступна в `intellij-community`, подключать модуль из исходников.

### 8.4. Собственная реализация

Использовать только для реально отсутствующих или непригодных компонентов.

---

## 9. Итоговая матрица повторного использования

До основной разработки Claude должен сформировать таблицу:

| Функция | JetBrains компонент | Модуль/JAR | Способ подключения | Собственный код |
|---|---|---|---|---|
| Data Sources | TBD | TBD | TBD | да/нет |
| Driver Manager | TBD | TBD | TBD | да/нет |
| Database Explorer | TBD | TBD | TBD | да/нет |
| SQL Console | TBD | TBD | TBD | да/нет |
| SQL PSI | TBD | TBD | TBD | да/нет |
| Dialects | TBD | TBD | TBD | да/нет |
| Completion | TBD | TBD | TBD | да/нет |
| Query execution | TBD | TBD | TBD | да/нет |
| Metadata | TBD | TBD | TBD | да/нет |
| DataGrid | TBD | TBD | TBD | да/нет |
| Editing | TBD | TBD | TBD | да/нет |
| DDL | TBD | TBD | TBD | да/нет |
| Transactions | TBD | TBD | TBD | да/нет |

---

## 10. Product configuration

Создать собственную конфигурацию продукта:

```text
OpenData IDE
```

Настроить:

```text
product name
product code
launcher
icon
splash
About
default plugins
bundled plugins
VM options
system/config paths
```

Если полноценную IDE можно получить через product configuration + набор существующих JetBrains plugins, предпочесть этот путь форку и дублированию кода.

---

## 11. Data Source Manager

Приоритетно использовать штатную систему JetBrains Data Sources.

Требуется:

```text
Add Data Source
Edit
Duplicate
Delete
Connect
Disconnect
Test Connection
Driver selection
Host
Port
Database
User
Password
SSL
JDBC URL
Advanced properties
```

Для PostgreSQL по возможности использовать существующий JetBrains PostgreSQL dialect/driver support.

Пароли хранить через штатный IntelliJ `PasswordSafe` или используемый Database Tools механизм.

Собственную модель `DataSourceConfig` создавать только при необходимости адаптера к внешним интеграциям.

---

## 12. Database Explorer

Приоритетно переиспользовать существующее Database Tool Window.

Ожидаемая структура:

```text
PostgreSQL Local
└── database
    └── Schemas
        └── public
            ├── Tables
            ├── Views
            ├── Functions
            ├── Sequences
            └── Types
```

Для таблиц должны быть доступны:

```text
Columns
Primary Key
Foreign Keys
Indexes
Triggers
Constraints
```

Если штатный Database Explorer можно включить как plugin/module — собственное дерево не писать.

---

## 13. Driver management

Переиспользовать JetBrains Driver Manager, если он доступен.

Поддержать:

```text
driver selection
driver class
JDBC URL templates
driver properties
driver version
driver download/manual path
```

Для MVP обязательно обеспечить PostgreSQL JDBC Driver.

---

## 14. SQL Console

Приоритетно использовать штатную DataGrip/Database Tools Console.

Функции MVP:

```text
SQL syntax highlighting
Execute current statement
Execute selected text
Execute script
Cancel query
Query duration
Row count
Error output
Commit
Rollback
Transaction mode
```

Основная горячая клавиша:

```text
Ctrl+Enter
```

Если готовую Console переиспользовать невозможно, использовать IntelliJ Editor + DataGrip SQL PSI/dialect API.

---

## 15. SQL PSI и Dialects

Переиспользовать SQL PSI/dialect infrastructure DataGrip.

Не писать собственный SQL parser, если соответствующая инфраструктура доступна.

Для PostgreSQL проверить:

```text
parsing
highlighting
statement boundaries
syntax errors
references
tables
columns
aliases
functions
CTE
subqueries
```

---

## 16. SQL Completion

Переиспользовать штатный JetBrains completion.

Пример:

```sql
SELECT * FROM pub
```

должен предлагать доступные объекты схемы.

Пример:

```sql
SELECT u.
FROM users u
```

должен предлагать колонки `users`.

Если штатный completion работает — отдельный metadata completion не писать.

---

## 17. Query execution

Приоритетно использовать штатный query execution pipeline Database Tools.

Поддержать:

```text
SELECT
INSERT
UPDATE
DELETE
DDL
multiple result sets
warnings
errors
cancel
transactions
```

Fallback на JDBC допускается только при невозможности использовать штатный executor.

Для fallback можно определить интерфейс:

```kotlin
interface QueryExecutor {
    suspend fun execute(sql: String): QueryExecutionResult
}
```

---

## 18. DataGrid

Результаты запросов должны отображаться через JetBrains DataGrid.

Требования:

```text
selection
copy/paste
column resize
sorting
filtering
NULL rendering
numbers
dates/timestamps
JSON
LOB
paging/fetch more
```

Контрольный запрос:

```sql
SELECT
    generate_series AS id,
    md5(generate_series::text) AS value
FROM generate_series(1,100);
```

Результат должен открыться в штатном DataGrid.

---

## 19. Редактирование данных

Переиспользовать JetBrains data editor / grid editing infrastructure.

Должны работать:

```text
edit cell
insert row
delete row
duplicate row
submit
revert
NULL/default
generated keys
transaction state
```

Контрольная таблица:

```sql
CREATE TABLE opendata_test (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    created_at TIMESTAMP DEFAULT now()
);
```

После изменения `name` через DataGrid и выполнения Submit значение должно сохраняться в PostgreSQL.

---

## 20. Table Data Editor

Двойной клик по таблице в Database Explorer должен открывать штатный табличный редактор данных.

Поддержать:

```text
filter
sorting
limit
refresh
editing
submit
revert
```

---

## 21. DDL

Переиспользовать JetBrains DDL generation/navigation.

Минимально поддержать:

```text
table
view
index
sequence
function
schema
```

Ожидаемое действие:

```text
Right Click
→ SQL Scripts / DDL
```

---

## 22. Навигация

Переиспользовать штатную навигацию DataGrip.

Из:

```sql
SELECT *
FROM public.users;
```

Ctrl+Click по `public.users` должен переходить к объекту БД.

Желательно поддержать:

```text
Go to declaration
Find usages
Quick documentation
Database object navigation
```

---

## 23. Metadata / Introspection

Если модель metadata DataGrip доступна — использовать её.

Исследовать и переиспользовать:

```text
schema introspection
refresh
database object model
tables
columns
keys
indexes
routines
types
dependencies
```

Fallback через:

```text
java.sql.DatabaseMetaData
pg_catalog
information_schema
```

использовать только для отсутствующих функций.

---

## 24. Асинхронность

Не выполнять DB-операции в EDT.

Все операции:

```text
Connect
Introspection
Query execution
Refresh
DDL generation
Driver operations
```

должны использовать штатные background/coroutine/task API IntelliJ/DataGrip.

---

## 25. Query cancellation

Переиспользовать штатный механизм cancel Database Tools.

При JDBC fallback использовать:

```java
Statement.cancel();
```

После отмены UI и connection state должны оставаться согласованными.

---

## 26. SQL errors

Для PostgreSQL показывать:

```text
ERROR
SQLSTATE
Position
Detail
Hint
Constraint
```

Если JetBrains API предоставляет позиционирование ошибки — использовать штатную подсветку в SQL Editor.

---

## 27. Query History

Переиспользовать штатную историю запросов, если она доступна.

Минимально хранить/отображать:

```text
timestamp
datasource
database
SQL
duration
rows
status
```

Собственную историю создавать только при отсутствии доступного механизма.

---

## 28. OpenData extensions

Собственные доработки оформлять отдельными модулями/плагинами, а не изменениями во всех слоях платформы.

Предпочтительная структура:

```text
opendata/
├── product
├── integration
├── extensions-api
├── extensions-ui
├── drivers
└── plugins
```

---

## 29. Build

Приоритетная ОС:

```text
Windows 11
```

Дополнительно:

```text
Linux
macOS
```

Создать:

```text
README.md
docs/build.md
scripts/build.ps1
scripts/run.ps1
```

`build.ps1` должен:

```text
проверять JDK
проверять исходники IntelliJ
находить локальную JetBrains IDE/DataGrip при необходимости
находить Database Tools plugin
проверять build compatibility
запускать сборку
выводить понятную ошибку при несовместимости
```

Пути конкретного компьютера не хардкодить.

---

## 30. Совместимость версий

Claude обязан сформировать и поддерживать:

```text
docs/compatibility-matrix.md
```

Матрица должна содержать:

```text
IntelliJ build
DataGrip build
Database Tools build
JDK
Kotlin
Gradle
status
```

Использовать компоненты одной совместимой build-линейки JetBrains.

---

## 31. Этап 1 — Proof of Concept

После Research создать минимальный POC:

```text
IntelliJ sandbox/product
→ Database Tools loaded
→ Database Tool Window visible
→ PostgreSQL Data Source
→ Test Connection
→ schema introspection
```

На этом этапе **не писать собственную DB инфраструктуру**.

Acceptance:

```text
IDE запускается
Database Tool Window присутствует
PostgreSQL Data Source создаётся
Test Connection проходит
schemas/tables загружаются
```

---

## 32. Этап 2 — SQL Console

Выполнить:

```sql
SELECT version();
```

Acceptance:

```text
SQL editor работает
PostgreSQL dialect выбран
highlighting работает
Ctrl+Enter выполняет запрос
ошибки отображаются
результат возвращается
```

---

## 33. Этап 3 — DataGrid

Контрольный запрос:

```sql
SELECT
    generate_series AS id,
    md5(generate_series::text) AS value
FROM generate_series(1,100);
```

Acceptance:

```text
результат отображается в JetBrains DataGrid
scrolling
selection
copy
column resize
sorting
filtering
```

---

## 34. Этап 4 — Editing

Создать:

```sql
CREATE TABLE opendata_test (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    created_at TIMESTAMP DEFAULT now()
);
```

Acceptance:

```text
таблица открывается из Database Explorer
значение редактируется в DataGrid
Submit сохраняет изменение
Revert отменяет несохранённое изменение
Commit/Rollback работают
```

---

## 35. Этап 5 — OpenData product

После успешного POC собрать отдельный продукт:

```text
OpenData IDE
```

Настроить:

```text
branding
launcher
product code
plugins
Database Tools integration
PostgreSQL support
settings
```

Acceptance:

```text
OpenData запускается отдельным launcher
Database Explorer работает
SQL Console работает
DataGrid работает
editing работает
```

---

## 36. Тесты

Обязательные smoke/integration tests:

```text
IDE startup
plugin loading
Database Tools availability
PostgreSQL connection
metadata introspection
SQL parsing
SELECT execution
DataGrid creation
UPDATE through grid
Commit
Rollback
query cancel
```

Для DB integration tests допускается локальный PostgreSQL или Testcontainers.

---

## 37. Документация

Создать:

```text
README.md

docs/
├── architecture.md
├── build.md
├── jetbrains-db-analysis.md
├── grid-analysis.md
├── plugin-dependencies.md
├── compatibility-matrix.md
├── postgresql.md
└── roadmap.md
```

В `plugin-dependencies.md` описать:

```text
plugin/module name
plugin ID
JAR/module path
version/build
compile dependency
runtime dependency
способ поиска локальной установки
```

---

## 38. Главное правило для Claude

При любом обращении к JetBrains API:

> **Сначала найти реальный класс/метод в текущей версии. Потом писать код.**

Не генерировать вымышленные JetBrains API на основании памяти.

Если готовый internal/закрытый API технически решает задачу и доступен в текущем окружении — рассматривать его как предпочтительный вариант для этой локальной сборки.

Если можно переиспользовать DataGrip/Database Tools вместо написания собственной подсистемы — **выбирать переиспользование**.

---

## 39. Формат работы Claude Code

Каждый этап выполнять в формате:

```text
PLAN
Что будет сделано.

FOUND
Какие реальные JetBrains API/модули найдены.

DECISION
Что переиспользуем и почему.

FILES
Какие файлы будут созданы/изменены.

IMPLEMENTATION
Что реализовано.

BUILD
Точная команда сборки.

TEST
Точная команда проверки.

RESULT
Фактический результат.

NEXT
Следующий минимальный шаг.
```

Если сборка падает:

```text
не переходить к следующему этапу
разобрать ошибку
исправить
повторить build
```

---

## 40. Definition of Done для MVP

MVP готов, когда можно:

```text
1. Запустить OpenData IDE
2. Создать PostgreSQL Data Source
3. Проверить соединение
4. Подключиться
5. Просмотреть schemas
6. Просмотреть tables/views/functions
7. Просмотреть структуру таблицы
8. Создать SQL Console
9. Получить PostgreSQL highlighting/completion
10. Выполнить SELECT
11. Получить ResultSet в JetBrains DataGrid
12. Сортировать и фильтровать данные
13. Копировать данные
14. Открыть таблицу напрямую
15. Изменить значение в DataGrid
16. Submit изменений
17. Revert изменений
18. Выполнить Commit/Rollback
19. Увидеть SQL error с позицией
20. Отменить долгий запрос
```

---

## 41. Стартовая команда для Claude Code

```text
Начни с ЭТАПА 0 — Research.

Не реализовывай собственные Database Explorer, Data Source Manager, SQL parser,
SQL completion, query executor, metadata model или DataGrid, пока не проверено,
можно ли переиспользовать соответствующие компоненты JetBrains DataGrip /
Database Tools and SQL.

Исследуй:
1. текущий JetBrains/intellij-community;
2. каталог grid/;
3. установленный Database Tools and SQL / DataGrip, если доступен;
4. plugin.xml и module descriptors;
5. JAR dependencies;
6. product configuration;
7. extension points и services com.intellij.database.*.

Сформируй:
- docs/jetbrains-db-analysis.md
- docs/grid-analysis.md
- docs/plugin-dependencies.md
- docs/compatibility-matrix.md

Затем создай минимальный Proof of Concept:

IntelliJ sandbox/product
→ Database Tools loaded
→ Database Tool Window visible
→ PostgreSQL Data Source
→ Test Connection
→ schema introspection
→ SQL Console
→ SELECT version()
→ result in JetBrains DataGrid.

Только после успешного POC переходи к формированию OpenData IDE.

В приоритете — максимальное переиспользование готовой реализации JetBrains,
включая доступные internal/закрытые API и модули.
Собственный код писать только для product integration и реально отсутствующих функций.
```

---

## 42. Текущее состояние репозитория (для продолжения работы)

- **С 0.2.0 продукт — самостоятельная IDE на открытой IntelliJ Platform** (`ossPlatformVersion` в `gradle.properties`,
  сейчас 2026.2.3 / IC-262.10968.63). Закрытый Database Tools and SQL не используется и не встраивается: лицензия
  и проверка `com.intellij.modules.database-capable` (см. `docs/architecture.md`). Плагин 0.1.0 (`opendata/integration`) удалён.
- Модуль `opendata/db` (`io.opendata.db`): DB-слой на JDBC + открытый `intellij.grid` (реестр API grid — `docs/architecture.md`).
  СУБД: PostgreSQL, Apache Cloudberry (драйвер PG), ClickHouse, Dremio (Arrow Flight SQL); всё СУБД-специфичное — `model/DbKind.kt`.
- Продукт: `tools/product/AssembleProduct.java` (задача `assembleProduct`): состав плагинов, брендинг, launcher, ZIP.
- Скрипты: `scripts/build.*` (платформа → плагин + тесты → продукт), `scripts/test.* --with-docker`, `scripts/run.* [--check | --smoke <id>]`.
- Тесты: `:opendata:db:test` — 25 (PG, CB-режим, CH, Dremio через `OPENDATA_*_URL`); smoke собранной IDE — `run.* --smoke`.
- CI: `.github/workflows/build.yml` — linux (СУБД-сервисы, тесты, smoke под Xvfb, tar.gz), windows (ZIP, `run.ps1 -Check`), release.
- Этап 0 (исследование DataGrip) сохранён как история: `scripts/research.*`, `docs/jetbrains-db-analysis.md` и др.
  Сырые выгрузки `docs/research/*` в публичный репозиторий не публикуются.
- Приёмка и DoD: `docs/acceptance.md`; СУБД: `docs/databases.md`, `docs/postgresql.md`; план и оценки (чд): `docs/roadmap.md`.
