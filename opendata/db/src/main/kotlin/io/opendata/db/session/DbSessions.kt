package io.opendata.db.session

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import java.util.concurrent.ConcurrentHashMap

/**
 * Реестр сессий: у каждой консоли и у каждого редактора таблицы своё соединение;
 * у Database Explorer — отдельная сессия на источник данных.
 */
@Service(Service.Level.APP)
class DbSessions : Disposable {
    private val sessions = ConcurrentHashMap<String, DbSession>()

    fun get(key: String, dataSourceId: String): DbSession {
        val s = sessions.computeIfAbsent(key) { DbSession(dataSourceId).also { Disposer.register(this, it) } }
        s.switchDataSource(dataSourceId)
        return s
    }

    fun explorer(dataSourceId: String): DbSession = get("explorer:$dataSourceId", dataSourceId)

    fun find(key: String): DbSession? = sessions[key]

    fun all(): Collection<DbSession> = sessions.values

    fun close(key: String) {
        sessions.remove(key)?.let { Disposer.dispose(it) }
    }

    /** Disconnect: закрывает все соединения источника данных (консоли, редакторы, explorer). */
    fun disconnect(dataSourceId: String) {
        sessions.values.filter { it.dataSourceId == dataSourceId }.forEach { it.close() }
    }

    fun isConnected(dataSourceId: String): Boolean = sessions.values.any { it.dataSourceId == dataSourceId && it.isConnected }

    override fun dispose() {
        sessions.clear()
    }

    companion object {
        fun getInstance(): DbSessions = service()
    }
}
