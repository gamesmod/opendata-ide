package io.opendata.db.model

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.EventDispatcher
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import java.util.EventListener
import java.util.UUID

/** Описание источника данных. Пароль хранится отдельно, в PasswordSafe. */
@Tag("data-source")
class DataSourceConfig {
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var kind: DbKind = DbKind.POSTGRESQL
    var host: String = "localhost"
    var port: Int = DbKind.POSTGRESQL.defaultPort
    var database: String = DbKind.POSTGRESQL.defaultDatabase
    var user: String = ""
    /** Если задан — используется вместо URL, собранного из host/port/database. */
    var url: String = ""
    /** Путь к собственному JAR драйвера (вместо загрузки из Maven Central). */
    var driverJar: String = ""
    var autoCommit: Boolean = true

    @get:MapAnnotation(surroundWithTag = false, entryTagName = "property", keyAttributeName = "name", valueAttributeName = "value")
    var properties: MutableMap<String, String> = LinkedHashMap()

    fun effectiveUrl(): String = url.ifBlank { io.opendata.db.drivers.DriverSettings.getInstance().buildUrl(kind, host, port, database) }

    fun copy(newId: Boolean = false): DataSourceConfig = DataSourceConfig().also {
        it.id = if (newId) UUID.randomUUID().toString() else id
        it.name = name; it.kind = kind; it.host = host; it.port = port; it.database = database
        it.user = user; it.url = url; it.driverJar = driverJar; it.autoCommit = autoCommit
        it.properties = LinkedHashMap(properties)
    }

    override fun toString(): String = name
}

/**
 * Источники данных проекта (как в DataGrip): хранятся в `.idea/opendata-datasources.xml` вместе с проектом,
 * пароли — отдельно, в PasswordSafe. У каждого проекта свой набор подключений.
 */
@Service(Service.Level.PROJECT)
@State(name = "OpenDataDataSources", storages = [Storage("opendata-datasources.xml")])
class DataSourceStorage(private val project: Project) : PersistentStateComponent<DataSourceStorage.State> {

    class State {
        @get:XCollection(style = XCollection.Style.v2)
        var dataSources: MutableList<DataSourceConfig> = ArrayList()
    }

    fun interface Listener : EventListener {
        fun dataSourcesChanged()
    }

    private var state = State()
    private val dispatcher = EventDispatcher.create(Listener::class.java)

    override fun getState(): State = state
    override fun loadState(state: State) {
        this.state = state
        dispatcher.multicaster.dataSourcesChanged()
    }

    val dataSources: List<DataSourceConfig> get() = state.dataSources.toList()

    fun find(id: String): DataSourceConfig? = state.dataSources.firstOrNull { it.id == id }

    fun addOrUpdate(config: DataSourceConfig) {
        val i = state.dataSources.indexOfFirst { it.id == config.id }
        if (i >= 0) state.dataSources[i] = config else state.dataSources.add(config)
        changed()
    }

    /** Подключения сразу записываются в .idea проекта, не дожидаясь автосохранения. */
    private fun changed() {
        dispatcher.multicaster.dataSourcesChanged()
        if (!project.isDefault && !project.isDisposed) project.scheduleSave()
    }

    fun remove(id: String) {
        state.dataSources.removeIf { it.id == id }
        DataSources.setPassword(id, null)
        changed()
    }

    fun addListener(listener: Listener, parent: com.intellij.openapi.Disposable) = dispatcher.addListener(listener, parent)

    companion object {
        fun getInstance(project: Project): DataSourceStorage = project.service()
    }
}

/**
 * Подключения версии 0.2.0, хранившиеся на уровне приложения (`config/options/opendata-datasources.xml`).
 * При открытии проекта переносятся в него ([LegacyDataSourcesMigration]).
 */
@Service(Service.Level.APP)
@State(name = "OpenDataDataSources", storages = [Storage("opendata-datasources.xml")])
class LegacyDataSourceStorage : PersistentStateComponent<DataSourceStorage.State> {
    private var state = DataSourceStorage.State()
    override fun getState(): DataSourceStorage.State = state
    override fun loadState(state: DataSourceStorage.State) {
        this.state = state
    }

    /** Забирает все старые подключения (список очищается). */
    @Synchronized
    fun takeAll(): List<DataSourceConfig> = state.dataSources.toList().also { state.dataSources.clear() }

    companion object {
        fun getInstance(): LegacyDataSourceStorage = service()
    }
}

/** Поиск источника по id среди открытых проектов (id — UUID) и пароли в PasswordSafe. */
object DataSources {
    fun find(id: String): DataSourceConfig? =
        ProjectManager.getInstance().openProjects.firstNotNullOfOrNull { p -> if (p.isDisposed) null else DataSourceStorage.getInstance(p).find(id) }

    private fun attributes(id: String) = CredentialAttributes(generateServiceName("OpenData", id))

    /** Хранилище паролей ОС может быть недоступно (нет Secret Service/D-Bus) — тогда пароль не сохраняется. */
    fun getPassword(id: String): String? = runCatching { PasswordSafe.instance.getPassword(attributes(id)) }.getOrNull()

    fun setPassword(id: String, password: String?) {
        runCatching { PasswordSafe.instance.set(attributes(id), password?.let { Credentials(null, it) }) }
    }
}
