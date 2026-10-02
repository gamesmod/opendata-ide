# Поддерживаемые СУБД

Сводная таблица отличий приведена в [`architecture.md`](architecture.md#поддержка-субд). PostgreSQL описан отдельно в [`postgresql.md`](postgresql.md).

## Apache Cloudberry

Cloudberry — MPP-форк Greenplum на ядре PostgreSQL. Протокол и системный каталог у него совместимы с PostgreSQL, поэтому:

- драйвер PostgreSQL JDBC (в комплекте), URL `jdbc:postgresql://<coordinator>:5432/<db>`;
- Explorer, completion, ошибки с позицией, транзакции и редактирование таблиц работают так же, как в PostgreSQL;
- DDL таблицы дополняется `DISTRIBUTED BY (...)` / `DISTRIBUTED RANDOMLY` через `pg_get_table_distributedby`.
  Если функции нет (например, при подключении к обычному PostgreSQL в режиме Cloudberry), строка пропускается.

Отдельного образа Cloudberry в стенде нет. Тесты (`testCloudberryModeOnPostgresProtocol`) и smoke выполняются
на PostgreSQL в режиме Cloudberry. Для проверки на кластере задайте `OPENDATA_CB_URL`, `OPENDATA_CB_USER` и `OPENDATA_CB_PASSWORD`.

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

## Драйверы без доступа в интернет

Драйвер ищется в таком порядке:

1. `OPENDATA_DRIVERS_DIR`;
2. `plugins/opendata-db/drivers` в каталоге IDE;
3. `<config>/opendata/drivers`. На Windows это `%APPDATA%\OpenData\OpenData2026.2\opendata\drivers`,
   на Linux — `~/.config/OpenData/OpenData2026.2/opendata/drivers`.

Положите туда JAR с точными именами из сообщения об ошибке: `clickhouse-jdbc-0.10.0-all.jar`, `slf4j-api-2.0.17.jar`,
`slf4j-nop-2.0.17.jar`, `flight-sql-jdbc-driver-19.0.0.jar`. Другой вариант — указать свой JAR в поле «JAR драйвера» диалога источника данных.
