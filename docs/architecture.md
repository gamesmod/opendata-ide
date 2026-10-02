# Архитектура OpenData IDE

## Почему отдельный продукт, а не плагин к DataGrip

Версия 0.1.0 была плагином поверх закрытого *Database Tools and SQL* (`com.intellij.database`). Задача
поменялась: нужна полноценная самостоятельная IDE. Закрытый плагин в ней использовать нельзя по двум причинам.

1. Лицензия JetBrains не разрешает распространять Database Tools в составе стороннего продукта,
   в том числе некоммерческого и учебного.
2. Плагин проверяет модуль `com.intellij.modules.database-capable`. Его объявляют только продукты JetBrains,
   и обходить эту проверку мы не будем.

Поэтому порядок переиспользования из ТЗ (раздел 2) применён к **открытой** части стека:

| Уровень ТЗ | Что взято |
|---|---|
| 1. API DataGrip / Database Tools | закрытый, в продукт не входит (исследование этапа 0 сохранено в `docs/jetbrains-db-analysis.md`) |
| 2. IntelliJ Platform API | вся платформа: редактор, PSI, Lexer/Highlighter, completion, tool windows, actions, `PasswordSafe`, фоновые задачи |
| 3. `intellij.grid` | DataGrid, модель строк и колонок, редактирование, мутации, paging (открытый модуль Apache 2.0) |
| 4. Стандартный JDBC | подключение, выполнение, метаданные (`DatabaseMetaData`, `pg_catalog`, `system.*`, `INFORMATION_SCHEMA`) |
| 5. Собственный код | только то, чего нет в открытом стеке: модель источников данных, драйверы, лёгкий SQL-лексер, Explorer, связь JDBC ↔ grid |

## Слои

```text
┌───────────────────────────────────────────────────────────────────────────────┐
│ Открытая IntelliJ Platform 2026.2.3 (IC-262.10968.63, JBR 25)                 │
│   Editor · PSI · VFS · Actions · Tool Windows · PasswordSafe · Progress/Tasks │
├───────────────────────────────────────────────────────────────────────────────┤
│ intellij.grid.core.plugin (+ platform-structureView-plugin, его зависимость)  │
│   DataGrid · DataGridListModel · GridMutator · GridPagingModel · копирование  │
├───────────────────────────────────────────────────────────────────────────────┤
│ opendata-db (io.opendata.db)                                                  │
│   model/    DbKind, DataSourceConfig, DataSourceStorage (проект), PasswordSafe│
│   drivers/  DriverService, DriverSettings, DriversConfigurable (менеджер)     │
│   data/     DataExporter, DataImporter, CsvReader, диалоги импорта/экспорта   │
│   session/  DbSession, DbSessions, QueryExecutor, SqlSplitter, DbError        │
│   meta/     MetadataLoader, MetadataCache, DdlGenerator                       │
│   lang/     SQL: FileType, Lexer, Highlighter, Commenter, Completion          │
│   grid/     ResultGrids, TableDataController, TablePager, TableMutator        │
│   ui/       Database Explorer, Results, Console, DataSourceDialog,            │
│             TableDataEditor, Workspace, SmokeTest                             │
├───────────────────────────────────────────────────────────────────────────────┤
│ tools/product/AssembleProduct.java — состав плагинов, брендинг, launcher, ZIP │
└───────────────────────────────────────────────────────────────────────────────┘
```

## Продукт

`AssembleProduct` берёт распакованную открытую сборку IntelliJ IDEA и выполняет следующие шаги:

- оставляет из `plugins/` только `grid-core-plugin` и `platform-structureView-plugin` (без второго grid не загрузится) и добавляет `opendata-db`;
- удаляет `idea.sh`/`.bat`, `format`/`inspect`/`ltedit`, значки IDEA и бинарный кэш `plugins/plugin-classpath.txt`;
- кладёт JDBC-драйвер PostgreSQL в `plugins/opendata-db/drivers`;
- собирает `lib/opendata-branding.jar`: `idea/IdeaApplicationInfo.xml` (OpenData IDE, product code `OD`, essential-плагины `io.opendata.db` и `intellij.grid.core.plugin`), значок и splash;
- переписывает `product-info.json`: имя, `productCode`, `envVarBaseName=OPENDATA`, `dataDirectoryName`, launcher, порядок `bootClassPathJarNames` (брендинг первым), layout плагинов;
- переименовывает launcher в `bin/opendata` или `bin\opendata64.exe` и задаёт JVM-параметры (в `additionalJvmArguments` файла `product-info.json` и в `.vmoptions`):

```text
-Dintellij.platform.load.app.info.from.resources=true   # ApplicationInfo из opendata-branding.jar, а не из байт-кода IDEA
-Didea.paths.selector=OpenData2026.2                    # свои каталоги config/system/logs
-Didea.vendor.name=OpenData
-Dide.no.platform.update=true                           # без проверки обновлений
-Dide.show.tips.on.startup.default.value=false
-Dide.experimental.ui.onboarding=false                  # без промо-диалога нового UI
```

IDE работает без пользовательских проектов: при старте открывается служебная папка `~/OpenData`, которая
сразу помечается доверенной (`TrustedProjects`). Источники данных, история и настройки хранятся на уровне приложения.

## Проекты и подключения

Как в DataGrip, подключения принадлежат проекту. Проект — это обычная папка:

| Что | Где хранится | Компонент |
|---|---|---|
| Подключения | `<проект>/.idea/opendata-datasources.xml` | `DataSourceStorage` (сервис проекта, `PersistentStateComponent`) |
| Пароли | хранилище паролей ОС через `PasswordSafe`, ключ — id подключения | `DataSources.getPassword/setPassword` |
| SQL-консоли | `<проект>/.idea/opendata/consoles/<id подключения>/console*.sql` | `ConsoleFiles` |
| Настройки драйверов | `<config>/options/opendata-drivers.xml` (общие для всех проектов) | `DriverSettings` |
| Загруженные драйверы | `<config>/opendata/drivers` | `DriverService` |

Id подключения — UUID, поэтому код без контекста проекта (сессии, completion) находит подключение по id среди открытых
проектов (`DataSources.find`). При первом запуске (нет недавних проектов) открывается проект по умолчанию `~/OpenData`.
Подключения версии 0.2.0, которые хранились на уровне IDE, при первом открытии проекта переносятся в него
(`LegacyDataSourcesMigration`), и пользователь получает уведомление. Изменения подключений сразу записываются
на диск (`project.scheduleSave()`), поэтому `.idea/opendata-datasources.xml` можно хранить в VCS вместе с проектом.

## Менеджер драйверов

Для каждой СУБД `DriverSettings` хранит переопределения. Пустое поле означает значение по умолчанию из `DbKind`:

- версия артефакта Maven: встроенная проверяется закреплённой SHA-256, другая — по `.sha1` из Maven Central;
- свои JAR вместо Maven;
- класс драйвера;
- шаблон URL `{host}`, `{port}`, `{database}`;
- свойства по умолчанию, которые дополняют встроенные.

Порядок поиска JAR: поле «JAR драйвера» подключения → свои JAR менеджера → `OPENDATA_DRIVERS_DIR` → комплект IDE
(`plugins/opendata-db/drivers`) → `<config>/opendata/drivers` → загрузка. Драйвер каждой СУБД загружается
в отдельный `URLClassLoader`.

## Импорт и экспорт

- **Экспорт** (`DataExporter`): таблица или представление выгружаются потоково через отдельное соединение
  (`fetchSize` 1000; в PostgreSQL курсор работает вне auto-commit). Результат консоли выгружается из уже загруженных
  строк, а если он был обрезан лимитом и это запрос на чтение, запрос выполняется повторно и выгружается полностью.
  Форматы: CSV и TSV (RFC 4180), JSON, SQL INSERT (литералы с учётом СУБД), Markdown.
- **Импорт** (`DataImporter`): CSV и TSV (кавычки, переводы строк внутри значения, BOM). Колонки сопоставляются
  по заголовку без учёта регистра, без заголовка — по порядку. Числа и логические значения приводятся к типу
  колонки, остальное передаётся строкой (PostgreSQL приводит тип сам благодаря `stringtype=unspecified`).
  Вставка идёт пакетами по 1000 строк. В СУБД с транзакциями импорт атомарный. Можно создать новую таблицу
  с текстовыми колонками: `TEXT` или `Nullable(String)` + `MergeTree` в ClickHouse. Dremio подключён только для чтения.
- **Подключения**: экспорт и импорт XML того же формата, что `.idea/opendata-datasources.xml`, без паролей.

## Поддержка СУБД

Всё, что зависит от СУБД, собрано в `DbKind`; остальной код работает со стандартным JDBC и ветвится по `kind`
только там, где без этого не обойтись.

| | PostgreSQL | Apache Cloudberry | ClickHouse | Dremio |
|---|---|---|---|---|
| Драйвер | `org.postgresql` 42.7.13 (в комплекте) | драйвер PostgreSQL | `clickhouse-jdbc` 0.10.0 `all` + slf4j | Arrow Flight SQL JDBC 19.0.0 |
| URL по умолчанию | `jdbc:postgresql://h:5432/db` | то же | `jdbc:clickhouse://h:8123/db` | `jdbc:arrow-flight-sql://h:32010/?useEncryption=false` |
| Свойства по умолчанию | `stringtype=unspecified`, `ApplicationName` | то же | `compress=0` | — |
| Транзакции | да | да | нет (auto-commit) | нет |
| Метаданные | `pg_catalog` | `pg_catalog` | `system.tables/columns/...` | `INFORMATION_SCHEMA` |
| Позиция ошибки | `ServerErrorMessage.position` | то же | `position N` из текста | — |
| Редактирование таблиц | по PK | по PK | по первичному ключу (`ALTER TABLE … UPDATE … SETTINGS mutations_sync=2`) | только чтение |
| DDL | `pg_get_*def` + сборка таблицы | + `DISTRIBUTED BY` | `SHOW CREATE` | DDL представлений |

## Реестр используемых API `intellij.grid`

Все классы найдены в открытой сборке 2026.2.3 (`plugins/grid-core-plugin/lib/modules/`). Модули
`intellij.grid.core.impl` и `intellij.grid.impl` объявлены публичными, `intellij.grid` и `intellij.grid.types`
внутренние. Публичного SDK у grid нет, поэтому риск изменения между версиями средний.

| Класс | JAR | Назначение | Где используется |
|---|---|---|---|
| `GridUtil.createDataGrid`, `GridUtil.configureCsvTable`, `configureFullSizeTable` | `intellij.grid.impl.jar` | создание DataGrid и стандартное оформление | `ResultGrids.create` |
| `GridHelper.set`, `GridHelperImpl` | `intellij.grid.impl.jar` | обязательный helper grid; `OpenDataGridHelper` разрешает редактирование только при наличии мутатора | `ResultGrids` |
| `DataGrid`, `DataGridRequestPlace` | `intellij.grid.impl.jar` | компонент таблицы, источник запросов | Results, редактор таблицы |
| `CachedGridDataHookUp` | `intellij.grid.core.impl.jar` | связь данных с grid | `TableHookUp` |
| `DataGridListModel`, `GridColumn`, `GridRow`, `ModelIndex(Set)` | `intellij.grid.core.impl.jar` | модель строк и колонок | `ResultGrids.fill` |
| `GridStorageAndModelUpdater`, `GridMutationModel` | `intellij.grid.core.impl.jar` | обновление модели с рассылкой событий (без него созданный grid не перерисуется) | `ResultGrids.fill` |
| `GridMutator.DatabaseMutator`, `MutationData`, `CellMutation`, `MutationType` | `intellij.grid.core.impl.jar` | правка ячеек, вставка и удаление строк, Submit/Revert | `TableMutator` |
| `GridPagingModel`, `GridLoader`, `GridRequestSource` | `intellij.grid.core.impl.jar` | постраничная загрузка и подсчёт строк | `TablePager`, `TableDataController` |
| `ReservedCellValue` | `intellij.grid.core.impl.jar` | NULL и DEFAULT в ячейках | `TableMutator` |
| `SimpleErrorInfo` | `intellij.grid.core.impl.jar` | ошибка запроса в grid | `TableDataController` |

Подключение в `plugin.xml` (модель плагинов v2):

```xml
<dependencies>
    <plugin id="intellij.grid.core.plugin"/>
    <module name="intellij.grid.core.impl"/>
    <module name="intellij.grid.impl"/>
</dependencies>
```

Сборка: `bundledPlugin("intellij.grid.core.plugin")` в `opendata/db/build.gradle.kts`, платформа подключается через `local(ossIdePath)`.

## Асинхронность (ТЗ 24, 25)

- Подключение, introspection, выполнение запросов, загрузка страниц, Submit и DDL выполняются в `Task.Backgroundable`
  или `executeOnPooledThread`. В EDT модель обновляется только готовым результатом.
- Отмена: `Statement.cancel()` из наблюдателя за `ProgressIndicator`. После отмены соединение остаётся рабочим (тест `testCancelLongQuery`).
- JDBC-драйверы загружаются в отдельный `URLClassLoader` с родителем `platform` classloader JDK, поэтому их зависимости
  не конфликтуют с библиотеками IDE.
