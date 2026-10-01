# docs/research — сырые данные research (не хранятся в репозитории)

Здесь `scripts/research.ps1` / `scripts/research.sh` сохраняют выгрузки из **вашей** установки DataGrip / IntelliJ IDEA:

- `api/*.txt` — публичные сигнатуры ключевых классов Database Tools (`javap -public`);
- `class-index.tsv` — индекс классов `com.intellij.database.*`, `com.intellij.sql.*`, grid;
- `raw-descriptors.md` — actions и extensions из `plugin.xml` и module descriptors.

Это производные данные закрытого плагина JetBrains, поэтому в публичный репозиторий они не попадают.
Ссылки «сигнатуры» в `docs/jetbrains-db-analysis.md` и `docs/grid-analysis.md` начинают работать после
локального запуска research.
