package io.opendata.db.history

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.EventDispatcher
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import java.util.EventListener

/** Запись истории запросов (ТЗ 27). */
@Tag("query")
class HistoryEntry {
    var timestamp: Long = 0
    var dataSource: String = ""
    var database: String = ""
    var sql: String = ""
    var durationMs: Long = 0
    var rows: Long = -1
    var status: String = ""
}

@Service(Service.Level.APP)
@State(name = "OpenDataQueryHistory", storages = [Storage("opendata-history.xml")])
class QueryHistory : PersistentStateComponent<QueryHistory.State> {
    class State {
        @get:XCollection(style = XCollection.Style.v2)
        var entries: MutableList<HistoryEntry> = ArrayList()
    }

    fun interface Listener : EventListener {
        fun added(entry: HistoryEntry)
    }

    private var state = State()
    private val dispatcher = EventDispatcher.create(Listener::class.java)

    override fun getState() = state
    override fun loadState(state: State) {
        this.state = state
    }

    val entries: List<HistoryEntry> @Synchronized get() = state.entries.toList()

    fun add(entry: HistoryEntry) {
        synchronized(this) {
            state.entries.add(0, entry)
            while (state.entries.size > MAX) state.entries.removeAt(state.entries.size - 1)
        }
        dispatcher.multicaster.added(entry)
    }

    fun addListener(l: Listener, parent: com.intellij.openapi.Disposable) = dispatcher.addListener(l, parent)

    companion object {
        const val MAX = 1000
        fun getInstance(): QueryHistory = service()
    }
}
