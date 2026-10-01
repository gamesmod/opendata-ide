# Этап 5 — продукт OpenData IDE

> По ТЗ (раздел 41) к этому этапу переходим только после успешного POC (этапы 1–4) и юридической
> проверки (roadmap, R-6). Код этапа не пишется до тех пор, пока research не подтвердит реальные API
> product configuration в выбранной версии intellij-community (ТЗ, раздел 38).

## Вариант A — дистрибутив поверх установленной IDE (3–5 чд)

OpenData IDE поставляется как набор:

- установленный DataGrip или IntelliJ IDEA (Database Tools уже входит в него, лицензия JetBrains не нарушается);
- плагин(ы) OpenData (`opendata/integration` и далее);
- отдельный launcher (`OpenData.cmd` / ярлык) со своими каталогами `idea.config.path` и `idea.system.path`
  через `IDEA_PROPERTIES` / `DATAGRIP_PROPERTIES`, предустановленными настройками и Data Source-шаблонами,
  а также набором включённых и отключённых плагинов (`disabled_plugins.txt`).

Плюсы: минимум кода и рисков, обновления IDE штатные. Минусы: About, splash, иконка и product code
остаются от JetBrains.

## Вариант B — собственный product configuration (15–25 чд)

Сборка продукта из intellij-community: собственный `ProductProperties`, ApplicationInfo
(имя, product code, иконки, splash, About), layout bundled-плагинов и Windows launcher.
Database Tools and SQL входит в продукт бинарно из локальной установки (ТЗ 8.2).

Что нужно подтвердить research'ем до начала работ:

1. реальные классы и точки расширения build-скриптов (`platform/build-scripts`) в выбранном теге intellij-community;
2. совместимость бинарного плагина `com.intellij.database` с платформой, собранной из исходников той же build-линии;
3. проверку лицензии и product code плагином: может ли он работать в продукте не от JetBrains (риск R1).

Если пункт 3 даёт отрицательный ответ, остаётся вариант A.

## Требования ТЗ (раздел 10) и где они выполняются

| Требование | Вариант A | Вариант B |
|---|---|---|
| product name, product code | — (от базовой IDE) | ApplicationInfo |
| launcher, icon | ярлык/скрипт OpenData | собственный launcher |
| splash, About | — | ApplicationInfo |
| default и bundled plugins | `disabled_plugins.txt` + плагины OpenData | layout продукта |
| VM options, system и config paths | `*.vmoptions` + `idea.properties` OpenData | product properties |
