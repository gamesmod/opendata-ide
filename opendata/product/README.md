# Этап 5 — продукт OpenData IDE

С версии 0.2.0 продукт собирается так: берётся открытая сборка IntelliJ IDEA (Apache 2.0) и перерабатывается
инструментом [`tools/product/AssembleProduct.java`](../../tools/product/AssembleProduct.java). Форка
`intellij-community` и сборки платформы из исходников нет.

```bash
./gradlew -PossIdePath=<распакованная открытая сборка> -PproductOs=linux|windows assembleProduct
# или целиком: scripts/build.ps1 / scripts/build.sh
```

## Требования ТЗ (раздел 10) и где они выполняются

| Требование | Реализация |
|---|---|
| product name, product code | `idea/IdeaApplicationInfo.xml` в `lib/opendata-branding.jar`: «OpenData IDE», `OD-<build>`; `product-info.json` |
| launcher | `bin/opendata` (Linux), `bin\opendata64.exe` (Windows); `envVarBaseName=OPENDATA` (`OPENDATA_VM_OPTIONS`, `OPENDATA_JDK`) |
| icon, splash, About | значок и splash генерируются в `opendata-branding.jar`, About показывает OpenData IDE |
| default и bundled plugins | `plugins/`: `grid-core-plugin`, `platform-structureView-plugin`, `opendata-db`; essential: `io.opendata.db`, `intellij.grid.core.plugin` |
| VM options | `bin/opendata64.exe.vmoptions` / `bin/opendata64.vmoptions` + `additionalJvmArguments` в `product-info.json` |
| system и config paths | `-Didea.paths.selector=OpenData2026.2`: `%APPDATA%\OpenData\OpenData2026.2`, `~/.config/OpenData/OpenData2026.2` и т. п. |

Подробности — в [`docs/architecture.md`](../../docs/architecture.md#продукт).

## Почему не вариант с Database Tools

В 0.1.0 рассматривались два варианта: «дистрибутив поверх DataGrip» и «свой product configuration с бинарным
Database Tools». Второй невозможен: закрытый плагин требует модуль `com.intellij.modules.database-capable`,
его объявляют только продукты JetBrains, а лицензия не разрешает распространять плагин в составе стороннего продукта.
Поэтому DB-функциональность реализована в `opendata-db` поверх JDBC и открытого `intellij.grid`.
