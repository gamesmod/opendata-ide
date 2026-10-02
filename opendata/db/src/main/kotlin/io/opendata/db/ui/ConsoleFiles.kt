package io.opendata.db.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.opendata.db.model.DataSourceConfig
import java.nio.file.Files
import java.nio.file.Path

/**
 * SQL-консоли — обычные .sql-файлы в config/opendata/consoles/<id источника>/.
 * Любой другой .sql-файл можно привязать к источнику данных (привязка хранится в PropertiesComponent).
 */
object ConsoleFiles {
    private const val KEY_PREFIX = "opendata.console.ds:"

    val consolesDir: Path get() = Path.of(PathManager.getConfigPath(), "opendata", "consoles")

    fun dataSourceIdOf(file: VirtualFile): String? {
        PropertiesComponent.getInstance().getValue(KEY_PREFIX + file.url)?.let { return it }
        val path = runCatching { file.toNioPath() }.getOrNull() ?: return null
        val root = consolesDir
        return if (path.startsWith(root) && path.nameCount > root.nameCount + 1) path.getName(root.nameCount).toString() else null
    }

    fun bind(file: VirtualFile, dataSourceId: String) {
        PropertiesComponent.getInstance().setValue(KEY_PREFIX + file.url, dataSourceId)
    }

    /** Файл консоли источника данных; создаётся при первом обращении. */
    fun consoleFile(ds: DataSourceConfig, index: Int = 0): VirtualFile {
        val dir = consolesDir.resolve(ds.id)
        Files.createDirectories(dir)
        val file = dir.resolve(if (index == 0) "console.sql" else "console_$index.sql")
        if (!Files.exists(file)) Files.writeString(file, "-- ${ds.name} (${ds.kind.displayName})\n\n")
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file) ?: error("Не удалось открыть $file")
        bind(vf, ds.id)
        return vf
    }

    fun nextConsoleFile(ds: DataSourceConfig): VirtualFile {
        val dir = consolesDir.resolve(ds.id)
        var i = 1
        while (Files.exists(dir.resolve("console_$i.sql"))) i++
        return consoleFile(ds, i)
    }
}
