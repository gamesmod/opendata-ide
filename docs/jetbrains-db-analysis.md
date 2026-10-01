# Анализ JetBrains DB stack

> Сгенерировано `tools/research/Research.java` 2026-10-01 по установке `DataGrip 2026.2.6` (build `DB-262.10968.148`).
> Все перечисленные классы/EP/сервисы реально найдены в этой сборке. Повторный запуск перезаписывает файл.

## 1. Версии

| Параметр | Значение |
|---|---|
| Продукт | DataGrip 2026.2.6 |
| Product code | DB |
| Build number (IntelliJ Platform) | 262.10968.148 |
| build.txt | DB-262.10968.148 |
| DataGrip build | 262.10968.148 |
| Database Tools and SQL | com.intellij.database 262.10968.148 |
| JBR (JDK runtime IDE) | 25.0.4 / JBR-25.0.4+1-508.27-nomod |
| Kotlin stdlib в IDE | 2.4.0 (lib/util-8.jar) |
| Gradle (проект) | 9.8.0 |
| intellij-community | не передан (`--community`) |
| Просканировано JAR | 972 |
| Проиндексировано классов (com.intellij.database/sql/grid) | 10250 |

## 2. Плагин Database Tools and SQL

| Поле | Значение |
|---|---|
| Plugin ID | `com.intellij.database` |
| Name | Database Tools and SQL |
| Version | 262.10968.148 |
| Vendor | JetBrains |
| Descriptor | `plugins/DatabaseTools/lib/database-plugin.jar!/META-INF/plugin.xml` |
| Plugin dir | `plugins/DatabaseTools` |
| depends | — |
| dependencies/plugin | `com.intellij.modules.database-capable` |
| dependencies/module | `intellij.spellchecker`, `intellij.json.backend`, `intellij.platform.rpc.backend`, `intellij.regexp` |
| content modules | 104 |

<details><summary>Content modules (104)</summary>

- `intellij.database.diagrams`
- `intellij.database.flameGraphs`
- `intellij.database.docker`
- `intellij.database.grazie`
- `intellij.database.java`
- `intellij.database.cloudExplorer.jba`
- `intellij.database.impl.bookmarks`
- `intellij.database.impl.frontend`
- `intellij.database.impl.navbar`
- `intellij.database.ai`
- `intellij.database.mcp`
- `intellij.database.ssh [optional]`
- `intellij.database.ssh.ui [optional]`
- `intellij.database.completionMlRanking`
- `intellij.database.sql.copyright`
- `intellij.database.sql.intelliLang`
- `intellij.database.sql.frontend.core`
- `intellij.database.sql.frontend.impl`
- `intellij.database.frontend.split`
- `intellij.database.backend.split`
- `intellij.database.trialPromotion.idesWithFreeTier`
- `intellij.database.pro`
- `intellij.database [required]`
- `intellij.database.util [required]`
- `intellij.database.sql [required]`
- `intellij.database.impl [required]`
- `intellij.database.impl.jcef`
- `intellij.database.connectivity [required]`
- `intellij.database.connectivity.ex [required]`
- `intellij.database.jdbcConsole [required]`
- `intellij.database.jdbcConsole.shim [required]`
- `intellij.database.core.impl [required]`
- `intellij.database.sql.core.impl [required]`
- `intellij.database.sql.impl [required]`
- `intellij.database.sql.backend.core [required]`
- `intellij.database.sql.common.core [required]`
- `intellij.database.sql.common.impl [required]`
- `intellij.database.impl.common [required]`
- `intellij.database.plugin.frameworks [required]`
- `intellij.database.cliCommands [required]`
- `intellij.libraries.jts.io.common [required]`
- `intellij.platform.commercial.verifier [embedded]`
- `intellij.database.dialects.mysqlbase.impl [required]`
- `intellij.database.dialects.mssql.impl [required]`
- `intellij.database.dialects.base [required]`
- `intellij.database.dialects.sql92 [required]`
- `intellij.database.dialects.generic [required]`
- `intellij.database.dialects.bigquery [required]`
- `intellij.database.dialects.cassandra [required]`
- `intellij.database.dialects.clickhouse [required]`
- `intellij.database.dialects.cockroach [required]`
- `intellij.database.dialects.couchbase [required]`
- `intellij.database.dialects.db2 [required]`
- `intellij.database.dialects.derby [required]`
- `intellij.database.dialects.exasol [required]`
- `intellij.database.dialects.greenplum [required]`
- `intellij.database.dialects.h2 [required]`
- `intellij.database.dialects.hivebase [required]`
- `intellij.database.dialects.hive [required]`
- `intellij.database.dialects.hsql [required]`
- `intellij.database.dialects.postgresbase [required]`
- `intellij.database.dialects.postgres [required]`
- `intellij.database.dialects.postgresgreenplumbase [required]`
- `intellij.database.dialects.sqlite [required]`
- `intellij.database.dialects.mysql [required]`
- `intellij.database.dialects.mysqlbase [required]`
- `intellij.database.dialects.maria [required]`
- `intellij.database.dialects.mssql [required]`
- `intellij.database.dialects.oracle [required]`
- `intellij.database.dialects.redis [required]`
- `intellij.database.dialects.redis.backend [required]`
- `intellij.database.dialects.redshift [required]`
- `intellij.database.dialects.mongo [required]`
- `intellij.database.dialects.mongo.sql [required]`
- `intellij.database.dialects.snowflake [required]`
- `intellij.database.dialects.spark [required]`
- `intellij.database.dialects.sybase [required]`
- `intellij.database.dialects.vertica [required]`
- `intellij.database.dialects.base.ex [required]`
- `intellij.database.dialects.bigquery.ex [required]`
- `intellij.database.dialects.cassandra.ex [required]`
- `intellij.database.dialects.clickhouse.ex [required]`
- `intellij.database.dialects.cockroach.ex [required]`
- `intellij.database.dialects.couchbase.ex [required]`
- `intellij.database.dialects.db2.ex [required]`
- `intellij.database.dialects.derby.ex [required]`
- `intellij.database.dialects.dynamo [required]`
- `intellij.database.dialects.generic.ex [required]`
- `intellij.database.dialects.greenplum.ex [required]`
- `intellij.database.dialects.h2.ex [required]`
- `intellij.database.dialects.postgresbase.ex [required]`
- `intellij.database.dialects.postgres.ex [required]`
- `intellij.database.dialects.postgresgreenplumbase.ex [required]`
- `intellij.database.dialects.mysqlbase.ex [required]`
- `intellij.database.dialects.mssql.ex [required]`
- `intellij.database.dialects.sqlite.ex [required]`
- `intellij.database.dialects.oracle.ex [required]`
- `intellij.database.dialects.oracle.ex.coverage`
- `intellij.database.dialects.mongo.ex [required]`
- `intellij.database.dialects.mongo.js.external`
- `intellij.database.dialects.redshift.ex [required]`
- `intellij.database.dialects.snowflake.ex [required]`
- `intellij.database.dialects.sybase.ex [required]`
- `intellij.database.dialects.vertica.ex [required]`

</details>

## 3. Дескрипторы DB/SQL/Grid (plugin, content modules, config-files)

| Дескриптор | Тип | Владелец | JAR | EP | Сервисы | Tool windows | Actions |
|---|---|---|---|---|---|---|---|
| `AllDatabaseDialects` | config | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `AllDatabaseDialectsCore` | config | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `DatabaseDialectsEx` | config | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `com.intellij.database` | plugin | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.database` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.jar` | 0 | 0 |  | 0 |
| `intellij.database.ai` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.ai.jar` | 0 | 0 |  | 0 |
| `intellij.database.backend.split` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.backend.split.jar` | 0 | 0 |  | 0 |
| `intellij.database.cliCommands` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar` | 0 | 0 |  | 0 |
| `intellij.database.cloudExplorer.jba` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.cloudExplorer.jba.jar` | 0 | 0 |  | 0 |
| `intellij.database.completionMlRanking` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.completionMlRanking.jar` | 0 | 0 |  | 0 |
| `intellij.database.connectivity` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` | 17 | 11 |  | 0 |
| `intellij.database.connectivity.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar` | 1 | 3 |  | 0 |
| `intellij.database.core.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` | 42 | 7 |  | 0 |
| `intellij.database.diagrams` | module | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 2 |
| `intellij.database.dialects.base` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.base.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.ex.jar` | 0 | 0 |  | 1 |
| `intellij.database.dialects.bigquery` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.bigquery.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.cassandra` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.cassandra.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.clickhouse` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.clickhouse.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.cockroach` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.cockroach.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.couchbase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.couchbase.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.db2` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.db2.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.derby` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.derby.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.dynamo` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.exasol` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.generic` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.generic.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.greenplum` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.greenplum.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.h2` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.h2.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.hive` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.hivebase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.hsql` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.maria` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mongo` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar` | 1 | 0 |  | 0 |
| `intellij.database.dialects.mongo.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mongo.js.external` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mongo.sql` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.sql.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mssql` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mssql.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mssql.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.impl.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mysql` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysql.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mysqlbase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mysqlbase.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.mysqlbase.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.impl.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.oracle` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.oracle.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.oracle.ex.coverage` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.coverage.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgres` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgres.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgresbase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgresbase.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgresgreenplumbase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.postgresgreenplumbase.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.redis` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.redis.backend` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.backend.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.redshift` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.redshift.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.snowflake` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.snowflake.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.ex.jar` | 0 | 0 |  | 1 |
| `intellij.database.dialects.spark` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.sql92` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sql92.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.sqlite` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.sqlite.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.sybase` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.sybase.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.vertica` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar` | 0 | 0 |  | 0 |
| `intellij.database.dialects.vertica.ex` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.ex.jar` | 0 | 0 |  | 0 |
| `intellij.database.docker` | module | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.database.flameGraphs` | module | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 1 |
| `intellij.database.frontend.split` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.frontend.split.jar` | 0 | 1 |  | 0 |
| `intellij.database.grazie` | module | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.database.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | 13 | 23 | Database | 288 |
| `intellij.database.impl.bookmarks` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.bookmarks.jar` | 0 | 0 |  | 0 |
| `intellij.database.impl.common` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.common.jar` | 0 | 0 |  | 0 |
| `intellij.database.impl.frontend` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.frontend.jar` | 0 | 0 |  | 0 |
| `intellij.database.impl.jcef` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jcef.jar` | 0 | 0 |  | 2 |
| `intellij.database.impl.navbar` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.impl.navbar.jar` | 0 | 0 |  | 0 |
| `intellij.database.java` | module | com.intellij.database | `plugins/DatabaseTools/lib/database-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.database.jdbcConsole` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar` | 0 | 0 |  | 0 |
| `intellij.database.jdbcConsole.shim` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.shim.jar` | 0 | 0 |  | 0 |
| `intellij.database.mcp` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.mcp.jar` | 0 | 0 |  | 0 |
| `intellij.database.plugin.frameworks` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.plugin.frameworks.jar` | 0 | 0 |  | 0 |
| `intellij.database.pro` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.pro.jar` | 0 | 0 | Database Changes | 0 |
| `intellij.database.sql` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` | 0 | 0 |  | 0 |
| `intellij.database.sql.backend.core` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar` | 0 | 1 |  | 0 |
| `intellij.database.sql.common.core` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.common.core.jar` | 0 | 0 |  | 0 |
| `intellij.database.sql.common.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar` | 0 | 1 |  | 0 |
| `intellij.database.sql.copyright` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.copyright.jar` | 0 | 0 |  | 0 |
| `intellij.database.sql.core.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` | 12 | 7 |  | 0 |
| `intellij.database.sql.frontend.core` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.frontend.core.jar` | 0 | 0 |  | 0 |
| `intellij.database.sql.frontend.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.frontend.impl.jar` | 0 | 0 |  | 0 |
| `intellij.database.sql.impl` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar` | 0 | 1 |  | 7 |
| `intellij.database.sql.intelliLang` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.sql.intelliLang.jar` | 0 | 0 |  | 0 |
| `intellij.database.ssh` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.ssh.jar` | 1 | 0 |  | 0 |
| `intellij.database.ssh.ui` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.ssh.ui.jar` | 0 | 0 |  | 0 |
| `intellij.database.trialPromotion.idesWithFreeTier` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.trialPromotion.idesWithFreeTier.jar` | 0 | 1 |  | 0 |
| `intellij.database.util` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.database.util.jar` | 0 | 0 |  | 0 |
| `intellij.libraries.jts.io.common` | module | com.intellij.database | `plugins/DatabaseTools/lib/modules/intellij.libraries.jts.io.common.jar` | 0 | 0 |  | 0 |
| `intellij.platform.commercial.verifier` | module | com.intellij.database | `plugins/DatabaseTools/lib/intellij.platform.commercial.verifier.jar` | 0 | 0 |  | 0 |
| `org.jetbrains.plugins.database.frontend` | plugin | com.intellij.database | `plugins/DatabaseTools/lib/frontend-split/database-frontend.jar` | 0 | 0 |  | 0 |
| `tips-database-plugin` | config | com.intellij.database | `plugins/DatabaseTools/lib/tips-database-plugin.jar` | 0 | 0 |  | 0 |
| `DGMainMenuActions` | config | com.intellij.database.ide | `plugins/datagrip-impl/lib/datagrip-impl.jar` | 0 | 0 |  | 12 |
| `com.intellij.database.ide` | plugin | com.intellij.database.ide | `plugins/datagrip-impl/lib/datagrip-impl.jar` | 0 | 12 |  | 22 |
| `intellij.platform.commercial.verifier` | module | com.intellij.database.ide | `plugins/datagrip-impl/lib/intellij.platform.commercial.verifier.jar` | 0 | 0 |  | 0 |
| `tips-datagrip` | config | com.intellij.database.ide | `plugins/datagrip-impl/lib/tips-datagrip.jar` | 0 | 0 |  | 0 |
| `intellij.platform.sqlite` | module | core (lib/) | `lib/intellij.platform.sqlite.jar` | 0 | 0 |  | 0 |
| `intellij.database.artifactsBundle` | plugin | intellij.database.artifactsBundle | `plugins/database-artifactsBundle/lib/database-artifactsBundle.jar` | 0 | 0 |  | 0 |
| `intellij.database.cloudExplorer.aws` | plugin | intellij.database.cloudExplorer.aws | `plugins/database-cloudExplorer-aws/lib/database-cloudExplorer-aws.jar` | 0 | 0 |  | 0 |
| `intellij.database.cloudExplorer.gcloud` | plugin | intellij.database.cloudExplorer.gcloud | `plugins/database-cloudExplorer-gcloud/lib/database-cloudExplorer-gcloud.jar` | 0 | 0 |  | 0 |
| `intellij.grid` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.jar` | 0 | 0 |  | 0 |
| `intellij.grid.core.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` | 3 | 1 |  | 0 |
| `intellij.grid.core.plugin` | plugin | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/grid-core-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.grid.csv.core.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.csv.core.impl.jar` | 0 | 1 |  | 0 |
| `intellij.grid.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` | 4 | 2 |  | 136 |
| `intellij.grid.impl.ide` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.ide.jar` | 0 | 0 |  | 0 |
| `intellij.grid.types` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.types.jar` | 0 | 0 |  | 0 |
| `intellij.grid.loader.json` | plugin | intellij.grid.loader.json | `plugins/grid-loader-json/lib/grid-loader-json.jar` | 0 | 0 |  | 0 |
| `intellij.grid.loader.xls` | plugin | intellij.grid.loader.xls | `plugins/grid-loader-xls/lib/grid-loader-xls.jar` | 0 | 0 |  | 0 |
| `intellij.grid.charts.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.charts.impl.jar` | 0 | 0 |  | 3 |
| `intellij.grid.images.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.images.impl.jar` | 0 | 0 |  | 0 |
| `intellij.grid.json.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.json.impl.jar` | 0 | 0 |  | 0 |
| `intellij.grid.plugin` | plugin | intellij.grid.plugin | `plugins/grid-plugin/lib/grid-plugin.jar` | 0 | 0 |  | 0 |
| `intellij.grid.scripting.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.scripting.impl.jar` | 1 | 0 |  | 0 |
| `intellij.grid.scripting.rt` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar` | 0 | 0 |  | 0 |
| `intellij.fullLine.sql` | module | org.jetbrains.completion.full.line | `plugins/fullLine/lib/modules/intellij.fullLine.sql.jar` | 0 | 0 |  | 0 |
| `intellij.ml.llm.sql.completion` | module | org.jetbrains.completion.full.line | `plugins/fullLine/lib/modules/intellij.ml.llm.sql.completion.jar` | 0 | 0 |  | 0 |

## 4. Tool windows

| ID | Factory | Дескриптор |
|---|---|---|
| `Database` | `com.intellij.database.DatabaseToolWindowFactory` | `intellij.database.impl` |
| `Database Changes` | `com.intellij.database.pro.dataSource.srcStorage.DbSrcToolWindowFactory` | `intellij.database.pro` |

## 5. Матрица повторного использования (ТЗ, раздел 9)

Колонки «Компонент» и «Модуль/JAR» заполнены автоматически по реально найденным классам. Колонка «Собственный код» — предварительное решение: «нет», если компонент найден в bundled-плагине или платформе.

| Функция | JetBrains компонент (найдено) | Модуль/JAR | Способ подключения | Собственный код |
|---|---|---|---|---|
| Data Sources | `com.intellij.database.dataSource.LocalDataSource`<br>`com.intellij.database.dataSource.LocalDataSourceManager`<br>`com.intellij.database.dataSource.DataSourceStorage` | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Driver Manager | `com.intellij.database.dataSource.DatabaseDriver`<br>`com.intellij.database.dataSource.DatabaseDriverManager`<br>`com.intellij.database.dataSource.DatabaseDriverImpl` | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Database Connection | `com.intellij.database.dataSource.DatabaseConnection`<br>`com.intellij.database.dataSource.DatabaseConnectionCore`<br>`com.intellij.database.dataSource.DatabaseConnectionManager` | `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Database Explorer | `com.intellij.database.view.DatabaseView`<br>`com.intellij.database.view.DatabaseStructure` | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| SQL Console | `com.intellij.database.console.JdbcConsole`<br>`com.intellij.database.console.JdbcConsoleProvider`<br>`com.intellij.database.console.JdbcConsoleCore` | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| SQL execution | `com.intellij.database.datagrid.DataRequest`<br>`com.intellij.database.datagrid.DataConsumer`<br>`com.intellij.database.extensions.DataConsumer` | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`<br>`plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| SQL PSI | `com.intellij.sql.psi.SqlFile`<br>`com.intellij.sql.psi.SqlLanguage`<br>`com.intellij.sql.psi.SqlElement` | `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| SQL dialects | `com.intellij.sql.dialects.SqlLanguageDialect`<br>`com.intellij.sql.dialects.SqlLanguageDialectEx`<br>`com.intellij.database.util.SqlDialects` | `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Completion | `com.intellij.sql.completion.SqlCompletionContributor` | `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Navigation | `com.intellij.database.schemaEditor.DbEditorElementReference`<br>`com.intellij.sql.dialects.dateTime.psi.SqlDtReference`<br>`com.intellij.sql.psi.SqlReference` | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Metadata / Introspection | `com.intellij.database.psi.DbPsiFacade`<br>`com.intellij.database.psi.DbElement`<br>`com.intellij.database.model.DasModel` | `plugins/DatabaseTools/lib/modules/intellij.database.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| DataGrid | `com.intellij.database.datagrid.DataGrid`<br>`com.intellij.database.datagrid.DataGridUtil`<br>`com.intellij.database.datagrid.GridModel` | `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | `bundledPlugin("intellij.grid.core.plugin")` | нет (переиспользуем) |
| Data modification | `com.intellij.database.datagrid.GridDataHookUp`<br>`com.intellij.database.run.ui.GridDataSupport`<br>`com.intellij.database.datagrid.GridMutator` | `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` | `bundledPlugin("intellij.grid.core.plugin")` | нет (переиспользуем) |
| DDL | `com.intellij.database.util.DdlBuilder`<br>`com.intellij.database.script.generator.ScriptGenerator`<br>`com.intellij.database.util.DbSqlUtil` | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Transactions | `com.intellij.database.layoutedQueries.DBTransaction`<br>`com.intellij.database.layoutedQueries.InTransaction`<br>`com.intellij.database.schemaEditor.DbModelDumbTransactionManager` | `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`<br>`plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |
| Query history | `com.intellij.database.actions.BrowseConsoleHistoryAction`<br>`com.intellij.database.console.session.DatabaseConsoleHistoryController` | `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` | `bundledPlugin("com.intellij.database")` | нет (переиспользуем) |

Обозначения: 🔒 — класс помечен `@ApiStatus.Internal`, 🧪 — `@ApiStatus.Experimental`, ⚠ — `@Deprecated`/`ScheduledForRemoval`, (impl) — пакет `.impl.`.

## 6. Кандидаты по функциям (детально)

### Data Sources

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.dataSource.LocalDataSource` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.LocalDataSource.txt)
- `com.intellij.database.dataSource.LocalDataSourceManager` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.LocalDataSourceManager.txt)
- `com.intellij.database.dataSource.DataSourceStorage` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DataSourceStorage.txt)
- `com.intellij.database.psi.DbDataSource` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.psi.DbDataSource.txt)
- `com.intellij.database.model.RawDataSource` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.model.RawDataSource.txt)
- `com.intellij.database.psi.DataSourceManager` — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.psi.DataSourceManager.txt)

Другие публичные классы по шаблону `^(Local)?DataSource(s)?(Manager|Storage|Util|Registry|Configurable)?$`:

- `com.intellij.database.autoconfig.DataSourceRegistry` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dataSource.DataSourceConfigurable` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.util.DataSourceUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`

### Driver Manager

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.dataSource.DatabaseDriver` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseDriver.txt)
- `com.intellij.database.dataSource.DatabaseDriverManager` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseDriverManager.txt)
- `com.intellij.database.dataSource.DatabaseDriverImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseDriverImpl.txt)

Не найдены в этой сборке (не использовать): `DriverManager`

Другие публичные классы по шаблону `^Database(Driver|Artifact).*(Manager|Impl)?$`:

- `com.intellij.database.ai.dataSources.DatabaseDriverUIUtilsKt` — `plugins/DatabaseTools/lib/modules/intellij.database.ai.jar`
- `com.intellij.database.ai.dataSources.DatabaseDriverUtilsKt` — `plugins/DatabaseTools/lib/modules/intellij.database.ai.jar`
- `com.intellij.database.connectivity.ex.dataSource.artifacts.DatabaseArtifactDataSourceContext` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar`
- `com.intellij.database.connectivity.ex.dataSource.artifacts.DatabaseArtifactLoaderImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar`
- `com.intellij.database.connectivity.ex.dataSource.artifacts.DatabaseArtifactProjectContext` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar`
- `com.intellij.database.dataSource.DatabaseDriverClasspathManager` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.DatabaseDriverClasspathManagerImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dataSource.DatabaseDriverListener` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.DatabaseDriverManagerImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`

### Database Connection

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.dataSource.DatabaseConnection` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseConnection.txt)
- `com.intellij.database.dataSource.DatabaseConnectionCore` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseConnectionCore.txt)
- `com.intellij.database.dataSource.DatabaseConnectionManager` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseConnectionManager.txt)
- `com.intellij.database.dataSource.DatabaseConnectionPoint` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.dataSource.DatabaseConnectionPoint.txt)

Другие публичные классы по шаблону `^Database(Connection|Session)(Manager|Core|Point|Impl)?$`:

- `com.intellij.database.console.session.DatabaseSession` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.console.session.DatabaseSession.txt)
- `com.intellij.database.console.session.DatabaseSessionManager` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.console.session.DatabaseSessionManager.txt)

### Database Explorer

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.view.DatabaseView` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.view.DatabaseView.txt)
- `com.intellij.database.view.DatabaseStructure` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.view.DatabaseStructure.txt)

Не найдены в этой сборке (не использовать): `DatabaseViewToolWindowFactory`, `DatabaseTreeStructure`

Другие публичные классы по шаблону `^Database(View|Tree|Structure).*$`:

- `com.intellij.database.actions.DatabaseViewActions` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.actions.DatabaseViewEyeGroup` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.actions.diagnostic.DatabaseTreeTweakAction` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.actions.diagnostic.DatabaseTreeTweakActionKt` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dump.DatabaseViewHandler` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.explorer.internals.DatabaseTreeTweakDialog` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.view.DatabaseTreeContext` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.view.DatabaseViewDesktopService` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.view.DatabaseViewMoreOptionsGroup` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.view.DatabaseViewOptions` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`

### SQL Console

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.console.JdbcConsole` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.console.JdbcConsole.txt)
- `com.intellij.database.console.JdbcConsoleProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.console.JdbcConsoleProvider.txt)
- `com.intellij.database.console.JdbcConsoleCore` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.console.JdbcConsoleCore.txt)
- `com.intellij.database.console.session.DatabaseSessionManager` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.console.session.DatabaseSessionManager.txt)
- `com.intellij.database.console.session.DatabaseSession` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.console.session.DatabaseSession.txt)

Другие публичные классы по шаблону `^(Jdbc)?Console(Provider|Core|Manager|Runner|Util)?$|^JdbcConsole.*$`:

- `com.intellij.database.console.JdbcConsoleBase` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.console.JdbcConsoleDataSourceProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.console.JdbcConsoleProviderCore` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.console.JdbcConsoleRunContext` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.console.JdbcConsoleService` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.console.JdbcConsoleServiceImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.console.JdbcConsoleUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.console.evaluation.JdbcConsoleEvaluationSupport` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.run.audit.JdbcConsoleAuditor` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`

### SQL execution

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.datagrid.DataRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataRequest.txt)
- `com.intellij.database.datagrid.DataConsumer` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataConsumer.txt)
- `com.intellij.database.extensions.DataConsumer` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.extensions.DataConsumer.txt)

Не найдены в этой сборке (не использовать): `SqlStatementRunner`, `ScriptExecutor`

Другие публичные классы по шаблону `^.*(Request|Execution)(Executor|Runner|Engine|Manager)?$`:

- `com.intellij.database.console.evaluation.EvaluationRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.datagrid.ActualGridCellRequest` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.FixedGridCellRequest` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridCellRequest` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridDataRequest` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.debugger.SqlDebugAuxiliaryRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dump.DumpRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.model.migration.DbMigrationRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.pro.settings.DatabaseSettingsQueryExecution` — `plugins/DatabaseTools/lib/modules/intellij.database.pro.jar`
- `com.intellij.database.run.ConsoleDataRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.schemaEditor.PropertyModelRequest` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`

### SQL PSI

Точные совпадения с подсказками ТЗ:

- `com.intellij.sql.psi.SqlFile` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.psi.SqlFile.txt)
- `com.intellij.sql.psi.SqlLanguage` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.psi.SqlLanguage.txt)
- `com.intellij.sql.psi.SqlElement` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.psi.SqlElement.txt)
- `com.intellij.database.remote.jdba.sql.SqlStatement` — `plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar` — [сигнатуры](research/api/com.intellij.database.remote.jdba.sql.SqlStatement.txt)
- `com.intellij.sql.psi.SqlStatement` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.psi.SqlStatement.txt)
- `com.intellij.sql.psi.SqlQueryExpression` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.psi.SqlQueryExpression.txt)

Другие публичные классы по шаблону `^Sql(File|Language|Statement|ElementTypes|TokenType)$`:

- `com.intellij.sql.psi.SqlElementTypes` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.psi.SqlTokenType` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar`

### SQL dialects

Точные совпадения с подсказками ТЗ:

- `com.intellij.sql.dialects.SqlLanguageDialect` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar` — [сигнатуры](research/api/com.intellij.sql.dialects.SqlLanguageDialect.txt)
- `com.intellij.sql.dialects.SqlLanguageDialectEx` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` — [сигнатуры](research/api/com.intellij.sql.dialects.SqlLanguageDialectEx.txt)
- `com.intellij.database.util.SqlDialects` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.util.SqlDialects.txt)
- `com.intellij.database.dialects.postgres.PgDialect` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar` — [сигнатуры](research/api/com.intellij.database.dialects.postgres.PgDialect.txt)
- `com.intellij.sql.dialects.postgres.PgDialect` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar` — [сигнатуры](research/api/com.intellij.sql.dialects.postgres.PgDialect.txt)
- `com.intellij.database.dialects.postgresbase.PgDialectBase` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar` — [сигнатуры](research/api/com.intellij.database.dialects.postgresbase.PgDialectBase.txt)
- `com.intellij.sql.dialects.postgres.PgDialectBase` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar` — [сигнатуры](research/api/com.intellij.sql.dialects.postgres.PgDialectBase.txt)
- `com.intellij.database.Dbms` — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.Dbms.txt)

Другие публичные классы по шаблону `^(Sql.*Dialect.*|Pg.*Dialect.*|Dbms)$`:

- `com.intellij.database.dialects.postgresbase.introspector.jdbc.PgDialectHelper` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar`
- `com.intellij.database.dialects.sqlite.SqliteDialect` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar`
- `com.intellij.database.dialects.sqlite.sql.SqliteDialect` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar`
- `com.intellij.sql.database.SqlDataSourceDialectComponent` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`
- `com.intellij.sql.dialects.SqlDialectImplUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`
- `com.intellij.sql.dialects.SqlDialectImplUtilCore` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.dialects.SqlDialectMappings` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.dialects.SqlDialectsConfigurable` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`

### Completion

Точные совпадения с подсказками ТЗ:

- `com.intellij.sql.completion.SqlCompletionContributor` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar` — [сигнатуры](research/api/com.intellij.sql.completion.SqlCompletionContributor.txt)

Не найдены в этой сборке (не использовать): `SqlCompletionProvider`

Другие публичные классы по шаблону `^Sql.*Completion.*$`:

- `com.intellij.sql.completion.SqlCompletionScopeProcessor` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.SqlCompletionUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.SqlKeywordCompletionContributor` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.SqlWordCompletionFilter` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`
- `com.intellij.sql.completion.options.SqlCodeCompletionConfigurable` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`
- `com.intellij.sql.completion.options.SqlCodeCompletionSettings` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.providers.SqlBinaryExpressionCompletionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.providers.SqlColumnListCompletionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.providers.SqlCompletionProviderBase` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.providers.SqlExperimentalNameCompletionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.completion.providers.SqlFunctionDefinitionCompletions` — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`

### Navigation

Точные совпадения с подсказками ТЗ:

- нет

Не найдены в этой сборке (не использовать): `SqlReferenceProvider`, `DbNavigationUtil`, `SqlGotoDeclarationHandler`

Другие публичные классы по шаблону `^(Sql|Db).*(Navigation|GotoDeclaration|Reference)(Util|Handler|Provider)?$`:

- `com.intellij.database.schemaEditor.DbEditorElementReference` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.sql.dialects.dateTime.psi.SqlDtReference` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `com.intellij.sql.psi.SqlReference` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar`
- `com.intellij.sql.psi.impl.SqlPositionalReference` (impl) — `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`

### Metadata / Introspection

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.psi.DbPsiFacade` — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.psi.DbPsiFacade.txt)
- `com.intellij.database.psi.DbElement` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.psi.DbElement.txt)
- `com.intellij.database.model.DasModel` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.model.DasModel.txt)
- `com.intellij.database.model.DasTable` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.model.DasTable.txt)
- `com.intellij.database.model.DasColumn` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.jar` — [сигнатуры](research/api/com.intellij.database.model.DasColumn.txt)
- `com.intellij.database.model.basic.BasicModel` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.model.basic.BasicModel.txt)

Не найдены в этой сборке (не использовать): `Introspector`

Другие публичные классы по шаблону `^(Das(Model|Table|Column|Object)|Db(PsiFacade|Element)|.*Introspect(or|ion).*)$`:

- `com.intellij.database.actions.diagnostic.PrepareIntrospectionDiagnostic` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.ai.IntrospectionHelpersKt` — `plugins/DatabaseTools/lib/modules/intellij.database.ai.jar`
- `com.intellij.database.dataSource.DatabaseIntrospectionScheduleManager` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dataSource.DatabaseIntrospectionScheduleManagerKt` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dataSource.DesktopRegularIntrospectionContext` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dataSource.IntrospectionScheduleOptionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dataSource.JdbcIntrospectorNoEscapingOptionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.JdbcIntrospectorOptionProvider` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.RegularIntrospectionContext` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `com.intellij.database.dialects.base.introspector.BaseIntrospectionFunctions` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.introspector.BaseIntrospector` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.introspector.BaseMultiDatabaseIntrospector` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`

### DataGrid

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.datagrid.DataGrid` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataGrid.txt)
- `com.intellij.database.datagrid.DataGridUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataGridUtil.txt)
- `com.intellij.database.datagrid.GridModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridModel.txt)
- `com.intellij.database.datagrid.GridColumn` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridColumn.txt)
- `com.intellij.database.datagrid.GridRow` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridRow.txt)
- `com.intellij.database.datagrid.GridUtil` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridUtil.txt)
- `com.intellij.database.run.ui.TableResultPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.run.ui.TableResultPanel.txt)
- `com.intellij.database.datagrid.GridHelper` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridHelper.txt)

Другие публичные классы по шаблону `^(DataGrid.*|Grid(Model|Column|Row|Util|Helper|Panel).*)$`:

- `com.intellij.database.DataGridBundle` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.console.GridColumnsManager` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.console.GridColumnsManagerFactory` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.datagrid.DataGridAppearance` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.DataGridCellTypeListener` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.DataGridListModel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.DataGridListener` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.DataGridNotifications` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.DataGridPomTarget` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.DataGridSessionClient` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.datagrid.DataGridStartupActivity` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Data modification

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.datagrid.GridDataHookUp` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridDataHookUp.txt)
- `com.intellij.database.run.ui.GridDataSupport` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.run.ui.GridDataSupport.txt)
- `com.intellij.database.datagrid.GridMutator` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridMutator.txt)
- `com.intellij.database.datagrid.DatabaseMutator` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DatabaseMutator.txt)

Другие публичные классы по шаблону `^(Grid(DataHookUp|DataSupport|Mutat).*|.*Mutator)$`:

- `com.intellij.database.datagrid.GridDataHookUpBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridDataHookUpManager` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.TypesMutator` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridDataSupportImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridMutationModel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`

### DDL

Точные совпадения с подсказками ТЗ:

- `com.intellij.database.util.DdlBuilder` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.util.DdlBuilder.txt)
- `com.intellij.database.script.generator.ScriptGenerator` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.script.generator.ScriptGenerator.txt)
- `com.intellij.database.util.DbSqlUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.util.DbSqlUtil.txt)

Не найдены в этой сборке (не использовать): `SqlScriptGenerator`

Другие публичные классы по шаблону `^(Ddl.*|.*ScriptGenerator.*)$`:

- `com.intellij.database.actions.DdlActions` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dataSource.DdlMapping` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.DdlMappingConfigurable` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dataSource.DdlMappingLink` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dataSource.DdlMappingsManager` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.dataSource.validation.DdlMappingSourceValidator` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.dialects.base.generator.AbstractScriptGenerator` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.generator.AbstractScriptGeneratorKt` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.generator.ScriptGeneratorDiagnosticListener` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.generator.ScriptGeneratorDiagnosticTool` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.ex.jar`
- `com.intellij.database.dialects.base.generator.ScriptGeneratorHelper` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `com.intellij.database.dialects.base.generator.ScriptGeneratorHelperKt` — `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`

### Transactions

Точные совпадения с подсказками ТЗ:

- нет

Не найдены в этой сборке (не использовать): `DatabaseTransactionManager`, `TransactionController`, `TxController`

Другие публичные классы по шаблону `^.*Transaction(Manager|Controller|Mode|Isolation)?$`:

- `com.intellij.database.layoutedQueries.DBTransaction` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.layoutedQueries.InTransaction` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.schemaEditor.DbModelDumbTransactionManager` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.schemaEditor.DbModelTransactionManager` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`

### Query history

Точные совпадения с подсказками ТЗ:

- нет

Не найдены в этой сборке (не использовать): `QueryHistory`, `ConsoleHistoryModel`, `SqlHistory`

Другие публичные классы по шаблону `^.*(Query|Console|Sql)History.*$`:

- `com.intellij.database.actions.BrowseConsoleHistoryAction` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.console.session.DatabaseConsoleHistoryController` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`

## 7. Extension points DB/SQL/Grid

| EP | Interface/Bean | Area | Dynamic | Дескриптор |
|---|---|---|---|---|
| `com.intellij.database.queryParametersProvider` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.dataConsumer` | `com.intellij.database.datagrid.DataConsumer` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.dataProducer` | `com.intellij.database.datagrid.DataProducer` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.queryValidator` | `com.intellij.database.console.DbQueryValidator` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.consoleRunContextParametersTuner` | `com.intellij.database.run.ConsoleRunContextParametersTuner` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.toDatabaseScriptTranslator` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.jdbcSourceLoader` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.jdbcMetadataWrapper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.gridHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.errorProvider` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.jdbcHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.objectEditorFactory` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.objectEditorModelFactory` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.remoteProcessInitializer` | `com.intellij.database.dataSource.RemoteProcessInitializer` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.connectionExtraParamProvider` | `com.intellij.database.console.ConnectionExtraParamProvider` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.sshTunnelsProvider` | `com.intellij.database.console.ssh.DatabaseSshTunnelsProvider` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.localArtifactStorage` | `com.intellij.database.dataSource.artifacts.LocalArtifactStorage` | APPLICATION | да | `intellij.database.connectivity` |
| `com.intellij.database.artifactRepositoriesProvider` | `com.intellij.database.connectivity.ex.dataSource.artifacts.ArtifactRepositoriesProvider` | APPLICATION | да | `intellij.database.connectivity.ex` |
| `com.intellij.database.dbms` | `com.intellij.database.Dbms$DbmsBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.gridColumnsManagerFactory` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.optionProvider` | `com.intellij.database.dataSource.DbOptionProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.dataSourceDetector` | `com.intellij.database.autoconfig.DataSourceDetector` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.urlEditorInspector` | `com.intellij.database.dataSource.url.ui.UrlEditorInspector` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.dataSourceManager` | `com.intellij.database.psi.DataSourceManager` | PROJECT | да | `intellij.database.core.impl` |
| `com.intellij.database.consoleProvider` | `com.intellij.database.script.PersistenceConsoleProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.modelRelationProvider` | `com.intellij.database.model.ModelRelationManager$ModelRelationProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.synchronizeHandler` | `com.intellij.database.SynchronizeHandler` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.dataAuditor` | `com.intellij.database.datagrid.DataAuditor` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.parameterPatternProvider` | `com.intellij.database.settings.DatabaseParameterPatternProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.urlParamEditorProvider` | `com.intellij.database.dataSource.url.TypesRegistry$TypeDescriptorFactory` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.processParamProvider` | `com.intellij.database.run.ConsoleConfigurationParamProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.connectionInterceptor` | `com.intellij.database.dataSource.DatabaseConnectionInterceptor` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.sshConfigurationProvider` | `com.intellij.database.dataSource.DataSourceSshConfigurationProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.driversConfig` | `com.intellij.database.dataSource.ConfigUrlBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.artifactsConfig` | `com.intellij.database.dataSource.ConfigUrlBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.modelExternalData` | `com.intellij.database.dataSource.ConfigUrlBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.addToHSet` | `com.intellij.database.HSet$HSetBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.extensionFallback` | `com.intellij.database.DbmsExtension$FallbackBean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.virtualFileDataSourceProvider` | `com.intellij.database.util.VirtualFileDataSourceProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.introspectorStatsProvider` | `com.intellij.database.introspection.DBIntrospectorStatsProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.runtimeErrorFixProvider` | `com.intellij.database.connection.throwable.info.RuntimeErrorActionProvider` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.definitionProvider` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.scriptGenerator` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.dmlHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.hookUpHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.introspector` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.modelFacade` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.routineExecutionHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.explainPlanProvider` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.dataImporter` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.domainRegistry` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.namingService` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.typeSystem` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.sqlObjectBuilder` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.executionEnvironmentHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.errorHandler` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.geoHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.linkedDataSourceHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.databaseViewStructureExtension` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.predicatesHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.core.impl` |
| `com.intellij.database.mongo.resolveHelper` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.dialects.mongo` |
| `com.intellij.database.debuggerFacade` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.schemaDiffCustomization` | `com.intellij.database.model.diff.SchemaDiffCustomization` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.configValidator` | `com.intellij.database.dataSource.validation.DatabaseConfigValidator` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.selectInProvider` | `com.intellij.database.view.SelectInDatabaseView$Extension` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.urlParamEditorUiProvider` | `com.intellij.database.dataSource.url.TypesRegistryUi$TypeDescriptorUiFactory` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.runConsoleAvailable` | `com.intellij.database.intentions.RunQueryIntentionActionAvailable` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.cli.runTargetProvider` | `com.intellij.database.cli.CliRunTargetProvider` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.activeConnectionInfoProvider` | `com.intellij.database.view.DbActiveConnectionInfoProvider` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.cloudDataSourceProvider` | `com.intellij.util.KeyedLazyInstanceEP` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.cloudCommunicatorProvider` | `com.intellij.util.KeyedLazyInstanceEP` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.sshPanelProvider` | `com.intellij.database.dataSource.DataSourceSshPanelBridgeProvider` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.explorer.decoration` | `com.intellij.database.explorer.structure.DvDecorationExtension` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.database.dbDocumentationDelegate` | `com.intellij.database.psi.documentation.DbDocumentationDelegate` | APPLICATION | да | `intellij.database.impl` |
| `com.intellij.sql.dialect` | `com.intellij.database.DbmsExtension$InstanceBean` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.database.dialect` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.database.sqlEffectAnalyzer` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.dialectCodeStyleProvider` | `com.intellij.sql.formatter.SqlDialectCodeStyleProvider` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.navigationHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.membersHelper` | `com.intellij.database.DbmsExtension$Bean` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.evaluationHelper` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.formatterHelper` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.executionFlowAnalyzerProvider` | `com.intellij.lang.LanguageExtensionPoint` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.resolveExtension` | `com.intellij.sql.psi.impl.SqlResolveExtension` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.inspectionSuppressorDelegate` | `com.intellij.sql.inspections.suppression.SqlInspectionSuppressorDelegate` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.sql.dataSourceProvider` | `com.intellij.sql.psi.impl.DataSourceProvider` | APPLICATION | да | `intellij.database.sql.core.impl` |
| `com.intellij.database.sshCredentialsProvider` | `com.intellij.database.ssh.DatabaseSshCredentialsProvider` | APPLICATION | да | `intellij.database.ssh` |
| `com.intellij.database.datagrid.objectNormalizerProvider` | `com.intellij.database.datagrid.ObjectNormalizerProvider` | APPLICATION | да | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.formatterCreatorProvider` | `com.intellij.database.datagrid.FormatterCreatorProvider` | APPLICATION | да | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.extractorsHelper` | `com.intellij.database.extractors.ExtractorsHelper` | APPLICATION | да | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.valueEditorTab` | `com.intellij.database.run.ui.ValueEditorTab` | APPLICATION | да | `intellij.grid.impl` |
| `com.intellij.database.datagrid.cellViewerFactory` | `com.intellij.database.run.ui.CellViewerFactory` | APPLICATION | да | `intellij.grid.impl` |
| `com.intellij.database.minimizedFormatDetector` | `com.intellij.database.run.ui.MinimizedFormatDetector` | APPLICATION | да | `intellij.grid.impl` |
| `com.intellij.database.datagrid.customToolbarProvider` | `com.intellij.database.datagrid.CustomGridToolbarProvider` | APPLICATION | да | `intellij.grid.impl` |
| `com.intellij.grid.scripting.ivyLocalRepository` | `com.intellij.grid.scripting.impl.IvyLocalRepository` | APPLICATION | да | `intellij.grid.scripting.impl` |

## 8. Сервисы DB/SQL/Grid

| Уровень | Интерфейс | Реализация | Дескриптор |
|---|---|---|---|
| application | `com.intellij.database.util.DocumentationBuilderService` | `com.intellij.database.util.DefaultDocumentationBuilderService` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.DataSourceModelStorage` | `com.intellij.database.dataSource.DataSourceModelStorageImpl$App` | `intellij.database.connectivity` |
| project | `com.intellij.database.dataSource.DataSourceModelStorage` | `com.intellij.database.dataSource.DataSourceModelStorageImpl$Prj` | `intellij.database.connectivity` |
| project | `com.intellij.database.console.JdbcDriverManager` | `com.intellij.database.console.JdbcDriverManagerImpl` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.DatabaseDriverManager` | `com.intellij.database.dataSource.DatabaseDriverManagerImpl` | `intellij.database.connectivity` |
| project | `—` | `com.intellij.database.dataSource.srcStorage.backend.DbSrcStorageManager` | `intellij.database.connectivity` |
| application | `—` | `com.intellij.database.dataSource.srcStorage.backend.DbSrcStorageManager$App` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.connection.statements.SmartStatementFactoryService` | `com.intellij.database.dataSource.connection.statements.SmartStatementFactoryServiceImpl` | `intellij.database.connectivity` |
| application | `com.intellij.database.access.DatabaseCredentials` | `com.intellij.database.access.DatabaseCredentialsImpl` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.srcStorage.DbSrcModelStorageService` | `com.intellij.database.dataSource.srcStorage.DbSrcModelStorageServiceImpl` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.DBConnectionAccessibilityMatchingService` | `com.intellij.database.dataSource.DataSourceConnectionAccessibilityMatchingService` | `intellij.database.connectivity` |
| application | `com.intellij.database.dataSource.artifacts.DatabaseArtifactLoader` | `com.intellij.database.connectivity.ex.dataSource.artifacts.DatabaseArtifactLoaderImpl` | `intellij.database.connectivity.ex` |
| project | `com.intellij.database.dataSource.artifacts.DatabaseArtifactContext` | `com.intellij.database.connectivity.ex.dataSource.artifacts.DatabaseArtifactProjectContext` | `intellij.database.connectivity.ex` |
| project | `com.intellij.database.dataSource.DatabaseDriverClasspathManager` | `com.intellij.database.dataSource.DatabaseDriverClasspathManagerImpl` | `intellij.database.connectivity.ex` |
| application | `—` | `com.intellij.database.settings.DatabaseSettings` | `intellij.database.core.impl` |
| application | `com.intellij.database.types.DasTypeFacade` | `com.intellij.database.types.DasTypeFacadeImpl` | `intellij.database.core.impl` |
| application | `com.intellij.database.dataSource.DataSourceStorage` | `com.intellij.database.dataSource.DataSourceStorage$App` | `intellij.database.core.impl` |
| application | `com.intellij.database.dataSource.DataSourceStorageShared` | `com.intellij.database.dataSource.DataSourceStorageShared$App` | `intellij.database.core.impl` |
| project | `com.intellij.database.dataSource.DataSourceStorage` | `com.intellij.database.dataSource.DataSourceStorage$Prj` | `intellij.database.core.impl` |
| project | `com.intellij.database.dataSource.DataSourceStorageShared` | `com.intellij.database.dataSource.DataSourceStorageShared$Prj` | `intellij.database.core.impl` |
| project | `com.intellij.database.psi.DbPsiFacade` | `com.intellij.database.psi.DbPsiFacadeImpl` | `intellij.database.core.impl` |
| application | `com.intellij.database.console.JdbcConsoleService` | `com.intellij.database.frontend.split.database.console.FakeFrontendJdbcConsoleService` | `intellij.database.frontend.split` |
| application | `com.intellij.database.dialects.DatabaseFixFactory` | `com.intellij.database.dialects.DatabaseFixFactoryImpl` | `intellij.database.impl` |
| application | `com.intellij.database.dataSource.srcStorage.DbSrcChangesTracker` | `com.intellij.database.dataSource.srcStorage.DbSrcChangesTrackerApplication` | `intellij.database.impl` |
| application | `—` | `com.intellij.database.model.NameBasedRelationProvider$NameBasedRelationSettings` | `intellij.database.impl` |
| project | `com.intellij.database.dataSource.srcStorage.DbSrcChangesTracker` | `com.intellij.database.dataSource.srcStorage.DbSrcChangesTrackerProject` | `intellij.database.impl` |
| application | `com.intellij.database.dataSource.srcStorage.DbSrcChangesService` | `com.intellij.database.dataSource.srcStorage.DbSrcChangesServiceImpl` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.psi.DbFindUsagesOptionsProvider` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.view.DatabaseView` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.actions.DumpToDdlDataSourceFileManager` | `intellij.database.impl` |
| application | `com.intellij.database.psi.DbElementTextProvider` | `com.intellij.database.psi.DbElementTextProviderImpl` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.datagrid.DbGridDataHookUpManager` | `intellij.database.impl` |
| application | `com.intellij.database.vfs.DbVFSUtils` | `com.intellij.database.vfs.DbVFSUtilsImpl` | `intellij.database.impl` |
| project | `com.intellij.database.console.ConsolesMigrationManager` | `com.intellij.database.console.migration.ConsolesMigrationManagerImpl` | `intellij.database.impl` |
| project | `com.intellij.database.view.DatabaseViewService` | `com.intellij.database.view.DatabaseViewDesktopService` | `intellij.database.impl` |
| project | `com.intellij.database.explorer.structure.DvForestActionService` | `com.intellij.database.explorer.structure.DvForestActionServiceImpl` | `intellij.database.impl` |
| project | `com.intellij.database.console.DataSourcePerFileMappings` | `com.intellij.database.dataSource.DataSourcePerFileMappingsImpl` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.diff.TableDiffSettingsHolder` | `intellij.database.impl` |
| project | `—` | `com.intellij.database.dataSource.srcStorage.DbSrcMapping` | `intellij.database.impl` |
| application | `com.intellij.database.console.JdbcConsoleService` | `com.intellij.database.console.JdbcConsoleServiceImpl` | `intellij.database.impl` |
| application | `com.intellij.database.view.DatabaseCoreUiService` | `com.intellij.database.view.DatabaseCoreUiServiceImpl` | `intellij.database.impl` |
| application | `com.intellij.database.view.DatabaseUiService` | `com.intellij.database.view.DatabaseUiServiceImpl` | `intellij.database.impl` |
| application | `com.intellij.database.console.session.DatabaseSessionViewService` | `com.intellij.database.console.session.DatabaseSessionViewServiceImpl` | `intellij.database.impl` |
| application | `com.intellij.database.csv.CsvSettingsService` | `com.intellij.database.csv.DatabaseCsvSettingsService` | `intellij.database.impl` |
| project | `com.intellij.database.dataSource.RegularIntrospectionContext` | `com.intellij.database.dataSource.DesktopRegularIntrospectionContext` | `intellij.database.impl` |
| project | `com.intellij.sql.psi.ExecutionFlowAnalyzer` | `com.intellij.database.sql.backend.core.SqlGeneralExecutionFlowAnalyzer` | `intellij.database.sql.backend.core` |
| application | `com.intellij.sql.formatter.settings.SqlCodeStyleProviderService` | `com.intellij.database.sql.common.impl.formatter.settings.SqlCodeStyleProviderServiceImpl` | `intellij.database.sql.common.impl` |
| application | `—` | `com.intellij.sql.editor.SqlEditorOptions` | `intellij.database.sql.core.impl` |
| application | `—` | `com.intellij.sql.editor.SqlEditorTabsSettings` | `intellij.database.sql.core.impl` |
| application | `—` | `com.intellij.sql.completion.options.SqlCodeCompletionSettings` | `intellij.database.sql.core.impl` |
| project | `—` | `com.intellij.sql.dialects.SqlResolveMappings` | `intellij.database.sql.core.impl` |
| project | `—` | `com.intellij.sql.dialects.SqlDataSourceMappings` | `intellij.database.sql.core.impl` |
| application | `—` | `com.intellij.sql.editor.SqlFoldingSettings` | `intellij.database.sql.core.impl` |
| project | `com.intellij.sql.psi.SqlPsiFacade` | `com.intellij.sql.psi.SqlPsiFacadeImpl` | `intellij.database.sql.core.impl` |
| application | `com.intellij.database.SqlUiService` | `com.intellij.sql.SqlUiServiceImpl` | `intellij.database.sql.impl` |
| project | `com.intellij.database.util.DbePromoProvider` | `com.intellij.database.trialPromotion.idesWithFreeTier.DbePromoProviderImpl` | `intellij.database.trialPromotion.idesWithFreeTier` |
| application | `com.intellij.openapi.fileEditor.impl.EditorEmptyTextPainter` | `com.intellij.database.ide.DataGripEditorEmptyTextPainter` | `com.intellij.database.ide` |
| application | `com.intellij.openapi.module.ModuleTypeManager` | `com.intellij.database.ide.DatabaseModuleTypeManager` | `com.intellij.database.ide` |
| project | `com.intellij.openapi.wm.ex.WelcomeScreenTabService` | `com.intellij.database.ide.welcomeScreen.DataGripWelcomeScreenTabService` | `com.intellij.database.ide` |
| project | `com.intellij.ide.projectView.ProjectView` | `com.intellij.database.ide.DataGripProjectView` | `com.intellij.database.ide` |
| project | `com.intellij.psi.search.ProjectScopeBuilder` | `com.intellij.database.ide.DataGripProjectScopeBuilder` | `com.intellij.database.ide` |
| application | `com.intellij.ui.IdeUICustomization` | `com.intellij.database.ide.DataGripIdeUICustomization` | `com.intellij.database.ide` |
| application | `com.intellij.platform.ide.core.customization.ProjectLifecycleUiCustomization` | `com.intellij.database.ide.DataGripProjectLifecycleUiCustomization` | `com.intellij.database.ide` |
| application | `com.intellij.platform.ide.customization.ExternalProductResourceUrls` | `com.intellij.database.ide.DataGripExternalResourceUrls` | `com.intellij.database.ide` |
| application | `com.intellij.openapi.updateSettings.UpdateStrategyCustomization` | `com.intellij.openapi.updateSettings.base.ShowWhatIsNewPageAfterUpdateCustomization` | `com.intellij.database.ide` |
| application | `com.intellij.openapi.ui.DialogBackgroundImageProvider` | `com.intellij.database.ide.DataGripDialogBackgroundImageProvider` | `com.intellij.database.ide` |
| application | `com.intellij.ide.actions.searcheverywhere.TabsCustomizationStrategy` | `com.intellij.database.ide.DataGripTabCustomisationStrategy` | `com.intellij.database.ide` |
| application | `com.intellij.codeInsight.template.TemplateGroupOrderProvider` | `com.intellij.database.ide.DataGripTemplateGroupOrderProvider` | `com.intellij.database.ide` |
| application | `—` | `com.intellij.database.settings.DataGridAppearanceSettingsImpl` | `intellij.grid.core.impl` |
| application | `—` | `com.intellij.database.settings.CsvSettings` | `intellij.grid.csv.core.impl` |
| application | `com.intellij.database.extensions.ExtensionsService` | `com.intellij.database.extensions.ExtensionsServiceImpl` | `intellij.grid.impl` |
| project | `—` | `com.intellij.database.datagrid.GridDataHookUpManager` | `intellij.grid.impl` |

## 9. Пакеты com.intellij.database.* / com.intellij.sql.*

| Пакет | Классов | public | 🔒 Internal | JAR |
|---|---|---|---|---|
| `com.intellij.database` | 28 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.access` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.actions` | 124 | 121 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.actions.ddl` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.actions.diagnostic` | 15 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.actions.util` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.ai` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.ai.consent` | 5 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.ai.dataSources` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.ai.queryProcessing` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.ai.safety` | 3 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.ai.settings` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ai.jar |
| `com.intellij.database.artifactsBundle` | 4 | 4 | 0 | plugins/database-artifactsBundle/lib/database-artifactsBundle.jar |
| `com.intellij.database.autoconfig` | 7 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.cli` | 29 | 29 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.argument` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.bcp` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.component` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.dump` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.dump.mysql` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.dump.pg` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.lexer` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.restore` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.restore.mysql` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cli.restore.pg` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cliCommands.dataSources` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar |
| `com.intellij.database.cliCommands.dataSources.actions` | 16 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar |
| `com.intellij.database.cliCommands.dataSources.commands` | 12 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar |
| `com.intellij.database.cliCommands.dataSources.statistics` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar |
| `com.intellij.database.cloud.explorer` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cloud.jba` | 11 | 11 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.cloudExplorer.aws` | 18 | 18 | 0 | plugins/database-cloudExplorer-aws/lib/database-cloudExplorer-aws.jar |
| `com.intellij.database.cloudExplorer.gcloud` | 16 | 16 | 0 | plugins/database-cloudExplorer-gcloud/lib/database-cloudExplorer-gcloud.jar |
| `com.intellij.database.cloudExplorer.jba` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.cloudExplorer.jba.jar |
| `com.intellij.database.connection.throwable` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.connection.throwable.info` | 8 | 7 | 1 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.connectivity.dataSource` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.connectivity.ex.dataSource.artifacts` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar |
| `com.intellij.database.console` | 64 | 61 | 3 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.console.client` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.console.evaluation` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.migration` | 8 | 8 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.migration.dialog` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.migration.view` | 9 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.queryFiles` | 7 | 5 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.runConfiguration` | 11 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.console.session` | 27 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.console.ssh` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.csv` | 17 | 17 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.csv.core.impl.jar |
| `com.intellij.database.csv.ui` | 8 | 8 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.csv.ui.preview` | 5 | 5 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.data.types` | 51 | 51 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.data.types.domain` | 34 | 34 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource` | 153 | 137 | 6 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dataSource.artifacts` | 7 | 7 | 3 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.dataSource.connection` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.connection.audit` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.connection.statements` | 50 | 50 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.dataSource.history` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.dataSource.srcStorage` | 31 | 29 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.srcStorage.backend` | 8 | 8 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.dataSource.ui` | 12 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.url` | 12 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dataSource.url.template` | 19 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.url.ui` | 39 | 28 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dataSource.validation` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.datagrid` | 159 | 158 | 1 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.color` | 10 | 10 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.mutating` | 14 | 14 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.datagrid.nested` | 1 | 1 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.objects` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.dbimport` | 36 | 34 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.dbimport.csv` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.editor` | 16 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.editor.data` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.editor.editor` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.editor.model.applier` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.editor.model.state` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.scripted` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dbimport.ui.tree` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.debugger` | 22 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.diagnostic` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.diagram` | 8 | 8 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.diagram.actions` | 5 | 4 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.diagram.plan` | 6 | 6 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.dialects` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.dialects.base` | 24 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.ex.jar |
| `com.intellij.database.dialects.base.generator` | 52 | 49 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.ex.jar |
| `com.intellij.database.dialects.base.generator.dml` | 12 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.generator.producers` | 111 | 111 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.introspector` | 26 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.introspector.jdbc` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.introspector.jdbc.wrappers` | 15 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.plan` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.base.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.database.dialects.bigquery` | 29 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.ex.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.generator.dml` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.generator.producers` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.introspector` | 5 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.model` | 22 | 21 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.model.properties` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.bigquery.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.database.dialects.cassandra` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.generator.dml` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.generator.producers` | 46 | 46 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.introspector` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.model` | 29 | 28 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.ex.jar |
| `com.intellij.database.dialects.cassandra.model.defaults` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.model.properties` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.cassandra.schemaEditor.model.applier` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.ex.jar |
| `com.intellij.database.dialects.cassandra.schemaEditor.ui` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.ex.jar |
| `com.intellij.database.dialects.cassandra.types` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.database.dialects.clickhouse` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.generator.dml` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.generator.producers` | 32 | 32 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.model` | 25 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.ex.jar |
| `com.intellij.database.dialects.clickhouse.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.clickhouse.types` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.database.dialects.cockroach` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.cockroach.generator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.cockroach.generator.producers` | 32 | 32 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.cockroach.introspector` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.cockroach.model` | 24 | 23 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.cockroach.model.properties` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.database.dialects.couchbase` | 11 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.ex.jar |
| `com.intellij.database.dialects.couchbase.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.generator.dml` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.generator.producers` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.model` | 16 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.model.defaults` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.couchbase.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.database.dialects.db2` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.generator` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.generator.producers` | 71 | 71 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.introspector` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.model` | 52 | 51 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.ex.jar |
| `com.intellij.database.dialects.db2.model.properties` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.plan` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.db2.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.database.dialects.derby` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.generator.dml` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.generator.producers` | 40 | 40 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.model` | 30 | 29 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.ex.jar |
| `com.intellij.database.dialects.derby.plan` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.derby.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.database.dialects.dynamo` | 11 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.generator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.generator.dml` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.generator.producers` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.introspector` | 3 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.model` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.dynamo.model.properties` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.database.dialects.exasol` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.generator.producers` | 50 | 50 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.introspector` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.model` | 35 | 34 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.model.properties` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.exasol.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.database.dialects.generic` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.execution` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.generator.producers` | 41 | 41 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.introspector.jdbc` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.model` | 28 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.naming` | 16 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.generic.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.database.dialects.greenplum` | 8 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.generator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.generator.producers` | 24 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.model` | 53 | 52 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.model.properties` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.greenplum.plan` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.database.dialects.h2` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.generator.producers` | 57 | 57 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.model` | 37 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.ex.jar |
| `com.intellij.database.dialects.h2.model.properties` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.plan` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.database.dialects.h2.ui` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.ex.jar |
| `com.intellij.database.dialects.hive` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hive.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hive.generator.producers` | 22 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hive.introspector` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hive.model` | 28 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hive.plan` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar |
| `com.intellij.database.dialects.hivebase` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.generator` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.generator.producers` | 23 | 23 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.model` | 16 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.model.properties` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hivebase.types` | 8 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.database.dialects.hsql` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.generator.producers` | 43 | 43 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.model` | 26 | 25 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.plan` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.hsql.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.database.dialects.maria` | 5 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.maria.generator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.maria.generator.producers` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.maria.introspector` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.maria.model` | 25 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.maria.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.database.dialects.mongo` | 28 | 26 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.generator.dml` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.generator.producers` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.model` | 14 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression` | 15 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression.function` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression.hard` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression.literal` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression.literal.primitive` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.expression.simple` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.query` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.query.select` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree.query.select.from` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree_aggregator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree_creator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.translator.tree_creator.builder` | 51 | 51 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mongo.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.database.dialects.mssql` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.generator` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.generator.producers` | 100 | 100 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.inspections` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mssql.introspector` | 12 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.introspector.jdbc` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.localdb` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.model` | 73 | 72 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mssql.model.properties` | 18 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mssql.ssrp` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mssql.testing.tsqlt` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mssql.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.database.dialects.mssql.ui` | 7 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.database.dialects.mysql` | 5 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysql.jar |
| `com.intellij.database.dialects.mysql.model` | 24 | 23 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysql.jar |
| `com.intellij.database.dialects.mysql.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysql.jar |
| `com.intellij.database.dialects.mysqlbase` | 20 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar |
| `com.intellij.database.dialects.mysqlbase.generator` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.generator.producers` | 54 | 54 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.introspector` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.introspector.jdbc` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.model` | 33 | 33 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar |
| `com.intellij.database.dialects.mysqlbase.model.properties` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.model.properties.references` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.mysqlbase.schemaEditor.model.applier` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar |
| `com.intellij.database.dialects.mysqlbase.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.database.dialects.oracle` | 32 | 30 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.database.dialects.oracle.debugger` | 75 | 75 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.database.dialects.oracle.ex.coverage` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.coverage.jar |
| `com.intellij.database.dialects.oracle.generator` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.generator.dml` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.generator.producers` | 88 | 88 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.introspector` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.introspector.jdbc` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.model` | 91 | 90 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.database.dialects.oracle.model.properties` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.testing.utplsql` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.database.dialects.oracle.tns` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.oracle.tns.ui` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.database.dialects.oracle.types` | 12 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.database.dialects.postgres` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgres.ex.schemaEditor.ui` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.ex.jar |
| `com.intellij.database.dialects.postgres.generator` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgres.generator.producers` | 33 | 33 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgres.introspector` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgres.model` | 57 | 56 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.ex.jar |
| `com.intellij.database.dialects.postgres.model.properties` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgres.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.database.dialects.postgresbase` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.generator.producers` | 32 | 32 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.introspector` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.introspector.jdbc` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.model` | 45 | 45 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.ex.jar |
| `com.intellij.database.dialects.postgresbase.model.properties` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.pgpass` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresbase.types` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.generator` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.generator.dml` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.generator.producers` | 97 | 97 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.model` | 59 | 59 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.ex.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.model.properties` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.schemaEditor.model.applier` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.ex.jar |
| `com.intellij.database.dialects.postgresgreenplumbase.schemaEditor.ui` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.ex.jar |
| `com.intellij.database.dialects.redis` | 19 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.backend` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.backend.jar |
| `com.intellij.database.dialects.redis.backend.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.backend.jar |
| `com.intellij.database.dialects.redis.delegates` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.hashTable` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.jsonString` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.list` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.other` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.set` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.sortedSet` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.stream` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.string` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.delegates.unknown` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.hashTable` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.jsonString` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.list` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.other` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.set` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.sortedSet` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.stream` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.string` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.dml.unknown` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.generator.producers` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.introspector` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.model` | 10 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redis.model.properties` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.database.dialects.redshift` | 9 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.generator.producers` | 36 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.introspector` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.model` | 47 | 46 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.model.properties` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.plan` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.redshift.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.database.dialects.snowflake` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.ex.actions` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.ex.jar |
| `com.intellij.database.dialects.snowflake.ex.extensions` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.ex.jar |
| `com.intellij.database.dialects.snowflake.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.generator.producers` | 67 | 67 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.introspector` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.model` | 44 | 43 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.ex.jar |
| `com.intellij.database.dialects.snowflake.model.properties` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.model.properties.references` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.plan` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.snowflake.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.database.dialects.spark` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.database.dialects.spark.generator` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.database.dialects.spark.generator.producers` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.database.dialects.spark.model` | 39 | 37 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.database.dialects.spark.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.database.dialects.sql92` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sql92.jar |
| `com.intellij.database.dialects.sqlite` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.ex` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.ex.jar |
| `com.intellij.database.dialects.sqlite.ex.model` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.ex.jar |
| `com.intellij.database.dialects.sqlite.ex.ui` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.ex.jar |
| `com.intellij.database.dialects.sqlite.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.generator.producers` | 36 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.introspector` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.introspector.jdbc` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.model` | 29 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.model.properties` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.sql` | 19 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.sql.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.sql.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.sql.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sqlite.types` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar |
| `com.intellij.database.dialects.sybase` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.generator` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.generator.producers` | 47 | 47 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.introspector` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.introspector.jdbc` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.model` | 38 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.ex.jar |
| `com.intellij.database.dialects.sybase.model.properties` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.plan` | 3 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.sybase.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.database.dialects.vertica` | 10 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.generator` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.generator.producers` | 55 | 55 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.introspector` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.introspector.jdbc` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.model` | 37 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.ex.jar |
| `com.intellij.database.dialects.vertica.model.properties` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.plan` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.dialects.vertica.types` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.database.diff` | 17 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.docker.cli` | 3 | 3 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.docker.compose` | 1 | 1 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.docker.utils` | 1 | 1 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.dump` | 12 | 12 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.editor` | 60 | 58 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.explorer` | 20 | 20 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.explorer.actions` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.explorer.forest` | 13 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.explorer.icons` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.explorer.internals` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.explorer.settings` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.explorer.structure` | 80 | 80 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.extensions` | 19 | 18 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.jar, plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.extractors` | 56 | 56 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.extractors.tz` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.featureStatistics` | 2 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.flameGraph.plan` | 3 | 2 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.frontend.split.database.console` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.frontend.split.jar |
| `com.intellij.database.frontend.split.editor` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.frontend.split.jar |
| `com.intellij.database.frontend.split.sql.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.frontend.split.jar |
| `com.intellij.database.fulltextsearch` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.grazie` | 1 | 0 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.grid.editors.lexers` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.ide` | 24 | 17 | 1 | lib/intellij.datagrip.jar, plugins/datagrip-impl/lib/datagrip-impl.jar |
| `com.intellij.database.ide.actions` | 7 | 7 | 0 | plugins/datagrip-impl/lib/datagrip-impl.jar |
| `com.intellij.database.ide.hierarchy` | 4 | 4 | 0 | plugins/datagrip-impl/lib/datagrip-impl.jar |
| `com.intellij.database.ide.welcomeScreen` | 4 | 4 | 0 | plugins/datagrip-impl/lib/datagrip-impl.jar |
| `com.intellij.database.impl.bookmarks` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.bookmarks.jar |
| `com.intellij.database.impl.frontend.actions.util` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.frontend.jar |
| `com.intellij.database.impl.jcef.actions` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jcef.jar |
| `com.intellij.database.impl.jcef.datagrid` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jcef.jar |
| `com.intellij.database.impl.navbar` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.navbar.jar |
| `com.intellij.database.intentions` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.introspection` | 61 | 61 | 2 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.introspection.query` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.java` | 1 | 1 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.layoutedQueries` | 17 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.layoutedQueries.impl` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.liveTemplates` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.liveTemplates.macros` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.loaders` | 1 | 1 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.mcp.toolsets` | 7 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.mcp.jar |
| `com.intellij.database.model` | 121 | 115 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.model.basic` | 193 | 192 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.model.diff` | 8 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.model.families` | 17 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.model.meta` | 19 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.model.migration` | 27 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.model.properties` | 16 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.model.properties.references` | 15 | 15 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.model.serialization` | 25 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.model.serialization.converters` | 45 | 45 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.notifications` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.plan` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.plan.ui` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.plugin` | 2 | 2 | 0 | plugins/DatabaseTools/lib/database-plugin.jar |
| `com.intellij.database.plugin.frameworks` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.plugin.frameworks.jar |
| `com.intellij.database.pro.dataSource.srcStorage` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.pro.jar |
| `com.intellij.database.pro.settings` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.pro.jar |
| `com.intellij.database.psi` | 64 | 62 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.psi.documentation` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.psi.documentation.delegates` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.remote` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.dbimport` | 12 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.types.jar |
| `com.intellij.database.remote.jdba` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.core` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.exceptions` | 29 | 29 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.impl` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.intermediate` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.jdbc` | 20 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.jdbc.dialects` | 35 | 35 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.sql` | 13 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdba.util` | 11 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.shim.jar, plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc.helpers` | 57 | 57 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc.impl` | 27 | 26 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc.impl.cassandra` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc.impl.dynamo` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.jdbc.impl.mssql` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.toolkit` | 5 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.remote.util` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar |
| `com.intellij.database.run` | 19 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.run.actions` | 136 | 131 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.audit` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.session` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar |
| `com.intellij.database.run.ui` | 73 | 67 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.ui.grid` | 38 | 38 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.run.ui.grid.documentation` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.ui.grid.editors` | 62 | 62 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.run.ui.grid.editors.lexers` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.run.ui.grid.renderers` | 9 | 9 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.ui.grid.selection` | 2 | 2 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.run.ui.table` | 24 | 21 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.run.ui.table.statisticsPanel` | 4 | 4 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.run.ui.table.statisticsPanel.types` | 10 | 10 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.run.ui.text` | 3 | 3 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.run.ui.treetable` | 21 | 20 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.database.schemaEditor` | 41 | 40 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.schemaEditor.model` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.schemaEditor.model.applier` | 29 | 29 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.schemaEditor.model.state` | 25 | 25 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.schemaEditor.owner` | 7 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.schemaEditor.ui` | 49 | 49 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.scopes` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.script` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.script.generator` | 39 | 39 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.script.generator.dml` | 35 | 33 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.script.generator.ui` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.script.translator` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.settings` | 28 | 27 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.csv.core.impl.jar |
| `com.intellij.database.sql.backend.core` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar |
| `com.intellij.database.sql.backend.core.editor` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar |
| `com.intellij.database.sql.backend.core.psi` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar |
| `com.intellij.database.sql.backend.core.psi.impl.support` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar |
| `com.intellij.database.sql.backend.core.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar |
| `com.intellij.database.sql.common.core.editor` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.core.jar |
| `com.intellij.database.sql.common.core.psi.impl.support` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.core.jar |
| `com.intellij.database.sql.common.core.searchEverywhere` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.core.jar |
| `com.intellij.database.sql.common.impl` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar |
| `com.intellij.database.sql.common.impl.editor` | 10 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar |
| `com.intellij.database.sql.common.impl.formatter` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar |
| `com.intellij.database.sql.common.impl.formatter.settings` | 19 | 18 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar |
| `com.intellij.database.sql.common.impl.psi.impl.support` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar |
| `com.intellij.database.sql.copyright` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.copyright.jar |
| `com.intellij.database.sql.frontend.impl` | 3 | 3 | 3 | plugins/DatabaseTools/lib/modules/intellij.database.sql.frontend.impl.jar |
| `com.intellij.database.sql.intelliLang` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.intelliLang.jar |
| `com.intellij.database.sql.intelliLang.injection` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.intelliLang.jar |
| `com.intellij.database.sql.intelliLang.intentions` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.intelliLang.jar |
| `com.intellij.database.ssh` | 6 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ssh.jar |
| `com.intellij.database.ssh.ui` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.ssh.ui.jar |
| `com.intellij.database.statistic` | 28 | 24 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.symbols` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.targetChooser` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.trialPromotion.idesWithFreeTier` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.trialPromotion.idesWithFreeTier.jar |
| `com.intellij.database.types` | 57 | 57 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.jar |
| `com.intellij.database.util` | 100 | 95 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.util.common` | 35 | 35 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.util.jar |
| `com.intellij.database.util.sequences` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.util.jar |
| `com.intellij.database.util.tree` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.util.jar |
| `com.intellij.database.vfs` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.vfs.fragment` | 4 | 4 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view` | 73 | 71 | 1 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.view.actions` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.editors` | 3 | 3 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.emptyState` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.emptyState.actions` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.integrationgTests` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.introspection` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.ui` | 40 | 39 | 4 | plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.ui.cloud.explorer` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.view.ui.cloud.jba` | 12 | 12 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.sql` | 36 | 36 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.jar |
| `com.intellij.sql.actions` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.actions.members` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.annotators` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.cardinality` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar |
| `com.intellij.sql.completion` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.completion.ml` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.completionMlRanking.jar |
| `com.intellij.sql.completion.options` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.completion.providers` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dataFlow` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dataFlow.instructions` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.database` | 15 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.dialects` | 19 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.dialects.base` | 22 | 21 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.sql.dialects.base.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.sql.dialects.base.psi.stubs` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar |
| `com.intellij.sql.dialects.bigquery` | 19 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.sql.dialects.bigquery.psi` | 28 | 28 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.sql.dialects.bigquery.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.sql.dialects.bigquery.refactoring` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar |
| `com.intellij.sql.dialects.cassandra` | 21 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.sql.dialects.cassandra.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.sql.dialects.cassandra.psi` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.sql.dialects.cassandra.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar |
| `com.intellij.sql.dialects.clickhouse` | 18 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.sql.dialects.clickhouse.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.sql.dialects.clickhouse.psi` | 19 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.sql.dialects.clickhouse.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.sql.dialects.clickhouse.refactoring` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar |
| `com.intellij.sql.dialects.cockroach` | 18 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.sql.dialects.cockroach.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.sql.dialects.cockroach.psi` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.sql.dialects.cockroach.psi.resolve` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.sql.dialects.cockroach.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar |
| `com.intellij.sql.dialects.couchbase` | 19 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.sql.dialects.couchbase.psi` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.sql.dialects.couchbase.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar |
| `com.intellij.sql.dialects.dateTime` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dialects.dateTime.psi` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dialects.dateTime.psi.impl` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dialects.dateTime.psi.values` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dialects.db2` | 27 | 26 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.db2.iseries` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.db2.psi` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.db2.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.db2.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.db2.zos` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar |
| `com.intellij.sql.dialects.derby` | 21 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.sql.dialects.derby.psi` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.sql.dialects.derby.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar |
| `com.intellij.sql.dialects.dynamo` | 14 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.sql.dialects.dynamo.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar |
| `com.intellij.sql.dialects.exasol` | 20 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.sql.dialects.exasol.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.sql.dialects.exasol.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.sql.dialects.exasol.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar |
| `com.intellij.sql.dialects.functions` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.dialects.generic` | 6 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.sql.dialects.generic.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar |
| `com.intellij.sql.dialects.greenplum` | 20 | 20 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.sql.dialects.greenplum.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.sql.dialects.greenplum.psi` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.sql.dialects.greenplum.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar |
| `com.intellij.sql.dialects.h2` | 21 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.sql.dialects.h2.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.sql.dialects.h2.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar |
| `com.intellij.sql.dialects.hive` | 23 | 21 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.sql.dialects.hive.psi` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.sql.dialects.hive.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar |
| `com.intellij.sql.dialects.hsql` | 20 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.sql.dialects.hsql.psi` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.sql.dialects.hsql.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.sql.dialects.hsql.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar |
| `com.intellij.sql.dialects.maria` | 7 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.sql.dialects.maria.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar |
| `com.intellij.sql.dialects.mongo` | 20 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar |
| `com.intellij.sql.dialects.mongo.js` | 63 | 59 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.ex.jar |
| `com.intellij.sql.dialects.mongo.js.completion` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar |
| `com.intellij.sql.dialects.mongo.js.editing` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.ex.jar |
| `com.intellij.sql.dialects.mongo.js.inspections` | 12 | 11 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar |
| `com.intellij.sql.dialects.mongo.js.parser` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar |
| `com.intellij.sql.dialects.mongo.js.psi` | 19 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar |
| `com.intellij.sql.dialects.mongo.js.psi.impl` | 20 | 20 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.manipulators` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve.context` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve.place` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve.scopes` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve.symbols` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.js.psi.resolve.types` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar |
| `com.intellij.sql.dialects.mongo.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.sql.jar |
| `com.intellij.sql.dialects.mongo.psi.stubs` | 1 | 0 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.sql.jar |
| `com.intellij.sql.dialects.mssql` | 28 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mssql.dataFlow` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mssql.formatter` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mssql.inspections` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar |
| `com.intellij.sql.dialects.mssql.psi` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mssql.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mssql.refactoring` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar |
| `com.intellij.sql.dialects.mysql` | 27 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.sql.dialects.mysql.dataFlow` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.sql.dialects.mysql.inspections` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar |
| `com.intellij.sql.dialects.mysql.psi` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.sql.dialects.mysql.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.sql.dialects.mysql.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar |
| `com.intellij.sql.dialects.netsuite` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle` | 31 | 30 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.sql.dialects.oracle.dataFlow` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.formatter.model` | 8 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.inspections` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.sql.dialects.oracle.psi` | 28 | 28 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.psi.stubs` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oracle.tns` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar |
| `com.intellij.sql.dialects.oraplus` | 17 | 16 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar |
| `com.intellij.sql.dialects.postgres` | 31 | 31 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.postgres.dataFlow` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.postgres.formatter` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar, plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar |
| `com.intellij.sql.dialects.postgres.formatter.model` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.postgres.inspections` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.ex.jar |
| `com.intellij.sql.dialects.postgres.psi` | 27 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.postgres.psi.stubs` | 13 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.postgres.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar |
| `com.intellij.sql.dialects.redis` | 18 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.sql.dialects.redis.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.sql.dialects.redis.formatter.model` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.sql.dialects.redis.psi` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.sql.dialects.redis.psi.stubs` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar |
| `com.intellij.sql.dialects.redshift` | 20 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.sql.dialects.redshift.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.sql.dialects.redshift.psi` | 8 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.sql.dialects.redshift.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar |
| `com.intellij.sql.dialects.snowflake` | 19 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.snowflake.dataFlow` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.snowflake.formatter` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.snowflake.psi` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.snowflake.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.snowflake.refactoring` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar |
| `com.intellij.sql.dialects.spark` | 15 | 13 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.sql.dialects.spark.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar |
| `com.intellij.sql.dialects.sql92` | 22 | 20 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sql92.jar |
| `com.intellij.sql.dialects.sql92.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sql92.jar |
| `com.intellij.sql.dialects.sybase` | 21 | 19 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.sql.dialects.sybase.psi` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.sql.dialects.sybase.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.sql.dialects.sybase.refactoring` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar |
| `com.intellij.sql.dialects.vertica` | 18 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.sql.dialects.vertica.psi` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.sql.dialects.vertica.psi.stubs` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.sql.dialects.vertica.refactoring` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar |
| `com.intellij.sql.editor` | 30 | 27 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.editor.surroundWith` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.formatter` | 18 | 18 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.formatter.model` | 202 | 184 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.formatter.settings` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.jar |
| `com.intellij.sql.highlighting` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.injection` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inlays` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.inspections` | 60 | 59 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.inspections.configuration` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.inspections.dataflow` | 14 | 14 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.dataflow.sql` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.dataflow.sql.anchor` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.dataflow.sql.inst` | 30 | 30 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.dataflow.sql.problems` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.dataflow.types` | 25 | 25 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.inspections.suppression` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.intentions` | 41 | 41 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.liveTemplates` | 3 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.liveTemplates.contextTypes` | 6 | 6 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.postfixTemplates` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.psi` | 233 | 232 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.psi.impl` | 262 | 261 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.psi.impl.lexer` | 4 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.impl.parser` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.impl.support` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.psi.patterns` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.stubs` | 9 | 9 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.stubs.elementStubs` | 17 | 17 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.stubs.elementTypes` | 22 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.stubs.factories` | 21 | 21 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.psi.stubs.serializers` | 22 | 22 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.refactoring` | 8 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.refactoring.extractFunction` | 11 | 8 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.refactoring.inline` | 3 | 3 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.refactoring.rename` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.refactoring.rename.inplace` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.refactoring.suggested` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.script` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.searchEverywhere` | 2 | 2 | 2 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.slicer` | 10 | 10 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.smartenter` | 5 | 5 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.structuralsearch` | 2 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.structuralsearch.compiler` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.structuralsearch.matching` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.structuralsearch.predicates` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.symbols` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.symbols.virtual` | 7 | 7 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar |
| `com.intellij.sql.unwrap` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar |
| `com.intellij.sql.util` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.sql.jar |

## 10. Выгруженные сигнатуры (javap -public)

- [`com.intellij.database.dataSource.LocalDataSource`](research/api/com.intellij.database.dataSource.LocalDataSource.txt)
- [`com.intellij.database.dataSource.LocalDataSourceManager`](research/api/com.intellij.database.dataSource.LocalDataSourceManager.txt)
- [`com.intellij.database.dataSource.DataSourceStorage`](research/api/com.intellij.database.dataSource.DataSourceStorage.txt)
- [`com.intellij.database.psi.DbDataSource`](research/api/com.intellij.database.psi.DbDataSource.txt)
- [`com.intellij.database.model.RawDataSource`](research/api/com.intellij.database.model.RawDataSource.txt)
- [`com.intellij.database.psi.DataSourceManager`](research/api/com.intellij.database.psi.DataSourceManager.txt)
- [`com.intellij.database.dataSource.DatabaseDriver`](research/api/com.intellij.database.dataSource.DatabaseDriver.txt)
- [`com.intellij.database.dataSource.DatabaseDriverManager`](research/api/com.intellij.database.dataSource.DatabaseDriverManager.txt)
- [`com.intellij.database.dataSource.DatabaseDriverImpl`](research/api/com.intellij.database.dataSource.DatabaseDriverImpl.txt)
- [`com.intellij.database.dataSource.DatabaseConnection`](research/api/com.intellij.database.dataSource.DatabaseConnection.txt)
- [`com.intellij.database.dataSource.DatabaseConnectionCore`](research/api/com.intellij.database.dataSource.DatabaseConnectionCore.txt)
- [`com.intellij.database.dataSource.DatabaseConnectionManager`](research/api/com.intellij.database.dataSource.DatabaseConnectionManager.txt)
- [`com.intellij.database.dataSource.DatabaseConnectionPoint`](research/api/com.intellij.database.dataSource.DatabaseConnectionPoint.txt)
- [`com.intellij.database.view.DatabaseView`](research/api/com.intellij.database.view.DatabaseView.txt)
- [`com.intellij.database.view.DatabaseStructure`](research/api/com.intellij.database.view.DatabaseStructure.txt)
- [`com.intellij.database.console.JdbcConsole`](research/api/com.intellij.database.console.JdbcConsole.txt)
- [`com.intellij.database.console.JdbcConsoleProvider`](research/api/com.intellij.database.console.JdbcConsoleProvider.txt)
- [`com.intellij.database.console.JdbcConsoleCore`](research/api/com.intellij.database.console.JdbcConsoleCore.txt)
- [`com.intellij.database.console.session.DatabaseSessionManager`](research/api/com.intellij.database.console.session.DatabaseSessionManager.txt)
- [`com.intellij.database.console.session.DatabaseSession`](research/api/com.intellij.database.console.session.DatabaseSession.txt)
- [`com.intellij.database.datagrid.DataRequest`](research/api/com.intellij.database.datagrid.DataRequest.txt)
- [`com.intellij.database.datagrid.DataConsumer`](research/api/com.intellij.database.datagrid.DataConsumer.txt)
- [`com.intellij.database.extensions.DataConsumer`](research/api/com.intellij.database.extensions.DataConsumer.txt)
- [`com.intellij.sql.psi.SqlFile`](research/api/com.intellij.sql.psi.SqlFile.txt)
- [`com.intellij.sql.psi.SqlLanguage`](research/api/com.intellij.sql.psi.SqlLanguage.txt)
- [`com.intellij.sql.psi.SqlElement`](research/api/com.intellij.sql.psi.SqlElement.txt)
- [`com.intellij.database.remote.jdba.sql.SqlStatement`](research/api/com.intellij.database.remote.jdba.sql.SqlStatement.txt)
- [`com.intellij.sql.psi.SqlStatement`](research/api/com.intellij.sql.psi.SqlStatement.txt)
- [`com.intellij.sql.psi.SqlQueryExpression`](research/api/com.intellij.sql.psi.SqlQueryExpression.txt)
- [`com.intellij.sql.dialects.SqlLanguageDialect`](research/api/com.intellij.sql.dialects.SqlLanguageDialect.txt)
- [`com.intellij.sql.dialects.SqlLanguageDialectEx`](research/api/com.intellij.sql.dialects.SqlLanguageDialectEx.txt)
- [`com.intellij.database.util.SqlDialects`](research/api/com.intellij.database.util.SqlDialects.txt)
- [`com.intellij.database.dialects.postgres.PgDialect`](research/api/com.intellij.database.dialects.postgres.PgDialect.txt)
- [`com.intellij.sql.dialects.postgres.PgDialect`](research/api/com.intellij.sql.dialects.postgres.PgDialect.txt)
- [`com.intellij.database.dialects.postgresbase.PgDialectBase`](research/api/com.intellij.database.dialects.postgresbase.PgDialectBase.txt)
- [`com.intellij.sql.dialects.postgres.PgDialectBase`](research/api/com.intellij.sql.dialects.postgres.PgDialectBase.txt)
- [`com.intellij.database.Dbms`](research/api/com.intellij.database.Dbms.txt)
- [`com.intellij.sql.completion.SqlCompletionContributor`](research/api/com.intellij.sql.completion.SqlCompletionContributor.txt)
- [`com.intellij.database.psi.DbPsiFacade`](research/api/com.intellij.database.psi.DbPsiFacade.txt)
- [`com.intellij.database.psi.DbElement`](research/api/com.intellij.database.psi.DbElement.txt)
- [`com.intellij.database.model.DasModel`](research/api/com.intellij.database.model.DasModel.txt)
- [`com.intellij.database.model.DasTable`](research/api/com.intellij.database.model.DasTable.txt)
- [`com.intellij.database.model.DasColumn`](research/api/com.intellij.database.model.DasColumn.txt)
- [`com.intellij.database.model.basic.BasicModel`](research/api/com.intellij.database.model.basic.BasicModel.txt)
- [`com.intellij.database.datagrid.DataGrid`](research/api/com.intellij.database.datagrid.DataGrid.txt)
- [`com.intellij.database.datagrid.DataGridUtil`](research/api/com.intellij.database.datagrid.DataGridUtil.txt)
- [`com.intellij.database.datagrid.GridModel`](research/api/com.intellij.database.datagrid.GridModel.txt)
- [`com.intellij.database.datagrid.GridColumn`](research/api/com.intellij.database.datagrid.GridColumn.txt)
- [`com.intellij.database.datagrid.GridRow`](research/api/com.intellij.database.datagrid.GridRow.txt)
- [`com.intellij.database.datagrid.GridUtil`](research/api/com.intellij.database.datagrid.GridUtil.txt)
- [`com.intellij.database.run.ui.TableResultPanel`](research/api/com.intellij.database.run.ui.TableResultPanel.txt)
- [`com.intellij.database.datagrid.GridHelper`](research/api/com.intellij.database.datagrid.GridHelper.txt)
- [`com.intellij.database.datagrid.GridDataHookUp`](research/api/com.intellij.database.datagrid.GridDataHookUp.txt)
- [`com.intellij.database.run.ui.GridDataSupport`](research/api/com.intellij.database.run.ui.GridDataSupport.txt)
- [`com.intellij.database.datagrid.GridMutator`](research/api/com.intellij.database.datagrid.GridMutator.txt)
- [`com.intellij.database.datagrid.DatabaseMutator`](research/api/com.intellij.database.datagrid.DatabaseMutator.txt)
- [`com.intellij.database.util.DdlBuilder`](research/api/com.intellij.database.util.DdlBuilder.txt)
- [`com.intellij.database.script.generator.ScriptGenerator`](research/api/com.intellij.database.script.generator.ScriptGenerator.txt)
- [`com.intellij.database.util.DbSqlUtil`](research/api/com.intellij.database.util.DbSqlUtil.txt)

## 11. Предупреждения сканирования

Нет.
