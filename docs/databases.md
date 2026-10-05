# Поддерживаемые СУБД

Сводная таблица отличий приведена в [`architecture.md`](architecture.md#поддержка-субд). PostgreSQL описан отдельно в [`postgresql.md`](postgresql.md).

## Greenplum 6/7

Тип **Greenplum** — для Greenplum 6 (ядро PostgreSQL 9.4), Greenplum 7 (PostgreSQL 12) и совместимых форков
(Greengage, WarehousePG, open-gpdb). Драйвер — PostgreSQL JDBC из комплекта, URL `jdbc:postgresql://<master>:5432/<db>`.
Explorer, консоль, транзакции, редактирование таблиц и DDL (с `DISTRIBUTED BY`) работают как для PostgreSQL;
запросы к каталогу учитывают версию ядра (в GP6 нет `pg_proc.prokind` и `attidentity`).
Проверено в CI на `andruche/greenplum:6` и `andruche/greenplum:7`.

## Apache Cloudberry

Cloudberry — MPP-форк Greenplum на ядре PostgreSQL. Протокол и системный каталог у него совместимы с PostgreSQL, поэтому:

- драйвер PostgreSQL JDBC (в комплекте), URL `jdbc:postgresql://<coordinator>:5432/<db>`;
- Explorer, completion, ошибки с позицией, транзакции и редактирование таблиц работают так же, как в PostgreSQL;
- DDL таблицы дополняется `DISTRIBUTED BY (...)` / `DISTRIBUTED RANDOMLY` через `pg_get_table_distributedby`.
  Если функции нет (например, при подключении к обычному PostgreSQL в режиме Cloudberry), строка пропускается.

Проверено в CI на кластере Apache Cloudberry 2.1.0-incubating (`woblerr/cloudberry`): метаданные, DDL с `DISTRIBUTED BY`,
bulk-загрузка COPY и gpfdist, выгрузка. Для своего кластера задайте `OPENDATA_CB_URL`, `OPENDATA_CB_USER` и `OPENDATA_CB_PASSWORD`.

## ClickHouse

| Параметр | Значение |
|---|---|
| Драйвер | `com.clickhouse:clickhouse-jdbc:0.10.0:all` + `slf4j-api`/`slf4j-nop` 2.0.17 (загружаются при первом подключении) |
| URL | `jdbc:clickhouse://<host>:8123/<db>` (HTTP-интерфейс) |
| Свойства по умолчанию | `compress=0`: без него драйвер 0.10 не читает ответы серверов 25.x и 26.x (`Invalid LZ4 magic byte`) |
| Стенд | `clickhouse/clickhouse-server:25.8`, порт 8123, пользователь `default` без пароля; init `docker/clickhouse/01-opendata.sql` |

Особенности:

- в Explorer верхний уровень — базы данных (`default`, `opendata`, …); метаданные берутся из `system.tables`, `system.columns` и т. д.;
- транзакций нет, консоль всегда работает в auto-commit;
- редактирование таблиц идёт по колонкам первичного ключа (`system.columns.is_in_primary_key`; если PRIMARY KEY не задан явно, он совпадает с ORDER BY).
  `UPDATE` выполняется как `ALTER TABLE … UPDATE … SETTINGS mutations_sync = 2`, то есть синхронная мутация;
  `INSERT` и `DELETE` (lightweight delete) выполняются обычным SQL;
- DDL: `SHOW CREATE TABLE/VIEW`;
- позиция ошибки извлекается из текста `… (position N)`.

## Dremio

| Параметр | Значение |
|---|---|
| Драйвер | `org.apache.arrow:flight-sql-jdbc-driver:19.0.0` (загружается при первом подключении) |
| URL | `jdbc:arrow-flight-sql://<host>:32010/?useEncryption=false`; для TLS — `useEncryption=true` |
| Стенд | `dremio/dremio-oss:26.0`, UI http://localhost:9047, Flight 32010; `docker/dremio/init.sh` создаёт пользователя `dremio`/`dremio123`, пространство `opendata` и представление `events_v` |

Особенности:

- в Explorer верхний уровень — источники и пространства Dremio (`$scratch`, `@<user>`, `opendata`, `sys.*`);
  метаданные берутся из `INFORMATION_SCHEMA`;
- идентификаторы всегда квотируются двойными кавычками (`"opendata"."events_v"`);
- данные открываются только для чтения: Flight SQL в Dremio не поддерживает DML по ключу. Paging работает через `LIMIT/OFFSET`;
- DDL доступен для представлений (`INFORMATION_SCHEMA."VIEWS"`);
- транзакций нет.

## Bulk-загрузка Greenplum и Cloudberry (Windows без gpfdist.exe и gpload)

Контекстное меню таблицы → **Bulk-загрузка (COPY / gpfdist)…** и **Bulk-выгрузка (gpfdist)…**; окно Database → «Ещё»
→ **gpfdist-сервер…**. Нативные утилиты Greenplum (gpfdist.exe, gpload, Python) не нужны: всё работает внутри IDE.

| Способ | Как работает | Когда использовать |
|---|---|---|
| COPY через координатор | `COPY … FROM STDIN` (PgJDBC CopyManager), поток файла идёт через координатор | любые объёмы до десятков ГБ; единственный способ, если сегменты не видят рабочую станцию |
| gpfdist (параллельно) | IDE запускает встроенный gpfdist, создаёт временную `READABLE EXTERNAL TABLE … LOCATION ('gpfdist://<адрес>:<порт>/<файл>')`, сегменты читают блоки файла параллельно, затем `INSERT … SELECT` в одной транзакции | большие файлы: скорость определяется сегментами, а не координатором |
| Выгрузка (gpfdist) | временная `WRITABLE EXTERNAL TABLE … DISTRIBUTED RANDOMLY`, сегменты пишут файл параллельно; заголовок пишет IDE | быстрая выгрузка больших таблиц на рабочую станцию |

Параметры: CSV или TEXT, разделитель, заголовок (сопоставление колонок по именам без учёта регистра), строка NULL,
кодировка файла (`UTF8`, `WIN1251`, …), TRUNCATE перед загрузкой и **допустимое число ошибочных строк**: при значении ≥ 2
используется `LOG ERRORS SEGMENT REJECT LIMIT n ROWS`, отклонённые строки видны в `gp_read_error_log()`, их число IDE
показывает в уведомлении. При 0 любая ошибка отменяет загрузку целиком.

**Сеть.** Сегменты подключаются к рабочей станции: адрес по умолчанию — тот, которым IDE видна координатору
(`inet_client_addr()`), его можно указать вручную (например, при VPN/NAT). Порт `0` — любой свободный; при первом
запуске Windows спросит разрешение брандмауэра для Java (JetBrains Runtime) — его нужно дать для частной сети, либо
задать фиксированный порт и открыть его.

Встроенный gpfdist реализует протокол сегментов GP6/GP7/Cloudberry (по исходникам `gpfdist.c` и `url_curl.c`):
протокол 1 (блоки F/O/L/D), разбивку файла на блоки целых записей с учётом кавычек CSV, пропуск заголовка сервером,
маски и несколько файлов, запись с `X-GP-SEQ`/`X-GP-DONE`. Не поддерживаются сжатие zstd (сегменты автоматически
обходятся без него), `gpfdists` (TLS) и трансформации (`#transform`).

**gpfdist-сервер** (окно Database → «Ещё»): отдаёт выбранный каталог для своих внешних таблиц, например
`CREATE READABLE EXTERNAL TABLE … LOCATION ('gpfdist://10.0.0.5:8080/sales_*.csv') FORMAT 'CSV' (HEADER)`.
Работает, пока открыта IDE.

## Менеджер драйверов

**Settings → Tools → OpenData: драйверы**. Тот же экран открывают меню SQL → Драйверы…, «Ещё → Драйверы…» в окне
Database и ссылка «Менеджер драйверов…» в диалоге подключения. Для каждой СУБД доступно:

| Поле / кнопка | Назначение |
|---|---|
| Состояние, файлы | где найден каждый JAR: в комплекте, загружен, `OPENDATA_DRIVERS_DIR`, свои JAR; путь — во всплывающей подсказке |
| Версия драйвера | другая версия из Maven Central (контрольная сумма сверяется с `.sha1` Maven Central) |
| Свои JAR | локальные файлы вместо загрузки |
| Класс драйвера, шаблон URL, свойства | переопределения для всех подключений этой СУБД |
| Загрузить / Проверить / Удалить загруженные / Открыть папку | загрузка заранее; проверка класса и версии драйвера; очистка; каталог драйверов |

## Драйверы

С версии 0.4.0 все драйверы входят в дистрибутив (`plugins/opendata-db/drivers`, ~60 МБ): PostgreSQL JDBC 42.7.13
(PostgreSQL, Greenplum, Cloudberry), ClickHouse JDBC 0.10.0 + SLF4J, Arrow Flight SQL JDBC 19.0.0 (Dremio). Загрузка из
Maven Central нужна, только если в менеджере драйверов выбрана другая версия. Порядок поиска JAR:

1. поле «JAR драйвера» подключения, затем «Свои JAR» в менеджере драйверов;
2. `OPENDATA_DRIVERS_DIR`;
3. `plugins/opendata-db/drivers` в каталоге IDE;
4. `<config>/opendata/drivers` (загруженные версии): на Windows `%APPDATA%\OpenData\OpenData2026.2\opendata\drivers`.

## JDBC URL

Поле JDBC URL в диалоге подключения синхронизировано с полями: изменение хоста, порта или базы пересобирает URL, а
вставленный URL заполняет поля и **определяет тип СУБД по префиксу** (`jdbc:clickhouse:` → ClickHouse,
`jdbc:arrow-flight-sql:` → Dremio, `jdbc:postgresql:` — PostgreSQL, Greenplum или Cloudberry). URL с параметрами
(`?sslmode=require&…`) сохраняется как есть. В дереве Database адрес показывается из URL. Если URL не подходит к типу
подключения, IDE сообщает об этом явно, а не ошибкой драйвера.
