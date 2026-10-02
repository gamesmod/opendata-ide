## OpenData IDE 0.2.0 — самостоятельная IDE

OpenData IDE стала отдельным продуктом на **открытой IntelliJ Platform 2026.2.3** (IC-262.10968.63, Apache 2.0).
DataGrip и закрытый плагин Database Tools and SQL больше не нужны.

### СУБД
- **PostgreSQL**: драйвер 42.7.13 в комплекте.
- **Apache Cloudberry**: драйвер PostgreSQL, DDL с `DISTRIBUTED BY`.
- **ClickHouse**: `clickhouse-jdbc` 0.10.0; редактирование таблиц через синхронные мутации.
- **Dremio**: Arrow Flight SQL JDBC 19.0.0, только чтение.

Драйверы ClickHouse и Dremio загружаются из Maven Central при первом подключении с проверкой SHA-256.
Без сети JAR можно положить в каталог драйверов вручную: см. `docs/databases.md`.

### Возможности
- Источники данных: создание, правка, копирование, удаление, Test Connection. Пароли хранятся в `PasswordSafe`.
- Database Explorer: схемы, таблицы, представления, функции, последовательности, типы, колонки, ключи, индексы, триггеры, ограничения.
- SQL-консоль:
  - подсветка и completion по метаданным;
  - Ctrl+Enter — текущий запрос, Ctrl+Shift+Enter — скрипт, Ctrl+F2 — отмена;
  - ошибки с подсветкой позиции;
  - Auto-commit, Commit и Rollback; история запросов (Ctrl+Alt+E).
- Результаты в открытом JetBrains DataGrid: сортировка, фильтр, копирование, NULL, JSON, LOB.
- Редактор таблицы: WHERE и ORDER BY, постраничная загрузка, правка ячеек, вставка и удаление строк, Submit и Revert.
- DDL таблиц, представлений, индексов, последовательностей, функций и схем.

### Облегчённый дистрибутив
В составе только ядро платформы, `intellij.grid` и плагин `opendata-db`. Нет плагинов Java, Kotlin и Git,
нет советов при запуске, onboarding нового UI и проверки обновлений. Linux-сборка занимает около 1 ГБ на диске (у IntelliJ IDEA — 2,6 ГБ).

### Файлы
- `OpenData-IDE-0.2.0-windows-x64.zip` — распаковать и запустить `bin\opendata64.exe`.
- `OpenData-IDE-0.2.0-linux-x64.tar.gz` — распаковать и запустить `bin/opendata`.
- `opendata-db-0.2.0.zip` — DB-плагин для разработчиков (ставится в открытую IntelliJ Platform 2026.2).

### Проверено
- 23 теста на PostgreSQL 16, в режиме Cloudberry, на ClickHouse и Dremio OSS 26.
- Самопроверка собранной IDE под Xvfb: Explorer → консоль → DataGrid → редактор таблицы, для всех четырёх СУБД.
- Windows: сборка ZIP и запуск IDE в CI. Ручная приёмка на Windows 11 ещё не проводилась.

### Ограничения
- Cloudberry проверен только в режиме совместимости на PostgreSQL.
- У `opendata64.exe` остаётся значок JetBrains в ресурсах exe; в окне, splash и About используется брендинг OpenData.
- Навигация Ctrl+Click и find usages по SQL пока не реализованы: парсер SQL плоский (roadmap R-11).
