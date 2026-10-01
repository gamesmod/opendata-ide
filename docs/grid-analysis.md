# Анализ intellij.grid / Data Editor and Viewer

> Сгенерировано `tools/research/Research.java` 2026-10-01 по установке `DataGrip 2026.2.6` (build `DB-262.10968.148`).
> Все перечисленные классы/EP/сервисы реально найдены в этой сборке. Повторный запуск перезаписывает файл.

## 1. Модули и дескрипторы grid в установке

| Дескриптор | Тип | Владелец | JAR | depends / dependencies |
|---|---|---|---|---|
| `intellij.grid` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.jar` | `intellij.platform.core` |
| `intellij.grid.core.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` | `intellij.grid`, `intellij.grid.csv.core.impl`, `intellij.grid.types`, `intellij.libraries.fastutil`, `intellij.libraries.kotlinx.collections.immutable`, `intellij.platform.analysis`, `intellij.platform.core`, `intellij.platform.core.impl`, `intellij.platform.core.ui`, `intellij.platform.editor.ui`, `intellij.platform.ide.core`, `intellij.platform.projectModel`, `intellij.platform.statistics`, `intellij.platform.util.coroutines` |
| `intellij.grid.core.plugin` | plugin | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/grid-core-plugin.jar` | — |
| `intellij.grid.csv.core.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.csv.core.impl.jar` | `intellij.grid.types`, `intellij.libraries.automaton`, `intellij.libraries.kotlinx.collections.immutable`, `intellij.platform.core`, `intellij.platform.core.impl`, `intellij.platform.projectModel` |
| `intellij.grid.impl` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` | `intellij.grid.core.impl`, `intellij.grid.csv.core.impl`, `intellij.grid.types`, `intellij.libraries.fastutil`, `intellij.libraries.kotlinx.collections.immutable`, `intellij.libraries.kotlinx.serialization.core`, `intellij.libraries.microba`, `intellij.platform.analysis`, `intellij.platform.codeStyle`, `intellij.platform.core`, `intellij.platform.core.impl`, `intellij.platform.core.ui`, `intellij.platform.diff`, `intellij.platform.editor.ex`, `intellij.platform.editor.ui`, `intellij.platform.execution`, `intellij.platform.execution.impl`, `intellij.platform.ide`, `intellij.platform.ide.core`, `intellij.platform.ide.core.plugins`, `intellij.platform.ide.impl`, `intellij.platform.indexing`, `intellij.platform.lang`, `intellij.platform.lang.core`, `intellij.platform.lang.impl`, `intellij.platform.projectModel`, `intellij.platform.projectModel.impl`, `intellij.platform.statistics`, `intellij.platform.structureView`, `intellij.platform.util.coroutines`, `intellij.platform.util.ui`, `intellij.xml.parser`, `intellij.xml.psi` |
| `intellij.grid.impl.ide` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.ide.jar` | `intellij.grid.core.impl`, `intellij.grid.impl`, `intellij.platform.core`, `intellij.platform.core.ui`, `intellij.platform.editor.ui`, `intellij.platform.ide.core`, `intellij.platform.ide.impl`, `intellij.platform.statistics`, `intellij.platform.util.ui` |
| `intellij.grid.types` | module | intellij.grid.core.plugin | `plugins/grid-core-plugin/lib/modules/intellij.grid.types.jar` | — |
| `intellij.grid.loader.json` | plugin | intellij.grid.loader.json | `plugins/grid-loader-json/lib/grid-loader-json.jar` | `intellij.grid.scripting.impl` |
| `intellij.grid.loader.xls` | plugin | intellij.grid.loader.xls | `plugins/grid-loader-xls/lib/grid-loader-xls.jar` | `intellij.grid.scripting.impl` |
| `intellij.grid.charts.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.charts.impl.jar` | `intellij.charts`, `intellij.grid.core.impl`, `intellij.grid.impl` |
| `intellij.grid.images.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.images.impl.jar` | `com.intellij.platform.images`, `intellij.grid.core.impl`, `intellij.grid.impl` |
| `intellij.grid.json.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.json.impl.jar` | `intellij.json.backend`, `intellij.grid.impl` |
| `intellij.grid.plugin` | plugin | intellij.grid.plugin | `plugins/grid-plugin/lib/grid-plugin.jar` | — |
| `intellij.grid.scripting.impl` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.scripting.impl.jar` | `intellij.grid.core.impl`, `intellij.grid.csv.core.impl`, `intellij.grid.impl`, `intellij.grid.scripting.rt` |
| `intellij.grid.scripting.rt` | module | intellij.grid.plugin | `plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar` | — |

## 2. Extension points grid

| EP | Interface/Bean | Дескриптор |
|---|---|---|
| `com.intellij.database.datagrid.objectNormalizerProvider` | `com.intellij.database.datagrid.ObjectNormalizerProvider` | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.formatterCreatorProvider` | `com.intellij.database.datagrid.FormatterCreatorProvider` | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.extractorsHelper` | `com.intellij.database.extractors.ExtractorsHelper` | `intellij.grid.core.impl` |
| `com.intellij.database.datagrid.valueEditorTab` | `com.intellij.database.run.ui.ValueEditorTab` | `intellij.grid.impl` |
| `com.intellij.database.datagrid.cellViewerFactory` | `com.intellij.database.run.ui.CellViewerFactory` | `intellij.grid.impl` |
| `com.intellij.database.minimizedFormatDetector` | `com.intellij.database.run.ui.MinimizedFormatDetector` | `intellij.grid.impl` |
| `com.intellij.database.datagrid.customToolbarProvider` | `com.intellij.database.datagrid.CustomGridToolbarProvider` | `intellij.grid.impl` |
| `com.intellij.grid.scripting.ivyLocalRepository` | `com.intellij.grid.scripting.impl.IvyLocalRepository` | `intellij.grid.scripting.impl` |

## 3. Пакеты grid

| Пакет | Классов | public | 🔒 Internal | JAR |
|---|---|---|---|---|
| `com.intellij.database.datagrid` | 159 | 158 | 1 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.color` | 10 | 10 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.mutating` | 14 | 14 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.datagrid.nested` | 1 | 1 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.datagrid.objects` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.grid.editors.lexers` | 1 | 1 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.run.ui.grid` | 38 | 38 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.run.ui.grid.documentation` | 4 | 4 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.ui.grid.editors` | 62 | 62 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.database.run.ui.grid.editors.lexers` | 2 | 2 | 0 | plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar |
| `com.intellij.database.run.ui.grid.renderers` | 9 | 9 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar, plugins/DatabaseTools/lib/modules/intellij.database.impl.jar |
| `com.intellij.database.run.ui.grid.selection` | 2 | 2 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar, plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar |
| `com.intellij.grid.charts.impl` | 5 | 4 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.charts.impl.jar |
| `com.intellij.grid.charts.impl.i18n` | 2 | 2 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.charts.impl.jar |
| `com.intellij.grid.core.impl.icons` | 1 | 1 | 1 | plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar |
| `com.intellij.grid.images.impl` | 2 | 2 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.images.impl.jar |
| `com.intellij.grid.impl.ide` | 1 | 1 | 0 | plugins/grid-core-plugin/lib/modules/intellij.grid.impl.ide.jar |
| `com.intellij.grid.json.impl` | 2 | 2 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.json.impl.jar |
| `com.intellij.grid.loader.json` | 1 | 1 | 0 | plugins/grid-loader-json/lib/grid-loader-json.jar |
| `com.intellij.grid.loader.xls` | 1 | 1 | 0 | plugins/grid-loader-xls/lib/grid-loader-xls.jar |
| `com.intellij.grid.scripting.impl` | 23 | 20 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.scripting.impl.jar |
| `com.intellij.grid.scripting.rt` | 4 | 4 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar |
| `com.intellij.grid.scripting.rt.bindings` | 1 | 1 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar |
| `com.intellij.grid.scripting.rt.impl` | 4 | 3 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar |
| `com.intellij.grid.scripting.rt.util` | 1 | 1 | 0 | plugins/grid-plugin/lib/modules/intellij.grid.scripting.rt.jar |

## 4. API для задач раздела 7 ТЗ

### Создание DataGrid

- `com.intellij.database.datagrid.DataGrid` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataGrid.txt)
- `com.intellij.database.datagrid.DataGridUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DataGridUtil.txt)
- `com.intellij.database.datagrid.GridEditorPanel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridHelper` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridHelper.txt)
- `com.intellij.database.datagrid.GridPanel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridUtil` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridUtil.txt)
- `com.intellij.database.run.ui.grid.GridFilterPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridMainPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridSortingPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Модель строк и колонок

- `com.intellij.database.datagrid.GridColumn` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridColumn.txt)
- `com.intellij.database.datagrid.GridColumnLayout` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridListModelBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridModel.txt)
- `com.intellij.database.datagrid.GridModelUtil` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridModelWithInjections` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridModelWithNestedTables` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridRow` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridRow.txt)
- `com.intellij.database.datagrid.mutating.GridColumnMutation` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.datagrid.mutating.GridRowMutation` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridModelUpdater` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridModelUpdaterUtil` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridRowComparator` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridRowHeader` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Передача данных / data hooks

- `com.intellij.database.datagrid.GridDataHookUp` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridDataHookUp.txt)
- `com.intellij.database.datagrid.GridDataHookUpBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridDataHookUpManager` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridLoader` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridLoaderBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridRequestSource` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridSortingModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridDataSupportImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridSortingPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Редактирование

- `com.intellij.database.datagrid.DatabaseMutator` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.connectivity.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.DatabaseMutator.txt)
- `com.intellij.database.datagrid.GridEditorPanel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridMutator` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar` — [сигнатуры](research/api/com.intellij.database.datagrid.GridMutator.txt)
- `com.intellij.database.datagrid.TypesMutator` (interface) — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.datagrid.mutating.GridColumnMutation` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.datagrid.mutating.GridRowMutation` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridEditorPanelBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridMutationModel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditor` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactories` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactory` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactoryImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactoryProvider` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorHelper` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorHelperImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Sorting / filtering

- `com.intellij.database.datagrid.GridFilterAndSortingComponent` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.datagrid.GridFilteringModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridFilteringModelImpl` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.datagrid.GridSortingModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.GridFilterAndSortingComponentImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridFilterPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridSortingPanel` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Copy / paste

- `com.intellij.database.run.ui.grid.GridCopyProvider` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridPasteProvider` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

### Paging / fetch more

- `com.intellij.database.datagrid.GridPagingModel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.datagrid.GridPagingModelImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`

### Renderers / editors

- `com.intellij.database.datagrid.GridEditorPanel` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.GridEditorPanelBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditor` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactories` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactory` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactoryImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorFactoryProvider` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorHelper` (interface) — `plugins/grid-core-plugin/lib/modules/intellij.grid.core.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorHelperImpl` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorTextField` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorUtilKt` — `plugins/DatabaseTools/lib/modules/intellij.database.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridCellEditorsUtil` — `plugins/DatabaseTools/lib/modules/intellij.database.core.impl.jar`
- `com.intellij.database.run.ui.grid.editors.GridTextCellEditorBase` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.renderers.GridCellRenderer` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`
- `com.intellij.database.run.ui.grid.renderers.GridCellRendererFactories` — `plugins/grid-core-plugin/lib/modules/intellij.grid.impl.jar`

## 5. Исходники intellij-community: grid/

Клон intellij-community не передан (`--community <dir>`).
