package io.opendata.db.drivers

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import io.opendata.db.model.DbKind
import io.opendata.db.model.MavenJar
import java.io.File
import java.nio.file.Path

/**
 * Настройки драйвера одной СУБД (ТЗ 13). Пустое поле — значение по умолчанию из [DbKind].
 * Хранятся на уровне IDE: драйвер общий для всех проектов, подключения — в проекте.
 */
@Tag("driver")
class DriverOverride {
    var kind: DbKind = DbKind.POSTGRESQL
    /** Версия основного артефакта Maven (пусто — встроенная, с закреплённой SHA-256). */
    var version: String = ""
    /** Свои JAR через [File.pathSeparator] (пусто — артефакты Maven). */
    var jars: String = ""
    var driverClass: String = ""
    /** Шаблон JDBC URL с {host}, {port}, {database}. */
    var urlTemplate: String = ""

    @get:MapAnnotation(surroundWithTag = false, entryTagName = "property", keyAttributeName = "name", valueAttributeName = "value")
    var properties: MutableMap<String, String> = LinkedHashMap()

    val isEmpty: Boolean get() = version.isBlank() && jars.isBlank() && driverClass.isBlank() && urlTemplate.isBlank() && properties.isEmpty()

    fun copy(): DriverOverride = DriverOverride().also {
        it.kind = kind; it.version = version; it.jars = jars; it.driverClass = driverClass; it.urlTemplate = urlTemplate
        it.properties = LinkedHashMap(properties)
    }

    override fun equals(other: Any?): Boolean = other is DriverOverride && other.kind == kind && other.version == version &&
        other.jars == jars && other.driverClass == driverClass && other.urlTemplate == urlTemplate && other.properties == properties

    override fun hashCode(): Int = kind.hashCode()
}

@Service(Service.Level.APP)
@State(name = "OpenDataDrivers", storages = [Storage("opendata-drivers.xml")])
class DriverSettings : PersistentStateComponent<DriverSettings.State> {
    class State {
        @get:XCollection(style = XCollection.Style.v2)
        var drivers: MutableList<DriverOverride> = ArrayList()
    }

    private var state = State()
    override fun getState(): State = state
    override fun loadState(state: State) {
        this.state = state
    }

    /** Копия настроек СУБД (для редактирования). */
    fun get(kind: DbKind): DriverOverride = state.drivers.firstOrNull { it.kind == kind }?.copy() ?: DriverOverride().also { it.kind = kind }

    fun set(o: DriverOverride) {
        state.drivers.removeIf { it.kind == o.kind }
        if (!o.isEmpty) state.drivers.add(o.copy())
    }

    private fun o(kind: DbKind): DriverOverride? = state.drivers.firstOrNull { it.kind == kind }

    fun driverClass(kind: DbKind): String = o(kind)?.driverClass?.takeIf { it.isNotBlank() } ?: kind.driverClass

    fun urlTemplate(kind: DbKind): String = o(kind)?.urlTemplate?.takeIf { it.isNotBlank() } ?: kind.urlTemplate

    fun buildUrl(kind: DbKind, host: String, port: Int, database: String): String =
        urlTemplate(kind).replace("{host}", host).replace("{port}", port.toString()).replace("{database}", database)

    /** Свойства по умолчанию: встроенные, затем заданные в менеджере драйверов. */
    fun defaultProperties(kind: DbKind): Map<String, String> = kind.defaultProperties + o(kind)?.properties.orEmpty()

    fun customJars(kind: DbKind): List<Path>? =
        o(kind)?.jars?.takeIf { it.isNotBlank() }?.split(File.pathSeparatorChar, ';')?.map { it.trim() }?.filter { it.isNotEmpty() }?.map { Path.of(it) }

    /** Артефакты Maven с учётом выбранной версии: у другой версии нет закреплённой суммы — проверяется .sha1 из Maven Central. */
    fun artifacts(kind: DbKind): List<MavenJar> {
        val jars = kind.driver.jars
        val v = o(kind)?.version?.trim().orEmpty()
        if (v.isEmpty() || v == jars.first().version) return jars
        return listOf(jars.first().copy(version = v, sha256 = "")) + jars.drop(1)
    }

    companion object {
        fun getInstance(): DriverSettings = service()
    }
}
