package io.opendata.db.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.opendata.db.model.DataSourceConfig
import java.nio.file.Files
import java.nio.file.Path

/**
 * SQL-консоли — обычные .sql-файлы проекта: `<проект>/.idea/opendata/consoles/<id источника>/`
 * (консоли 0.2.0 в config/opendata/consoles тоже распознаются).
 * Любой другой .sql-файл можно привязать к источнику данных (привязка хранится в PropertiesComponent).
 */
object ConsoleFiles {
    private const val KEY_PREFIX = "opendata.console.ds:"

    /** Каталог консолей проекта (у проекта без папки — каталог настроек IDE). */
    fun consolesDir(project: Project): Path =
        (project.basePath?.let { Path.of(it, Project.DIRECTORY_STORE_FOLDER) } ?: Path.of(PathManager.getConfigPath()))
            .resolve("opendata").resolve("consoles")

    fun dataSourceIdOf(file: VirtualFile): String? {
        PropertiesComponent.getInstance().getValue(KEY_PREFIX + file.url)?.let { return it }
        val path = runCatching { file.toNioPath() }.getOrNull() ?: return null
        // .../opendata/consoles/<id>/<file>.sql
        val n = path.nameCount
        return if (n >= 4 && path.getName(n - 4).toString() == "opendata" && path.getName(n - 3).toString() == "consoles") path.getName(n - 2).toString() else null
    }

    fun bind(file: VirtualFile, dataSourceId: String) {
        PropertiesComponent.getInstance().setValue(KEY_PREFIX + file.url, dataSourceId)
    }

    /** Файл консоли источника данных; создаётся при первом обращении. */
    fun consoleFile(project: Project, ds: DataSourceConfig, index: Int = 0): VirtualFile {
        val dir = consolesDir(project).resolve(ds.id)
        Files.createDirectories(dir)
        val file = dir.resolve(if (index == 0) "console.sql" else "console_$index.sql")
        if (!Files.exists(file)) Files.writeString(file, "-- ${ds.name} (${ds.kind.displayName})\n\n")
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file) ?: error("Не удалось открыть $file")
        bind(vf, ds.id)
        return vf
    }

    fun nextConsoleFile(project: Project, ds: DataSourceConfig): VirtualFile {
        val dir = consolesDir(project).resolve(ds.id)
        var i = 1
        while (Files.exists(dir.resolve("console_$i.sql"))) i++
        return consoleFile(project, ds, i)
    }
}
