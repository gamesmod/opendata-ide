# Сборка OpenData IDE

## Требования

| Компонент | Версия | Как проверяется |
|---|---|---|
| JDK | 21 (с `javac`) | `build.ps1`: `JAVA_HOME`, затем `java` в PATH. Сама IDE 2026.2 работает на JBR 25, но плагин собирается под 21: классы платформы совместимы |
| JetBrains IDE | DataGrip или IntelliJ IDEA с плагином Database Tools and SQL; baseline не ниже `pluginSinceBuild` из `gradle.properties` | `research --check`, затем `ide.properties` |
| Gradle | wrapper `9.8.0` (скачивается автоматически) | `gradlew` |
| IntelliJ Platform Gradle Plugin | `intellijPlatformPluginVersion` в `gradle.properties` | при первой сборке |
| Kotlin | `kotlinVersion` в `gradle.properties` (не новее Kotlin stdlib в IDE, см. compatibility-matrix) | — |
| Docker | для тестовой PostgreSQL (необязательно) | `test.ps1 -WithPostgres` |

Сети нужны Maven Central, Gradle Plugin Portal и services.gradle.org. Если IDE задана локально
(основной режим), саму платформу из репозитория JetBrains скачивать не нужно.

## Поиск IDE (ТЗ 8.2, 29)

Путь к IDE не хардкодится. Скрипты ищут его в таком порядке:

1. параметр `-IdeHome` (`.sh`: первый аргумент);
2. `JETBRAINS_IDE_HOME`;
3. `DATAGRIP_HOME`;
4. стандартные каталоги установщиков: `%LOCALAPPDATA%\Programs`, `%LOCALAPPDATA%\JetBrains\Toolbox\apps`,
   `%ProgramFiles%\JetBrains` (а также `/opt`, Toolbox и `/Applications`). Среди найденных установок,
   где есть Database Tools, выбирается DataGrip, иначе самая свежая сборка.

Для macOS указывайте путь к `.app` (например, `/Applications/DataGrip.app`).

## Команды

```powershell
.\scripts\research.ps1 [-IdeHome <path>] [-CommunityHome <intellij-community>]
.\scripts\build.ps1    [-IdeHome <path>] [-SkipTests] [-SkipResearch]
.\scripts\test.ps1     [-IdeHome <path>] [-WithPostgres]
.\scripts\run.ps1      [-IdeHome <path>] [-Poc]
```

Те же операции напрямую через Gradle:

```powershell
.\gradlew.bat research "-PlocalIdePath=C:\...\DataGrip 2025.2"
.\gradlew.bat :opendata:integration:buildPlugin :opendata:integration:test "-PlocalIdePath=..."
.\gradlew.bat :opendata:integration:runIde "-PlocalIdePath=..."
```

Если `localIdePath` не задан, платформа скачивается из репозитория JetBrains по
`platformType` и `platformVersion` из `gradle.properties` (запасной режим).

## Что проверяет build.ps1

1. JDK 21+ с `javac`.
2. Установку IDE: `product-info.json`.
3. `Research.java --check`: есть ли `com.intellij.database`, build number, JBR → `build/research/ide.properties`.
4. Совместимость: baseline IDE ≥ `pluginSinceBuild`; JBR major не ниже 21 (иначе предупреждение).
5. Полный research → `docs/*`.
6. `buildPlugin` + `test`. Готовый плагин лежит в `opendata/integration/build/distributions/*.zip`.

При любой ошибке сборка останавливается с понятным сообщением (ТЗ, раздел 39: к следующему этапу не переходить).

## Проверенная конфигурация

| Компонент | Версия |
|---|---|
| DataGrip | 2026.2.6, build DB-262.10968.148 (JBR 25.0.4, Kotlin stdlib 2.4.0) |
| IntelliJ Platform Gradle Plugin | 2.19.0 |
| Kotlin | 2.4.20 |
| Gradle | 9.8.0 |
| JDK сборки | 21 |
| Test framework | 262.10968.138 (подбирается IPGP автоматически) |
| Результат | `buildPlugin` OK, `test` 16/16 OK |

### Известные особенности

- **HTTP 429 от Maven Central.** При первой загрузке зависимостей Maven Central может ограничить частоту запросов.
  Повторите сборку; при необходимости добавьте `--max-workers=1`. Репозитории IntelliJ Platform стоят в
  `build.gradle.kts` первыми, а группы `bundled*` и `localIde*` исключены из Maven Central, чтобы виртуальные
  артефакты локальной IDE не запрашивались по сети.
- **JetBrains User Agreement.** При первом `runIde` песочница показывает пользовательское соглашение JetBrains,
  его принимает пользователь. Тесты (`test`) соглашения не требуют.
- **Прокси.** Если Java получает настройки прокси через `JAVA_TOOL_OPTIONS`, не очищайте эту переменную перед вызовом `gradlew`.

## Первая сборка: на что обратить внимание

Версии IntelliJ Platform Gradle Plugin и Kotlin в `gradle.properties` заданы консервативно. Если ваша
IDE новее (2025.3 / 2026.x), а сборка падает на этапе конфигурации IntelliJ Platform, поднимите
`intellijPlatformPluginVersion` до актуальной 2.x и `kotlinVersion` до версии Kotlin stdlib вашей IDE
(её показывает `docs/compatibility-matrix.md`). После успешной сборки поменяйте статус строки
в матрице на `build OK`.

## CI и релизы

GitHub Actions (`.github/workflows/build.yml`) на каждый push и pull request:

1. скачивает DataGrip (`DATAGRIP_VERSION`) и кэширует его;
2. поднимает PostgreSQL 16 и выполняет `docker/postgres/init/01-opendata.sql`;
3. запускает research и проверку совместимости, затем `buildPlugin` и `test`;
4. публикует артефакты: ZIP плагина и результаты тестов.

**Релиз.** Поднимите `version` в `gradle.properties` и запушьте в `main`. После зелёной сборки workflow сам создаст
тег `v<version>` и GitHub Release с `opendata-integration-<version>.zip` и описанием из `docs/release-notes.md`.

