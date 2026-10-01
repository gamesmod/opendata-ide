# Матрица совместимости

Строки добавляются `scripts/research.ps1` для каждой проверенной установки. Колонка «Статус» обновляется вручную после прохождения build/POC (`research OK` → `build OK` → `POC OK`).

Правило: IntelliJ Platform, Database Tools и grid берутся из **одной** установки (одна build-линейка); версия Kotlin плагина не выше версии Kotlin stdlib в IDE; JDK сборки = major версия JBR.

<!-- rows:begin -->
| Ключ | Дата | Продукт | IntelliJ build | DataGrip build | Database Tools | JBR | Kotlin (IDE) | Gradle | Статус |
|---|---|---|---|---|---|---|---|---|---|
| DB-262.10968.148 | 2026-10-01 | DataGrip 2026.2.6 | 262.10968.148 | 262.10968.148 | 262.10968.148 | 25.0.4 | 2.4.0 | 9.8.0 | build OK · tests 16/16 OK · JDK 21 · IPGP 2.19.0 · Kotlin 2.4.20 |
<!-- rows:end -->
