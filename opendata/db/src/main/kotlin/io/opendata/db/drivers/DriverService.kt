package io.opendata.db.drivers

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.io.HttpRequests
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DriverArtifact
import io.opendata.db.model.MavenJar
import java.security.MessageDigest
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.Driver
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

/**
 * Driver Manager OpenData: JDBC-драйвер каждой СУБД загружается в отдельный classloader.
 * JAR берётся из (по приоритету): driverJar источника → OPENDATA_DRIVERS_DIR → каталог драйверов IDE
 * (config/opendata/drivers) → загрузка из Maven Central.
 */
@Service(Service.Level.APP)
class DriverService {
    private val drivers = ConcurrentHashMap<String, Driver>()

    val driversDir: Path get() = Path.of(PathManager.getConfigPath(), "opendata", "drivers")

    /** Каталог драйверов, поставляемых с IDE (<plugin>/drivers). */
    private val bundledDir: Path? by lazy {
        com.intellij.ide.plugins.PluginManagerCore.getPlugin(com.intellij.openapi.extensions.PluginId.getId("io.opendata.db"))
            ?.pluginPath?.resolve("drivers")?.takeIf { Files.isDirectory(it) }
    }

    fun isAvailable(artifact: DriverArtifact): Boolean = artifact.jars.all { locate(it) != null }

    private fun locate(jar: MavenJar): Path? {
        val dirs = listOfNotNull(System.getenv("OPENDATA_DRIVERS_DIR")?.let { Path.of(it) }, bundledDir, driversDir)
        return dirs.map { it.resolve(jar.fileName) }.firstOrNull { Files.isRegularFile(it) }
    }

    /** Пути ко всем JAR драйвера, при необходимости скачивая их из Maven Central с проверкой SHA-256. */
    fun ensureDownloaded(artifact: DriverArtifact, indicator: ProgressIndicator?): List<Path> = artifact.jars.map { jar ->
        locate(jar) ?: download(jar, indicator)
    }

    private fun download(jar: MavenJar, indicator: ProgressIndicator?): Path {
        Files.createDirectories(driversDir)
        val target = driversDir.resolve(jar.fileName)
        val tmp = Files.createTempFile(driversDir, jar.artifact, ".part")
        try {
            indicator?.text = "Загрузка драйвера ${jar.fileName}"
            HttpRequests.request(jar.url()).productNameAsUserAgent().saveToFile(tmp.toFile(), indicator)
            val expected = HttpRequests.request(jar.url() + ".sha256").productNameAsUserAgent().readString().trim().take(64).lowercase()
            val actual = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(tmp)).joinToString("") { "%02x".format(it) }
            if (expected.length == 64 && expected != actual) throw SQLException("Контрольная сумма ${jar.fileName} не совпадает (ожидалось $expected)")
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: java.io.IOException) {
            // Без сети (или за прокси) драйвер можно положить вручную — подсказываем, куда.
            throw SQLException("Не удалось загрузить драйвер ${jar.fileName} из ${jar.url()}: ${e.message}. " +
                "Скачайте JAR вручную и положите в $driversDir (или укажите его в поле «JAR драйвера» источника данных).", e)
        } finally {
            Files.deleteIfExists(tmp)
        }
        return target
    }

    fun driverFor(config: DataSourceConfig, indicator: ProgressIndicator?): Driver {
        val jars = config.driverJar.takeIf { it.isNotBlank() }?.split(java.io.File.pathSeparatorChar)?.map { Path.of(it.trim()) }
            ?: ensureDownloaded(config.kind.driver, indicator)
        val key = jars.joinToString("|") { it.toAbsolutePath().toString() } + "|" + config.kind.driverClass
        return drivers.computeIfAbsent(key) {
            // Изоляция от классов IDE: родитель — platform classloader JDK.
            val loader = URLClassLoader(jars.map { it.toUri().toURL() }.toTypedArray(), ClassLoader.getPlatformClassLoader())
            Class.forName(config.kind.driverClass, true, loader).getDeclaredConstructor().newInstance() as Driver
        }
    }

    fun connect(config: DataSourceConfig, password: String?, indicator: ProgressIndicator?): Connection {
        val driver = driverFor(config, indicator)
        val props = Properties()
        config.kind.defaultProperties.forEach { (k, v) -> props[k] = v }
        config.properties.forEach { (k, v) -> props[k] = v }
        if (config.user.isNotBlank()) props["user"] = config.user
        if (!password.isNullOrEmpty()) props["password"] = password
        val url = config.effectiveUrl()
        indicator?.text = "Подключение к $url"
        // Драйвер вызывается напрямую: java.sql.DriverManager не видит классы из чужого classloader.
        val previous = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = driver.javaClass.classLoader
        try {
            return driver.connect(url, props) ?: throw SQLException("Драйвер ${config.kind.driverClass} не принял URL $url")
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
    }

    companion object {
        fun getInstance(): DriverService = service()
    }
}
