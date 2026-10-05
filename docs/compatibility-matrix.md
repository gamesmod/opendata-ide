# Матрица совместимости

## Установки JetBrains, исследованные на этапе 0

Строки добавляются `scripts/research.ps1` для каждой проверенной установки. Колонка «Статус» обновляется вручную после прохождения build/POC (`research OK` → `build OK` → `POC OK`).

Правило: IntelliJ Platform, Database Tools и grid берутся из **одной** установки (одна build-линейка); версия Kotlin плагина не выше версии Kotlin stdlib в IDE; JDK сборки = major версия JBR.

<!-- rows:begin -->
| Ключ | Дата | Продукт | IntelliJ build | DataGrip build | Database Tools | JBR | Kotlin (IDE) | Gradle | Статус |
|---|---|---|---|---|---|---|---|---|---|
| DB-262.10968.148 | 2026-10-01 | DataGrip 2026.2.6 | 262.10968.148 | 262.10968.148 | 262.10968.148 | 25.0.4 | 2.4.0 | 9.8.0 | build OK · tests 16/16 OK · JDK 21 · IPGP 2.19.0 · Kotlin 2.4.20 |
<!-- rows:end -->

## Продукт OpenData IDE (с версии 0.2.0)

Продукт собирается на открытой IntelliJ Platform. Платформа и `intellij.grid` берутся из одной открытой сборки,
JDBC-драйверы зафиксированы в `DbKind.kt`. Раздел ведётся вручную, `research` его не меняет.

| OpenData | Платформа (open source) | Build | JBR | JDK сборки | Kotlin | IPGP | Gradle | Статус |
|---|---|---|---|---|---|---|---|---|
| 0.4.0 | IntelliJ IDEA 2026.2.3 | IC-262.10968.63 | 25.0.4 | 21 | 2.4.20 | 2.19.0 | 9.8.0 | build OK · тесты 48 (+ MPP в CI: Greenplum 6, 7, Cloudberry 2.1) · smoke Linux OK · все драйверы в поставке |
| 0.3.0 | IntelliJ IDEA 2026.2.3 | IC-262.10968.63 | 25.0.4 | 21 | 2.4.20 | 2.19.0 | 9.8.0 | build OK · тесты 33/33 · smoke Linux OK (PG, CB, CH, Dremio) · UI-проверка проектов, драйверов, импорта/экспорта |
| 0.2.0 | IntelliJ IDEA 2026.2.3 | IC-262.10968.63 | 25.0.4 | 21 | 2.4.20 | 2.19.0 | 9.8.0 | build OK · тесты 25/25 · smoke Linux OK (PG, CB, CH, Dremio) · Windows: ZIP + запуск IDE в CI (RESULT=OK) |

| СУБД | Драйвер | Проверено на сервере |
|---|---|---|
| PostgreSQL | `org.postgresql:postgresql:42.7.13` | 16 |
| Greenplum | тот же драйвер PostgreSQL | 6 и 7 (`andruche/greenplum`, CI) |
| Apache Cloudberry | тот же драйвер PostgreSQL | 2.1.0-incubating (`woblerr/cloudberry`, CI) и режим совместимости на PostgreSQL 16 |
| ClickHouse | `com.clickhouse:clickhouse-jdbc:0.10.0:all` (+ slf4j 2.0.17) | 26.10 (локально), 25.8 (CI) |
| Dremio | `org.apache.arrow:flight-sql-jdbc-driver:19.0.0` | OSS 26.0.5 (локально), 26.0 (CI) |
