## OpenData IDE — integration plugin

Первый выпуск интеграционного плагина OpenData поверх штатного **Database Tools and SQL**.
Проверен на DataGrip 2026.2.6 (build 262.10968.148) и PostgreSQL 16.

### Что внутри
- Этап 0: инструмент research (`tools/research/Research.java`) и отчёты по реальной установке DataGrip.
- Этап 1 (POC): плагин `io.opendata.integration` с зависимостью от `com.intellij.database`, диагностика интеграции
  (Tools → OpenData: Integration Diagnostics).
- Тесты, все проходят в CI:
  - доступность Database Tools, SQL и диалекта PostgreSQL;
  - сценарий Data Source → Test Connection → introspection → commit/rollback через штатный API Database Tools;
  - JDBC-тесты тестовой базы.

### Установка
Settings → Plugins → ⚙ → Install Plugin from Disk → `opendata-integration-*.zip`.
Требуется DataGrip или IntelliJ IDEA **2026.2+** (build 262+) с плагином Database Tools and SQL.

### Ограничения
- Это плагин интеграции (этапы 0–1). Отдельный продукт OpenData IDE (этап 5) ещё не собран: см. `opendata/product/README.md`.
- Приёмка этапов 2–4 через UI выполняется вручную по `docs/acceptance.md`.
