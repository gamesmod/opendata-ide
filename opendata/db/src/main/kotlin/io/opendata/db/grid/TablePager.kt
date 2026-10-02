package io.opendata.db.grid

import com.intellij.database.datagrid.GridColumn
import com.intellij.database.datagrid.GridLoader
import com.intellij.database.datagrid.GridModel
import com.intellij.database.datagrid.GridPagingModel
import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.datagrid.GridRow
import com.intellij.database.datagrid.ModelIndex

/**
 * Серверная пагинация редактора таблицы (ТЗ 18, 20: paging / fetch more / limit) через LIMIT/OFFSET.
 * Штатные действия grid (First/Previous/Next/Last page, размер страницы) работают через этот класс.
 */
class TablePager(
    private val model: () -> GridModel<GridRow, GridColumn>,
    /** Загружает страницу (offset, limit) вне EDT, заполняет модель и сообщает о завершении через source. */
    private val loadPage: (source: GridRequestSource, offset: Long, limit: Int) -> Unit,
    /** Считает строки (SELECT count(*)) вне EDT; результат — через [totalCountReceived]. */
    private val countRows: (source: GridRequestSource) -> Unit,
) : GridPagingModel<GridRow, GridColumn>, GridLoader {

    @Volatile var offset: Long = 0
        private set
    @Volatile private var pageSize: Int = DEFAULT_PAGE_SIZE
    @Volatile private var total: Long = -1
    @Volatile private var lastPageReached = false

    /** Вызывается после загрузки страницы: сколько строк реально пришло. */
    fun pageLoaded(offset: Long, fetched: Int) {
        this.offset = offset
        lastPageReached = pageSize < 0 || fetched < pageSize
        if (lastPageReached) total = offset + fetched
    }

    @Volatile private var loadLastAfterCount = false

    /** Результат count(*); если его ждала загрузка последней страницы — продолжает её. */
    fun totalCountReceived(source: GridRequestSource, count: Long) {
        total = count
        if (loadLastAfterCount) {
            loadLastAfterCount = false
            loadLastPage(source)
        }
    }

    val limit: Int get() = pageSize

    // --- GridPagingModel ---
    override fun isFirstPage(): Boolean = offset == 0L
    override fun isLastPage(): Boolean = lastPageReached || (total >= 0 && offset + model().rowCount >= total)
    override fun setPageSize(pageSize: Int) {
        this.pageSize = if (pageSize == GridPagingModel.UNSET_PAGE_SIZE) DEFAULT_PAGE_SIZE else pageSize
    }
    override fun getPageSize(): Int = pageSize
    override fun pageSizeSet(): Boolean = true
    override fun getTotalRowCount(): Long = if (total >= 0) total else offset + model().rowCount
    override fun isTotalRowCountPrecise(): Boolean = total >= 0
    override fun isTotalRowCountUpdateable(): Boolean = true
    override fun getPageStart(): Int = (offset + 1).toInt()
    override fun getPageEnd(): Int = (offset + model().rowCount).toInt()
    override fun findRow(rowDataIdx: Int): ModelIndex<GridRow> {
        val idx = if (rowDataIdx >= pageStart && rowDataIdx <= pageEnd) rowDataIdx - pageStart else -1
        return ModelIndex.forRow(model(), idx)
    }

    // --- GridLoader ---
    override fun reloadCurrentPage(source: GridRequestSource) = loadPage(source, offset, pageSize)
    override fun loadNextPage(source: GridRequestSource) = loadPage(source, if (pageSize < 0) 0 else offset + pageSize, pageSize)
    override fun loadPreviousPage(source: GridRequestSource) = loadPage(source, if (pageSize < 0) 0 else maxOf(0, offset - pageSize), pageSize)
    override fun loadFirstPage(source: GridRequestSource) = loadPage(source, 0, pageSize)
    override fun loadLastPage(source: GridRequestSource) {
        if (total < 0 || pageSize < 0) {
            // Последняя страница требует количества строк: сначала count(*), затем загрузка (см. totalCountReceived).
            loadLastAfterCount = true
            countRows(source)
            return
        }
        loadPage(source, maxOf(0, ((total - 1) / pageSize) * pageSize), pageSize)
    }
    override fun load(source: GridRequestSource, offset: Int) = loadPage(source, maxOf(0, offset - 1).toLong(), pageSize)
    override fun updateTotalRowCount(source: GridRequestSource) = countRows(source)
    override fun applyFilterAndSorting(source: GridRequestSource) = loadPage(source, 0, pageSize)
    override fun updateIsTotalRowCountUpdateable() {}

    companion object {
        const val DEFAULT_PAGE_SIZE = 500
    }
}
