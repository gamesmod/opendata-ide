package io.opendata.db.grid

import com.intellij.database.datagrid.CachedGridDataHookUp
import com.intellij.database.datagrid.DataConsumer
import com.intellij.database.datagrid.DataGrid
import com.intellij.database.datagrid.DataGridListModel
import com.intellij.database.datagrid.GridColumn
import com.intellij.database.datagrid.GridMutator
import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.datagrid.GridRow
import com.intellij.database.datagrid.GridUtil
import com.intellij.database.run.ui.DataGridRequestPlace
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import io.opendata.db.session.ColumnInfo
import io.opendata.db.session.StatementResult
import java.util.function.BiConsumer
import javax.swing.JComponent

/** Построение штатного DataGrid (открытый intellij.grid) для результатов JDBC. */
object ResultGrids {

    fun newModel(): DataGridListModel = DataGridListModel { a, b -> a == b }

    fun columns(columns: List<ColumnInfo>): List<GridColumn> = columns.mapIndexed { i, c ->
        DataConsumer.Column(i, c.name, c.sqlType, c.typeName ?: "", c.className ?: "java.lang.Object")
    }

    fun rows(rows: List<Array<Any?>>): List<GridRow> = rows.mapIndexed { i, values -> DataConsumer.Row.create(i, values) }

    /**
     * Заполняет модель (на EDT) через штатный GridStorageAndModelUpdater: колонки заменяются, а grid получает
     * события columnsRemoved/Added, rowsRemoved/Added и afterLastRowAdded — иначе уже созданный DataGrid не обновится.
     */
    fun fill(hookUp: CachedGridDataHookUp, result: StatementResult.Rows) {
        val model = hookUp.dataModel as DataGridListModel
        val updater = com.intellij.database.run.ui.grid.GridStorageAndModelUpdater(
            model, hookUp.mutationModel as com.intellij.database.run.ui.grid.GridMutationModel, null)
        model.isUpdatingNow = true
        updater.removeRows(0, model.rowCount)
        updater.setColumns(columns(result.columns))
        updater.addRows(rows(result.rows))
        updater.afterLastRowAdded()
    }

    /** Результат запроса консоли: только чтение, но с сортировкой, фильтром, копированием, выделением. */
    fun createReadOnlyGrid(project: Project, result: StatementResult.Rows, parent: Disposable): DataGrid {
        val hookUp = CachedGridDataHookUp(project, newModel())
        fill(hookUp, result)
        return create(project, hookUp, parent)
    }

    fun create(project: Project, hookUp: CachedGridDataHookUp, parent: Disposable): DataGrid {
        val grid = GridUtil.createDataGrid(project, hookUp, GridUtil.getGridPopupActions(), BiConsumer { g, appearance ->
            // Штатная настройка grid (как у табличного CSV-редактора): настройки, редакторы и рендереры ячеек,
            // форматтеры значений; затем собственный GridHelper — редактирование только при наличии мутатора.
            GridUtil.configureCsvTable(g, appearance)
            com.intellij.database.datagrid.GridHelper.set(g, OpenDataGridHelper())
            GridUtil.configureFullSizeTable(g, appearance)
        })
        Disposer.register(parent, grid)
        return grid
    }

    fun component(grid: DataGrid): JComponent = grid.panel.component

    fun source(grid: DataGrid): GridRequestSource = GridRequestSource(DataGridRequestPlace(grid))
}

/**
 * Hook-up редактора таблицы: данные страницы + собственный мутатор, генерирующий DML,
 * и (опционально) серверная пагинация через [pager].
 */
class TableHookUp(
    project: Project,
    model: DataGridListModel,
    private val readOnly: Boolean,
    private val pager: TablePager?,
    private val mutatorFactory: (TableHookUp) -> TableMutator,
) : CachedGridDataHookUp(project, model) {
    val mutator: TableMutator by lazy { mutatorFactory(this) }
    val listModel: DataGridListModel = model
    override fun getMutator(): GridMutator<GridRow, GridColumn>? = if (readOnly) null else mutator
    override fun isReadOnly(): Boolean = readOnly
    override fun getPageModel(): com.intellij.database.datagrid.GridPagingModel<GridRow, GridColumn> = pager ?: super.getPageModel()
    override fun getLoader(): com.intellij.database.datagrid.GridLoader = pager ?: super.getLoader()
}

/** GridHelper OpenData: редактирование разрешено, только если hook-up предоставляет мутатор (редактор таблицы). */
class OpenDataGridHelper : com.intellij.database.datagrid.GridHelperImpl() {
    private fun editable(grid: com.intellij.database.datagrid.CoreGrid<GridRow, GridColumn>) =
        !grid.dataHookup.isReadOnly && grid.dataHookup.mutator != null

    override fun isEditable(grid: com.intellij.database.datagrid.CoreGrid<GridRow, GridColumn>): Boolean = editable(grid)
    override fun hasTargetForEditing(grid: com.intellij.database.datagrid.CoreGrid<GridRow, GridColumn>): Boolean = editable(grid)
    override fun canAddRow(grid: com.intellij.database.datagrid.CoreGrid<GridRow, GridColumn>): Boolean = editable(grid)
    override fun canMutateColumns(grid: com.intellij.database.datagrid.CoreGrid<GridRow, GridColumn>): Boolean = false
}
