package io.opendata.db.grid

import com.intellij.database.connection.throwable.info.SimpleErrorInfo
import com.intellij.database.datagrid.GridColumn
import com.intellij.database.datagrid.GridMutator
import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.datagrid.GridRow
import com.intellij.database.datagrid.ModelIndex
import com.intellij.database.datagrid.ModelIndexSet
import com.intellij.database.datagrid.MutationType
import com.intellij.database.datagrid.mutating.CellMutation
import com.intellij.database.datagrid.mutating.MutationData
import com.intellij.database.run.ReservedCellValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.util.containers.JBIterable

/** Описание редактируемой таблицы и способ применить к ней изменения. */
interface TableTarget {
    /** Полное имя таблицы для SQL. */
    val qualifiedName: String
    /** Колонки первичного ключа; если пусто — строки идентифицируются всеми исходными значениями. */
    val keyColumns: List<String>
    fun quote(identifier: String): String
    /** Выполняет DML-операции одной транзакцией (вне EDT). Возвращает null или текст ошибки. */
    fun apply(statements: List<Dml>): Throwable?
    /** Перечитывает данные таблицы (вне EDT) и возвращает их для модели. */
    fun reload(): io.opendata.db.session.StatementResult.Rows
}

data class Dml(val sql: String, val params: List<Any?>)

/**
 * Мутатор DataGrid для таблиц БД (ТЗ 19): копит изменения ячеек, вставленные/удалённые строки;
 * Submit генерирует UPDATE/INSERT/DELETE по первичному ключу; Revert отменяет.
 */
class TableMutator(private val hookUp: TableHookUp, private val target: TableTarget) :
    GridMutator.DatabaseMutator<GridRow, GridColumn> {

    private val modified = LinkedHashMap<Int, MutableMap<Int, Any?>>()
    private val deleted = LinkedHashSet<Int>()
    /** Вставленные строки: значения по колонкам (отсутствует ключ — значение по умолчанию). */
    private val inserted = ArrayList<MutableMap<Int, Any?>>()
    @Volatile private var failed = false

    private val model get() = hookUp.listModel
    private val baseCount get() = model.rowCount

    private fun rowIdx(i: Int): ModelIndex<GridRow> = ModelIndex.forRow(model, i)
    private fun isInsertedIdx(i: Int) = i >= baseCount && i - baseCount < inserted.size
    private fun columnName(c: Int): String = model.getColumn(ModelIndex.forColumn(model, c))!!.name

    // --- GridMutator ---
    override fun isUpdateSafe(rowIndices: ModelIndexSet<GridRow>, columnIndices: ModelIndexSet<GridColumn>, newValue: Any?) = true
    override fun hasPendingChanges(): Boolean = modified.isNotEmpty() || deleted.isNotEmpty() || inserted.isNotEmpty()
    override fun hasUnparsedValues(): Boolean = false
    override fun isUpdateImmediately(): Boolean = false

    override fun mutate(source: GridRequestSource, row: ModelIndexSet<GridRow>, column: ModelIndexSet<GridColumn>, newValue: Any?, allowImmediateUpdate: Boolean) {
        for (r in row.asIterable()) for (c in column.asIterable()) setCell(r.asInteger(), c.asInteger(), newValue)
        changed(source)
    }

    override fun mutate(source: GridRequestSource, mutations: List<CellMutation>, allowImmediateUpdate: Boolean) {
        mutations.forEach { setCell(it.row.asInteger(), it.column.asInteger(), it.value) }
        changed(source)
    }

    private fun setCell(r: Int, c: Int, value: Any?) {
        if (isInsertedIdx(r)) {
            inserted[r - baseCount][c] = value
            return
        }
        val original = model.getValueAt(rowIdx(r), ModelIndex.forColumn(model, c))
        val cells = modified.getOrPut(r) { LinkedHashMap() }
        if (value == original || (value == ReservedCellValue.NULL && original == null)) cells.remove(c) else cells[c] = value
        if (cells.isEmpty()) modified.remove(r)
    }

    private fun changed(source: GridRequestSource) = hookUp.notifyRequestFinished(source, true)

    // --- RowsMutator ---
    override fun deleteRows(source: GridRequestSource, rows: ModelIndexSet<GridRow>) {
        val toRemoveInserted = ArrayList<Int>()
        for (r in rows.asIterable().map { it.asInteger() }) {
            if (isInsertedIdx(r)) toRemoveInserted += r - baseCount else deleted += r
        }
        toRemoveInserted.sortedDescending().forEach { inserted.removeAt(it) }
        changed(source)
    }

    override fun insertRows(source: GridRequestSource, amount: Int) {
        repeat(amount) { inserted += LinkedHashMap() }
        changed(source)
    }

    override fun cloneRow(source: GridRequestSource, toClone: ModelIndex<GridRow>) {
        val r = toClone.asInteger()
        val values = LinkedHashMap<Int, Any?>()
        for (c in 0 until model.columnCount) {
            values[c] = if (isInsertedIdx(r)) inserted[r - baseCount][c] else (modified[r]?.get(c) ?: model.getValueAt(rowIdx(r), ModelIndex.forColumn(model, c)))
        }
        // Колонки ключа не копируются: значение по умолчанию (serial/identity).
        target.keyColumns.forEach { key -> (0 until model.columnCount).firstOrNull { columnName(it) == key }?.let { values.remove(it) } }
        inserted += values
        changed(source)
    }

    override fun isDeletedRow(row: ModelIndex<GridRow>): Boolean = row.asInteger() in deleted
    override fun isDeletedRows(rows: ModelIndexSet<GridRow>): Boolean = rows.asIterable().all { it.asInteger() in deleted }
    override fun isInsertedRow(row: ModelIndex<GridRow>): Boolean = isInsertedIdx(row.asInteger())
    override fun getInsertedRowsCount(): Int = inserted.size
    override fun getLastInsertedRow(): ModelIndex<GridRow>? = if (inserted.isEmpty()) null else rowIdx(baseCount + inserted.size - 1)
    override fun getAffectedRows(): ModelIndexSet<GridRow> =
        ModelIndexSet.forRows(model, *(modified.keys + deleted + inserted.indices.map { baseCount + it }).toIntArray())
    override fun getInsertedRows(): JBIterable<ModelIndex<GridRow>> = JBIterable.from(inserted.indices.map { rowIdx(baseCount + it) })

    // --- DatabaseMutator ---
    override fun getMutationType(row: ModelIndex<GridRow>): MutationType? {
        val r = row.asInteger()
        return when {
            isInsertedIdx(r) -> MutationType.INSERT
            r in deleted -> MutationType.DELETE
            r in modified -> MutationType.MODIFY
            else -> null
        }
    }

    override fun getMutationType(row: ModelIndex<GridRow>, column: ModelIndex<GridColumn>): MutationType? {
        val r = row.asInteger()
        return when {
            isInsertedIdx(r) -> MutationType.INSERT
            r in deleted -> MutationType.DELETE
            modified[r]?.containsKey(column.asInteger()) == true -> MutationType.MODIFY
            else -> null
        }
    }

    override fun getMutation(row: ModelIndex<GridRow>, column: ModelIndex<GridColumn>): MutationData? {
        val r = row.asInteger()
        val c = column.asInteger()
        if (isInsertedIdx(r)) {
            val cells = inserted[r - baseCount]
            return MutationData(if (cells.containsKey(c)) cells[c] else ReservedCellValue.DEFAULT)
        }
        val cells = modified[r] ?: return null
        return if (cells.containsKey(c)) MutationData(cells[c]) else null
    }

    override fun hasUnparsedValues(row: ModelIndex<GridRow>): Boolean = false
    override fun isFailed(): Boolean = failed

    override fun hasMutatedRows(rows: ModelIndexSet<GridRow>, columns: ModelIndexSet<GridColumn>): Boolean =
        rows.asIterable().any { r -> getMutationType(r) != null }

    override fun revert(source: GridRequestSource, rows: ModelIndexSet<GridRow>, columns: ModelIndexSet<GridColumn>) {
        val cols = columns.asIterable().map { it.asInteger() }.toSet()
        val removeInserted = ArrayList<Int>()
        for (r in rows.asIterable().map { it.asInteger() }) {
            when {
                isInsertedIdx(r) -> removeInserted += r - baseCount
                else -> {
                    deleted.remove(r)
                    modified[r]?.let { cells -> if (cols.isEmpty()) cells.clear() else cells.keys.removeAll(cols) }
                    if (modified[r]?.isEmpty() == true) modified.remove(r)
                }
            }
        }
        removeInserted.sortedDescending().forEach { inserted.removeAt(it) }
        changed(source)
    }

    fun revertAll(source: GridRequestSource) {
        modified.clear(); deleted.clear(); inserted.clear()
        changed(source)
    }

    override fun getPendingChanges(): String = buildDml(includeInserted = true).joinToString(";\n") { it.sql } +
        if (hasPendingChanges()) ";" else ""

    /** DML по накопленным изменениям. Значения передаются параметрами PreparedStatement. */
    fun buildDml(includeInserted: Boolean): List<Dml> {
        val out = ArrayList<Dml>()
        val colNames = (0 until model.columnCount).map { columnName(it) }
        val keyIdx = target.keyColumns.mapNotNull { k -> colNames.indexOf(k).takeIf { it >= 0 } }
            .ifEmpty { colNames.indices.toList() }

        fun where(r: Int): Pair<String, List<Any?>> {
            val params = ArrayList<Any?>()
            val cond = keyIdx.joinToString(" AND ") { c ->
                val v = model.getValueAt(rowIdx(r), ModelIndex.forColumn(model, c))
                if (v == null) "${target.quote(colNames[c])} IS NULL" else { params += v; "${target.quote(colNames[c])} = ?" }
            }
            return cond to params
        }

        for (r in deleted.sorted()) {
            val (cond, p) = where(r)
            out += Dml("DELETE FROM ${target.qualifiedName} WHERE $cond", p)
        }
        for ((r, cells) in modified) {
            if (r in deleted || cells.isEmpty()) continue
            val params = ArrayList<Any?>()
            val set = cells.entries.joinToString(", ") { (c, v) ->
                when (v) {
                    ReservedCellValue.DEFAULT -> "${target.quote(colNames[c])} = DEFAULT"
                    ReservedCellValue.NULL, null -> "${target.quote(colNames[c])} = NULL"
                    else -> { params += v; "${target.quote(colNames[c])} = ?" }
                }
            }
            val (cond, p) = where(r)
            out += Dml("UPDATE ${target.qualifiedName} SET $set WHERE $cond", params + p)
        }
        if (includeInserted) for (cells in inserted) {
            val explicit = cells.filterValues { it != ReservedCellValue.DEFAULT && it != ReservedCellValue.UNSET }
            if (explicit.isEmpty()) {
                out += Dml("INSERT INTO ${target.qualifiedName} DEFAULT VALUES", emptyList())
                continue
            }
            val params = ArrayList<Any?>()
            val names = explicit.keys.joinToString(", ") { target.quote(colNames[it]) }
            val values = explicit.values.joinToString(", ") { v ->
                if (v == null || v == ReservedCellValue.NULL) "NULL" else { params += v; "?" }
            }
            out += Dml("INSERT INTO ${target.qualifiedName} ($names) VALUES ($values)", params)
        }
        return out
    }

    /** Submit (ТЗ 19): выполняет DML в фоне, затем перечитывает таблицу. */
    override fun submit(source: GridRequestSource, includeInserted: Boolean) {
        if (!hasPendingChanges()) return
        val dml = buildDml(includeInserted)
        hookUp.notifyRequestStarted(source)
        ApplicationManager.getApplication().executeOnPooledThread {
            val error = target.apply(dml)
            val reloaded = if (error == null) runCatching { target.reload() }.getOrNull() else null
            ApplicationManager.getApplication().invokeLater({
                if (error != null) {
                    failed = true
                    hookUp.notifyRequestError(source, SimpleErrorInfo.create(error.message ?: error.toString(), error))
                    hookUp.notifyRequestFinished(source, false)
                } else {
                    failed = false
                    modified.clear(); deleted.clear(); inserted.clear()
                    reloaded?.let { ResultGrids.fill(hookUp, it) }
                    hookUp.notifyRequestFinished(source, true)
                }
            }, ModalityState.any())
        }
    }

    // --- ColumnsMutator: изменение структуры таблицы из grid не поддерживается (DDL — через консоль) ---
    override fun deleteColumns(source: GridRequestSource, columns: ModelIndexSet<GridColumn>) {}
    override fun insertColumn(source: GridRequestSource, name: String?) {}
    override fun cloneColumn(source: GridRequestSource, toClone: ModelIndex<GridColumn>) {}
    override fun getInsertedColumnsCount(): Int = 0
    override fun getInsertedColumns(): JBIterable<ModelIndex<GridColumn>> = JBIterable.empty()
    override fun isInsertedColumn(idx: ModelIndex<GridColumn>): Boolean = false
    override fun isDeletedColumn(idx: ModelIndex<GridColumn>): Boolean = false
    override fun getInsertedColumn(idx: ModelIndex<GridColumn>): GridColumn? = null
    override fun renameColumn(source: GridRequestSource, idx: ModelIndex<GridColumn>, newName: String) {}
}
