package io.opendata.db.meta

import io.opendata.db.model.DbKind
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet

enum class DbObjectKind(val isFolder: Boolean = false) {
    CATALOG, SCHEMA,
    TABLES_FOLDER(true), VIEWS_FOLDER(true), FUNCTIONS_FOLDER(true), SEQUENCES_FOLDER(true), TYPES_FOLDER(true),
    TABLE, VIEW, FUNCTION, SEQUENCE, TYPE,
    COLUMNS_FOLDER(true), KEYS_FOLDER(true), FOREIGN_KEYS_FOLDER(true), INDEXES_FOLDER(true), TRIGGERS_FOLDER(true), CONSTRAINTS_FOLDER(true),
    COLUMN, PRIMARY_KEY, FOREIGN_KEY, INDEX, TRIGGER, CONSTRAINT,
}

/** Объект базы данных в Database Explorer. catalog/schema/table — координаты для JDBC DatabaseMetaData. */
data class DbObject(
    val kind: DbObjectKind,
    val name: String,
    val catalog: String? = null,
    val schema: String? = null,
    val table: String? = null,
    val detail: String? = null,
) {
    val isLeaf: Boolean
        get() = kind in setOf(DbObjectKind.COLUMN, DbObjectKind.PRIMARY_KEY, DbObjectKind.FOREIGN_KEY, DbObjectKind.INDEX,
            DbObjectKind.TRIGGER, DbObjectKind.CONSTRAINT, DbObjectKind.FUNCTION, DbObjectKind.SEQUENCE, DbObjectKind.TYPE)

    /** Полное имя для SQL: "schema"."table" (или catalog.table для ClickHouse/Dremio). */
    fun qualifiedName(quote: (String) -> String): String =
        listOfNotNull(schema ?: catalog, table ?: name).joinToString(".") { quote(it) }
}

/**
 * Introspection через стандартный JDBC DatabaseMetaData (ТЗ 23, fallback) + запросы к pg_catalog там,
 * где JDBC не даёт данных (типы, триггеры, ограничения PostgreSQL/Cloudberry).
 */
class MetadataLoader(private val connection: Connection, private val kind: DbKind) {
    private val md: DatabaseMetaData = connection.metaData

    fun quote(identifier: String): String {
        // Dremio (Flight SQL JDBC) может не отдавать identifierQuoteString — используем стандартные двойные кавычки.
        val q = (if (kind == DbKind.DREMIO) null else runCatching { md.identifierQuoteString }.getOrNull())
            ?.trim().orEmpty().ifEmpty { "\"" }
        val plain = Regex("[a-z_][a-z0-9_]*")
        return if (kind.isPostgresFamily && plain.matches(identifier)) identifier
        else if (kind == DbKind.CLICKHOUSE && Regex("[A-Za-z_][A-Za-z0-9_]*").matches(identifier)) identifier
        else q + identifier.replace(q, q + q) + q
    }

    /** Верхний уровень: схемы (PostgreSQL, Cloudberry, Dremio) или базы данных (ClickHouse). */
    fun containers(showSystem: Boolean = false): List<DbObject> {
        val schemas = runCatching { rows(md.schemas) { DbObject(DbObjectKind.SCHEMA, it.getString("TABLE_SCHEM"), catalog = it.getStringOrNull("TABLE_CATALOG")) } }
            .getOrDefault(emptyList())
        val result = if (schemas.isNotEmpty()) schemas
        else runCatching { rows(md.catalogs) { DbObject(DbObjectKind.CATALOG, it.getString("TABLE_CAT"), catalog = it.getString("TABLE_CAT")) } }
            .getOrDefault(emptyList())
        return result.filter { showSystem || !isSystem(it.name) }.sortedWith(compareBy({ isSystem(it.name) }, { it.name.lowercase() }))
    }

    private fun isSystem(name: String): Boolean {
        val n = name.lowercase()
        return n == "information_schema" || n == "pg_catalog" || n.startsWith("pg_toast") || n.startsWith("pg_temp") ||
            n == "system" || n == "gp_toolkit" || n == "sys" || n == "information_schema_cluster"
    }

    fun folders(container: DbObject): List<DbObject> {
        val c = container
        val list = mutableListOf(
            c.copy(kind = DbObjectKind.TABLES_FOLDER, name = "Tables"),
            c.copy(kind = DbObjectKind.VIEWS_FOLDER, name = "Views"),
            c.copy(kind = DbObjectKind.FUNCTIONS_FOLDER, name = "Functions"),
        )
        if (kind.isPostgresFamily) {
            list += c.copy(kind = DbObjectKind.SEQUENCES_FOLDER, name = "Sequences")
            list += c.copy(kind = DbObjectKind.TYPES_FOLDER, name = "Types")
        }
        return list
    }

    /** Координаты контейнера: схема → (catalog, schema=name); каталог/БД ClickHouse → (catalog=name, schema=null). */
    fun scope(container: DbObject): DbObject = when (container.kind) {
        DbObjectKind.SCHEMA -> container.copy(schema = container.name)
        DbObjectKind.CATALOG -> container.copy(catalog = container.name, schema = null)
        else -> container
    }

    fun tables(folder: DbObject, views: Boolean): List<DbObject> {
        val schema = folder.schema
        val types = runCatching { rows(md.tableTypes) { it.getString(1).trim() } }.getOrDefault(emptyList())
        val wanted = types.filter { t ->
            val u = t.uppercase()
            if (views) u.contains("VIEW") && !u.startsWith("SYSTEM") && !u.startsWith("TEMPORARY")
            else (u == "TABLE" || u == "BASE TABLE" || u == "PARTITIONED TABLE" || u == "FOREIGN TABLE" || u == "REMOTE TABLE" ||
                (!kind.isPostgresFamily && u.contains("TABLE") && !u.contains("SYSTEM")))
        }.ifEmpty { listOf(if (views) "VIEW" else "TABLE") }
        return rows(md.getTables(folder.catalog, schema, "%", wanted.toTypedArray())) {
            val name = it.getString("TABLE_NAME")
            DbObject(if (views) DbObjectKind.VIEW else DbObjectKind.TABLE, name, it.getStringOrNull("TABLE_CAT"), it.getStringOrNull("TABLE_SCHEM"),
                table = name, detail = it.getStringOrNull("REMARKS")?.ifBlank { null } ?: it.getStringOrNull("TABLE_TYPE")?.lowercase())
        }.sortedBy { it.name.lowercase() }
    }

    fun functions(folder: DbObject): List<DbObject> {
        val fromFunctions = runCatching {
            rows(md.getFunctions(folder.catalog, folder.schema, "%")) {
                DbObject(DbObjectKind.FUNCTION, it.getString("FUNCTION_NAME"), it.getStringOrNull("FUNCTION_CAT"), it.getStringOrNull("FUNCTION_SCHEM"))
            }
        }.getOrDefault(emptyList())
        val list = fromFunctions.ifEmpty {
            runCatching {
                rows(md.getProcedures(folder.catalog, folder.schema, "%")) {
                    DbObject(DbObjectKind.FUNCTION, it.getString("PROCEDURE_NAME"), it.getStringOrNull("PROCEDURE_CAT"), it.getStringOrNull("PROCEDURE_SCHEM"))
                }
            }.getOrDefault(emptyList())
        }
        return list.distinctBy { it.name }.sortedBy { it.name.lowercase() }
    }

    fun sequences(folder: DbObject): List<DbObject> =
        rows(md.getTables(folder.catalog, folder.schema, "%", arrayOf("SEQUENCE"))) {
            DbObject(DbObjectKind.SEQUENCE, it.getString("TABLE_NAME"), it.getStringOrNull("TABLE_CAT"), it.getStringOrNull("TABLE_SCHEM"))
        }.sortedBy { it.name }

    /** Типы PostgreSQL: enum, composite, domain (pg_catalog). */
    fun types(folder: DbObject): List<DbObject> = query(
        """
        SELECT t.typname, CASE t.typtype WHEN 'e' THEN 'enum' WHEN 'c' THEN 'composite' WHEN 'd' THEN 'domain' ELSE t.typtype::text END
        FROM pg_type t JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE n.nspname = ? AND t.typtype IN ('e','c','d')
          AND (t.typtype <> 'c' OR EXISTS (SELECT 1 FROM pg_class c WHERE c.oid = t.typrelid AND c.relkind = 'c'))
        ORDER BY 1
        """.trimIndent(), folder.schema,
    ) { DbObject(DbObjectKind.TYPE, it.getString(1), folder.catalog, folder.schema, detail = it.getString(2)) }

    fun tableFolders(table: DbObject): List<DbObject> {
        val list = mutableListOf(
            table.copy(kind = DbObjectKind.COLUMNS_FOLDER, name = "Columns"),
            table.copy(kind = DbObjectKind.KEYS_FOLDER, name = "Primary Key"),
        )
        if (table.kind == DbObjectKind.TABLE) {
            list += table.copy(kind = DbObjectKind.FOREIGN_KEYS_FOLDER, name = "Foreign Keys")
            list += table.copy(kind = DbObjectKind.INDEXES_FOLDER, name = "Indexes")
            if (kind.isPostgresFamily) {
                list += table.copy(kind = DbObjectKind.TRIGGERS_FOLDER, name = "Triggers")
                list += table.copy(kind = DbObjectKind.CONSTRAINTS_FOLDER, name = "Constraints")
            }
        }
        return list
    }

    fun columns(t: DbObject): List<DbObject> = rows(md.getColumns(t.catalog, t.schema, t.table, "%")) {
        val type = it.getString("TYPE_NAME")
        val size = it.getIntOrNull("COLUMN_SIZE")
        val nullable = it.getIntOrNull("NULLABLE") == DatabaseMetaData.columnNullable
        val default = it.getStringOrNull("COLUMN_DEF")
        val typeText = if (size != null && size in 1..65535 && type.lowercase().let { n -> "char" in n || "numeric" in n || "decimal" in n }) "$type($size)" else type
        DbObject(DbObjectKind.COLUMN, it.getString("COLUMN_NAME"), t.catalog, t.schema, t.table,
            detail = typeText + (if (nullable) "" else " NOT NULL") + (default?.let { d -> " = $d" } ?: ""))
    }

    fun primaryKey(t: DbObject): List<DbObject> = runCatching {
        rows(md.getPrimaryKeys(t.catalog, t.schema, t.table)) { it.getString("PK_NAME") to (it.getInt("KEY_SEQ") to it.getString("COLUMN_NAME")) }
    }.getOrDefault(emptyList()).groupBy({ it.first ?: "pk" }, { it.second }).map { (name, cols) ->
        DbObject(DbObjectKind.PRIMARY_KEY, name, t.catalog, t.schema, t.table, detail = cols.sortedBy { it.first }.joinToString(", ") { it.second })
    }

    fun primaryKeyColumns(t: DbObject): List<String> = runCatching {
        rows(md.getPrimaryKeys(t.catalog, t.schema, t.table)) { it.getInt("KEY_SEQ") to it.getString("COLUMN_NAME") }
    }.getOrDefault(emptyList()).sortedBy { it.first }.map { it.second }

    /** Колонки для идентификации строки при редактировании: PK; для ClickHouse — колонки первичного ключа MergeTree. */
    fun keyColumns(t: DbObject): List<String> {
        if (kind == DbKind.CLICKHOUSE) {
            return runCatching {
                query("SELECT name FROM system.columns WHERE database = ? AND table = ? AND is_in_primary_key = 1 ORDER BY position",
                    t.schema ?: t.catalog, t.table) { it.getString(1) }
            }.getOrDefault(emptyList())
        }
        return primaryKeyColumns(t)
    }

    fun qualified(o: DbObject): String = o.qualifiedName(::quote)

    fun foreignKeys(t: DbObject): List<DbObject> = runCatching {
        rows(md.getImportedKeys(t.catalog, t.schema, t.table)) {
            Triple(it.getStringOrNull("FK_NAME") ?: "fk", it.getString("FKCOLUMN_NAME"), "${it.getString("PKTABLE_NAME")}(${it.getString("PKCOLUMN_NAME")})")
        }
    }.getOrDefault(emptyList()).groupBy { it.first }.map { (name, cols) ->
        DbObject(DbObjectKind.FOREIGN_KEY, name, t.catalog, t.schema, t.table,
            detail = cols.joinToString(", ") { it.second } + " → " + cols.joinToString(", ") { it.third })
    }

    fun indexes(t: DbObject): List<DbObject> = runCatching {
        rows(md.getIndexInfo(t.catalog, t.schema, t.table, false, true)) {
            Triple(it.getStringOrNull("INDEX_NAME"), it.getStringOrNull("COLUMN_NAME"), !it.getBoolean("NON_UNIQUE"))
        }
    }.getOrDefault(emptyList()).filter { it.first != null }.groupBy { it.first!! }.map { (name, cols) ->
        DbObject(DbObjectKind.INDEX, name, t.catalog, t.schema, t.table,
            detail = (if (cols.first().third) "UNIQUE " else "") + cols.mapNotNull { it.second }.joinToString(", "))
    }

    fun triggers(t: DbObject): List<DbObject> = query(
        "SELECT trigger_name, string_agg(event_manipulation, ' OR '), action_timing FROM information_schema.triggers " +
            "WHERE event_object_schema = ? AND event_object_table = ? GROUP BY trigger_name, action_timing ORDER BY 1",
        t.schema, t.table,
    ) { DbObject(DbObjectKind.TRIGGER, it.getString(1), t.catalog, t.schema, t.table, detail = "${it.getString(3)} ${it.getString(2)}") }

    fun constraints(t: DbObject): List<DbObject> = query(
        """
        SELECT con.conname, CASE con.contype WHEN 'c' THEN 'CHECK' WHEN 'u' THEN 'UNIQUE' WHEN 'p' THEN 'PRIMARY KEY'
               WHEN 'f' THEN 'FOREIGN KEY' WHEN 'x' THEN 'EXCLUDE' ELSE con.contype::text END || ' ' || pg_get_constraintdef(con.oid)
        FROM pg_constraint con JOIN pg_class rel ON rel.oid = con.conrelid JOIN pg_namespace n ON n.oid = rel.relnamespace
        WHERE n.nspname = ? AND rel.relname = ? ORDER BY 1
        """.trimIndent(), t.schema, t.table,
    ) { DbObject(DbObjectKind.CONSTRAINT, it.getString(1), t.catalog, t.schema, t.table, detail = it.getString(2)) }

    fun children(o: DbObject): List<DbObject> = when (o.kind) {
        DbObjectKind.SCHEMA, DbObjectKind.CATALOG -> folders(scope(o))
        DbObjectKind.TABLES_FOLDER -> tables(o, views = false)
        DbObjectKind.VIEWS_FOLDER -> tables(o, views = true)
        DbObjectKind.FUNCTIONS_FOLDER -> functions(o)
        DbObjectKind.SEQUENCES_FOLDER -> sequences(o)
        DbObjectKind.TYPES_FOLDER -> types(o)
        DbObjectKind.TABLE, DbObjectKind.VIEW -> tableFolders(o)
        DbObjectKind.COLUMNS_FOLDER -> columns(o)
        DbObjectKind.KEYS_FOLDER -> primaryKey(o)
        DbObjectKind.FOREIGN_KEYS_FOLDER -> foreignKeys(o)
        DbObjectKind.INDEXES_FOLDER -> indexes(o)
        DbObjectKind.TRIGGERS_FOLDER -> triggers(o)
        DbObjectKind.CONSTRAINTS_FOLDER -> constraints(o)
        else -> emptyList()
    }

    private fun <T> query(sql: String, vararg params: String?, map: (ResultSet) -> T): List<T> =
        connection.prepareStatement(sql).use { ps ->
            params.forEachIndexed { i, p -> ps.setString(i + 1, p) }
            rows(ps.executeQuery(), map)
        }

    private fun <T> rows(rs: ResultSet, map: (ResultSet) -> T): List<T> = rs.use {
        val out = ArrayList<T>()
        while (it.next()) out += map(it)
        out
    }
}

private fun ResultSet.getStringOrNull(column: String): String? = runCatching { getString(column) }.getOrNull()
private fun ResultSet.getIntOrNull(column: String): Int? = runCatching { getInt(column).takeIf { !wasNull() } }.getOrNull()
