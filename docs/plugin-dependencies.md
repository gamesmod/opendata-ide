# Зависимости от плагинов и модулей JetBrains

> Сгенерировано `tools/research/Research.java` 2026-10-01 по установке `DataGrip 2026.2.6` (build `DB-262.10968.148`).
> Все перечисленные классы/EP/сервисы реально найдены в этой сборке. Повторный запуск перезаписывает файл.

## 1. Что подключает OpenData

| Plugin/module | Plugin ID | JAR/module path | Version/build | Compile | Runtime | Поиск локальной установки |
|---|---|---|---|---|---|---|
| Database Tools and SQL | `com.intellij.database` | `plugins/DatabaseTools/lib/*.jar` | 262.10968.148 | да (`bundledPlugin`) | да (`<depends>`) | `JETBRAINS_IDE_HOME` / `DATAGRIP_HOME` → `plugins/` |

## 2. Транзитивные зависимости Database Tools

Plugin-level `<depends>`: —

`<dependencies><plugin>`: `com.intellij.modules.database-capable`

`<dependencies><module>`: `intellij.spellchecker`, `intellij.json.backend`, `intellij.platform.rpc.backend`, `intellij.regexp`

Все из них являются bundled в той же установке и подтягиваются IntelliJ Platform Gradle Plugin автоматически при `bundledPlugin("com.intellij.database")` — проверьте раздел 3.

## 3. Проверка наличия зависимостей в установке

| Зависимость | Найдена |
|---|---|
| `com.intellij.modules.database-capable` | да |
| module `intellij.spellchecker` | да |
| module `intellij.json.backend` | да |
| module `intellij.platform.rpc.backend` | да |
| module `intellij.regexp` | да |

## 4. JAR плагина Database Tools

- `plugins/DatabaseTools/lib/database-plugin.jar`
- `plugins/DatabaseTools/lib/frontend-split/database-frontend.jar`
- `plugins/DatabaseTools/lib/intellij.platform.commercial.verifier.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.ai.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.backend.split.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.cliCommands.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.cloudExplorer.jba.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.completionMlRanking.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.base.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.bigquery.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cassandra.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.clickhouse.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.cockroach.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.couchbase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.db2.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.derby.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.dynamo.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.exasol.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.generic.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.greenplum.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.h2.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hive.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hivebase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.hsql.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.maria.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.js.external.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mongo.sql.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mssql.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysql.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.mysqlbase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.coverage.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.oracle.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgres.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresbase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.postgresgreenplumbase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.backend.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redis.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.redshift.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.snowflake.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.spark.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sql92.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sqlite.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.sybase.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.ex.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.dialects.vertica.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.frontend.split.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.bookmarks.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.common.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.frontend.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.jcef.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.impl.navbar.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.jdbcConsole.shim.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.mcp.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.plugin.frameworks.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.pro.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.backend.core.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.common.core.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.common.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.copyright.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.core.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.frontend.core.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.frontend.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.impl.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.intelliLang.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.sql.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.ssh.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.ssh.ui.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.trialPromotion.idesWithFreeTier.jar`
- `plugins/DatabaseTools/lib/modules/intellij.database.util.jar`
- `plugins/DatabaseTools/lib/modules/intellij.libraries.jts.io.common.jar`
- `plugins/DatabaseTools/lib/tips-database-plugin.jar`

## 5. Все bundled-плагины установки

| Plugin ID | Name | Version | Каталог |
|---|---|---|---|
| `Git4Idea` | Git | 262.10968.148 | `plugins/vcs-git/lib` |
| `XPathView` | XPathView + XSLT | 262.10968.148 | `plugins/xpath/lib` |
| `com.intellij` | IDEA CORE | — | `lib` |
| `com.intellij.completion.ml.ranking` | Machine Learning Code Completion | 262.10968.148 | `plugins/completionMlRanking/lib` |
| `com.intellij.configurationScript` | Configuration Script | 262.10968.148 | `plugins/configurationScript/lib` |
| `com.intellij.database` | Database Tools and SQL | 262.10968.148 | `plugins/DatabaseTools/lib` |
| `com.intellij.database.ide` | DataGrip Customization | 262.10968.148 | `plugins/datagrip-impl/lib` |
| `com.intellij.datagrip.frontend.split.customization` | DataGrip Frontend Customization | — | `lib/frontend-split` |
| `com.intellij.dev` | DevKit Runtime | 262.10968.148 | `plugins/dev/lib` |
| `com.intellij.diagram` | Diagrams | 262.10968.148 | `plugins/uml/lib` |
| `com.intellij.groovy.scripting` | Groovy Scripting | 262.10968.148 | `plugins/groovy-scripting/lib` |
| `com.intellij.ja` | Japanese Language Pack / 日本語言語パック | 262.10968.148 | `plugins/localization-ja/lib` |
| `com.intellij.ko` | Korean Language Pack / 한국어 언어 팩 | 262.10968.148 | `plugins/localization-ko/lib` |
| `com.intellij.marketplace.ml` | Machine Learning in Marketplace | 262.10968.148 | `plugins/marketplaceMl/lib` |
| `com.intellij.mcpServer` | MCP Server | 262.10968.148 | `plugins/mcpserver/lib` |
| `com.intellij.mermaid` | Mermaid | 262.10968.148 | `plugins/mermaid/lib` |
| `com.intellij.modules.jcef` | Web Browser (JCEF) | 262.10968.148-linux-x86_64 | `plugins/jcef-plugin/lib` |
| `com.intellij.modules.json` | JSON | 262.10968.148 | `plugins/json/lib` |
| `com.intellij.platform.acp` | Agent Client Protocol | 262.10968.148 | `plugins/platform-acp-plugin/lib` |
| `com.intellij.platform.daemon` | JetBrains OS Integration | 262.10968.148-linux-x86_64 | `plugins/platform-daemon-plugin/lib` |
| `com.intellij.platform.images` | Images | 262.10968.148 | `plugins/platform-images/lib` |
| `com.intellij.plugins.datagrip.solarized.colorscheme` | DataGrip Solarized Color Scheme | 262.10968.148 | `plugins/color-scheme-solarized-datagrip/lib` |
| `com.intellij.searcheverywhere.ml` | Machine Learning in Search Everywhere | 262.10968.148 | `plugins/searchEverywhereMl/lib` |
| `com.intellij.settingsSync` | Backup and Sync | 262.10968.148 | `plugins/settingsSync/lib` |
| `com.intellij.zh` | Chinese (Simplified) Language Pack / 中文语言包 | 262.10968.148 | `plugins/localization-zh/lib` |
| `com.jetbrains.performancePlugin` | Performance Testing | 262.10968.148 | `plugins/performanceTesting/lib` |
| `com.jetbrains.remoteDevServer` | Remote Development Server | 262.10968.148 | `plugins/remote-dev-server/lib` |
| `com.jetbrains.remoteDevelopment` | Remote Development | 262.10968.148 | `plugins/cwm-plugin/lib` |
| `com.jetbrains.station` | Station | 262.10968.148 | `plugins/station-plugin/lib` |
| `intellij.bigdatatools.awsBase` | Big Data Tools AWS Base | 262.10968.148 | `plugins/bigdatatools-awsBase/lib` |
| `intellij.bigdatatools.coreUi` | Big Data Tools Core UI | 262.10968.148 | `plugins/bigdatatools-coreUi/lib` |
| `intellij.bigdatatools.gcloud` | Big Data Tools Google Cloud | 262.10968.148 | `plugins/bigdatatools-gcloud/lib` |
| `intellij.bookmarks.plugin` | Bookmarks Manager | 262.10968.148 | `plugins/platform-bookmarks-plugin/lib` |
| `intellij.database.artifactsBundle` | Database Bundled Drivers | 262.10968.148 | `plugins/database-artifactsBundle/lib` |
| `intellij.database.cloudExplorer.aws` | AWS Cloud Explorer | 262.10968.148 | `plugins/database-cloudExplorer-aws/lib` |
| `intellij.database.cloudExplorer.gcloud` | Google Cloud Explorer | 262.10968.148 | `plugins/database-cloudExplorer-gcloud/lib` |
| `intellij.execution.serviceView.plugin` | Services View | 262.10968.148 | `plugins/platform-execution-serviceView-plugin/lib` |
| `intellij.git.commit.modal` | Modal Commit Interface | 262.10968.148 | `plugins/vcs-git-commit-modal/lib` |
| `intellij.grid.core.plugin` | Grid Core | 262.10968.148 | `plugins/grid-core-plugin/lib` |
| `intellij.grid.loader.json` | Json Loader | 262.10968.148 | `plugins/grid-loader-json/lib` |
| `intellij.grid.loader.xls` | XLS Loader | 262.10968.148 | `plugins/grid-loader-xls/lib` |
| `intellij.grid.plugin` | Data Editor UI | 262.10968.148 | `plugins/grid-plugin/lib` |
| `intellij.java.aetherDependencyResolver.plugin` | Aether Dependency Resolver | 262.10968.148 | `plugins/java-aetherDependencyResolver-plugin/lib` |
| `intellij.libraries.misc.plugin` | Library Modules: Misc | 262.10968.148 | `plugins/libraries-misc-plugin/lib` |
| `intellij.navbar.plugin` | Navbar | 262.10968.148 | `plugins/platform-navbar-plugin/lib` |
| `intellij.platform.ijent.bundledBinaries` | Remote Execution Agent: Bundled Binary Files | 262.10968.148 | `plugins/platform-ijent-bundledBinaries/lib` |
| `intellij.recentFiles.plugin` | Recent Files | 262.10968.148 | `plugins/platform-recentFiles-plugin/lib` |
| `intellij.ssh.plugin` | SSH | 262.10968.148 | `plugins/platform-ssh-plugin/lib` |
| `intellij.structuralSearch.plugin` | Structural Search | 262.10968.148 | `plugins/platform-structuralSearch-plugin/lib` |
| `intellij.structureView.plugin` | Structure View | 262.10968.148 | `plugins/platform-structureView-plugin/lib` |
| `intellij.testRunner.plugin` | Test Runner | 262.10968.148 | `plugins/platform-testRunner-plugin/lib` |
| `intellij.todo.plugin` | TODO Comments | 262.10968.148 | `plugins/platform-todo-plugin/lib` |
| `org.intellij.plugins.markdown` | Markdown | 262.10968.148 | `plugins/markdown/lib` |
| `org.jetbrains.completion.full.line` | Full Line Code Completion | 262.10968.148 | `plugins/fullLine/lib` |
| `org.jetbrains.plugins.database.frontend` | Database for JetBrains Client | — | `plugins/DatabaseTools/lib/frontend-split` |
| `org.jetbrains.plugins.terminal` | Terminal | 262.10968.148 | `plugins/terminal/lib` |
| `org.jetbrains.plugins.textmate` | TextMate Bundles | 262.10968.148 | `plugins/textmate-plugin/lib` |
| `org.jetbrains.plugins.yaml` | YAML | 262.10968.148 | `plugins/yaml/lib` |
| `tanvd.grazi` | Natural Languages | 262.10968.148 | `plugins/grazie/lib` |
| `training` | IDE Features Trainer | 262.10968.148 | `plugins/featuresTrainer/lib` |
