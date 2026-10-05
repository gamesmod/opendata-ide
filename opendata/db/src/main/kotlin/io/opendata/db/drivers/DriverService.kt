package io.opendata.db.drivers

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.io.HttpRequests
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DbKind
import io.opendata.db.model.MavenJar
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.sql.Connection
import java.sql.Driver
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

/**
 * Driver Manager OpenData: JDBC-драйвер каждой СУБД загружается в отдельный classloader.
 * JAR берётся из (по приоритету): driverJar источника → свои JAR из менеджера драйверов ([DriverSettings]) →
 * OPENDATA_DRIVERS_DIR → драйверы в комплекте IDE → config/opendata/drivers → загрузка из Maven Central.
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

    /** Где найден JAR. */
    enum class Origin(val title: String) { ENV("OPENDATA_DRIVERS_DIR"), BUNDLED("в комплекте"), DOWNLOADED("загружен") }

    data class JarStatus(val jar: MavenJar, val path: Path?, val origin: Origin?)

    /** Состояние драйвера СУБД для менеджера драйверов. */
    data class Status(val kind: DbKind, val customJars: List<Path>?, val jars: List<JarStatus>) {
        val isReady: Boolean get() = customJars?.all { Files.isRegularFile(it) } ?: jars.all { it.path != null }
        val summary: String
            get() = when {
                customJars != null -> if (isReady) "свои JAR" else "свои JAR: файл не найден"
                isReady -> jars.mapNotNull { it.origin?.title }.distinct().joinToString(", ")
                else -> "не загружен — будет загружен при подключении"
            }
    }

    fun status(kind: DbKind): Status =
        Status(kind, DriverSettings.getInstance().customJars(kind), DriverSettings.getInstance().artifacts(kind).map { locateWithOrigin(it) })

    fun isAvailable(kind: DbKind): Boolean = status(kind).isReady

    private fun locateWithOrigin(jar: MavenJar): JarStatus {
        val dirs = listOfNotNull(
            System.getenv("OPENDATA_DRIVERS_DIR")?.let { Path.of(it) to Origin.ENV },
            bundledDir?.let { it to Origin.BUNDLED },
            driversDir to Origin.DOWNLOADED,
        )
        for ((dir, origin) in dirs) {
            val p = dir.resolve(jar.fileName)
            if (Files.isRegularFile(p)) return JarStatus(jar, p, origin)
        }
        return JarStatus(jar, null, null)
    }

    /** Пути ко всем JAR драйвера, при необходимости скачивая их из Maven Central с проверкой контрольной суммы. */
    fun ensureDownloaded(kind: DbKind, indicator: ProgressIndicator?): List<Path> =
        DriverSettings.getInstance().artifacts(kind).map { jar -> locateWithOrigin(jar).path ?: downloadTo(jar, driversDir, indicator) }

    /** Удаляет загруженные (не встроенные) JAR драйвера СУБД. Возвращает число удалённых файлов. */
    fun deleteDownloaded(kind: DbKind): Int {
        drivers.clear()
        return DriverSettings.getInstance().artifacts(kind).count { Files.deleteIfExists(driversDir.resolve(it.fileName)) }
    }

    /**
     * Загружает [jar] в [dir] и сверяет контрольную сумму: закреплённую SHA-256, а для версии, выбранной
     * пользователем, — SHA-1 из Maven Central.
     */
    internal fun downloadTo(jar: MavenJar, dir: Path, indicator: ProgressIndicator?): Path {
        Files.createDirectories(dir)
        val target = dir.resolve(jar.fileName)
        val tmp = Files.createTempFile(dir, jar.artifact, ".part")
        try {
            indicator?.text = "Загрузка драйвера ${jar.fileName}"
            HttpRequests.request(jar.url()).productNameAsUserAgent().saveToFile(tmp.toFile(), indicator)
            val bytes = Files.readAllBytes(tmp)
            if (jar.sha256.isNotBlank()) {
                val actual = digest("SHA-256", bytes)
                if (actual != jar.sha256) throw SQLException("Контрольная сумма ${jar.fileName} не совпадает: ожидалось ${jar.sha256}, получено $actual")
            } else {
                val expected = HttpRequests.request(jar.url() + ".sha1").productNameAsUserAgent().readString().trim().take(40).lowercase()
                val actual = digest("SHA-1", bytes)
                if (expected != actual) throw SQLException("Контрольная сумма ${jar.fileName} не совпадает с Maven Central (SHA-1 $expected, получено $actual)")
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: java.io.IOException) {
            // Без сети (или за прокси) драйвер можно положить вручную — подсказываем, куда.
            throw SQLException("Не удалось загрузить драйвер ${jar.fileName} из ${jar.url()}: ${e.message}. " +
                "Скачайте JAR вручную и положите в $dir (или укажите свои JAR в менеджере драйверов).", e)
        } finally {
            Files.deleteIfExists(tmp)
        }
        return target
    }

    private fun digest(algorithm: String, bytes: ByteArray) = MessageDigest.getInstance(algorithm).digest(bytes).joinToString("") { "%02x".format(it) }

    /** Драйвер для СУБД: [explicitJars] (JAR источника данных) → свои JAR менеджера → Maven. */
    fun driver(kind: DbKind, explicitJars: List<Path>?, indicator: ProgressIndicator?): Driver {
        val settings = DriverSettings.getInstance()
        val jars = explicitJars ?: settings.customJars(kind) ?: ensureDownloaded(kind, indicator)
        jars.firstOrNull { !Files.isRegularFile(it) }?.let { throw SQLException("Файл драйвера не найден: $it") }
        val driverClass = settings.driverClass(kind)
        val key = jars.joinToString("|") { it.toAbsolutePath().toString() } + "|" + driverClass
        return drivers.computeIfAbsent(key) {
            // Изоляция от классов IDE: родитель — platform classloader JDK.
            val loader = URLClassLoader(jars.map { it.toUri().toURL() }.toTypedArray(), ClassLoader.getPlatformClassLoader())
            try {
                Class.forName(driverClass, true, loader).getDeclaredConstructor().newInstance() as Driver
            } catch (e: ClassNotFoundException) {
                throw SQLException("Класс драйвера $driverClass не найден в ${jars.joinToString { it.fileName.toString() }}", e)
            }
        }
    }

    fun driverFor(config: DataSourceConfig, indicator: ProgressIndicator?): Driver =
        driver(config.kind, config.driverJar.takeIf { it.isNotBlank() }?.split(java.io.File.pathSeparatorChar)?.map { Path.of(it.trim()) }, indicator)

    /** Проверка драйвера для менеджера: загрузка класса и версия. */
    fun probe(kind: DbKind, indicator: ProgressIndicator?): String {
        val d = driver(kind, null, indicator)
        return "${d.javaClass.name} ${d.majorVersion}.${d.minorVersion}"
    }

    fun connect(config: DataSourceConfig, password: String?, indicator: ProgressIndicator?): Connection {
        val driver = driverFor(config, indicator)
        val props = Properties()
        DriverSettings.getInstance().defaultProperties(config.kind).forEach { (k, v) -> props[k] = v }
        config.properties.forEach { (k, v) -> props[k] = v }
        if (config.user.isNotBlank()) props["user"] = config.user
        if (!password.isNullOrEmpty()) props["password"] = password
        val url = config.effectiveUrl()
        if (!io.opendata.db.model.JdbcUrls.matches(config.kind, url))
            throw SQLException("JDBC URL $url не подходит для типа подключения ${config.kind.displayName}: смените тип в свойствах подключения")
        indicator?.text = "Подключение к $url"
        // Драйвер вызывается напрямую: java.sql.DriverManager не видит классы из чужого classloader.
        val previous = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = driver.javaClass.classLoader
        try {
            return driver.connect(url, props) ?: throw SQLException("Драйвер ${driver.javaClass.name} не принял URL $url")
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
    }

    companion object {
        fun getInstance(): DriverService = service()
    }
}
