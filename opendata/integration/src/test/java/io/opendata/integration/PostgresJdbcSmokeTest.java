package io.opendata.integration;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.sql.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Интеграционные тесты тестовой PostgreSQL (ТЗ 36): соединение, SELECT, UPDATE, commit/rollback,
 * cancel, SQL error с позицией.
 *
 * Проверяют тестовый стенд (docker/postgres) и поведение, на которое опираются сценарии этапов 2–4.
 * Сами Database Tools здесь не участвуют: UI-сценарии проверяются по docs/acceptance.md.
 *
 * Пропускаются, если не задана переменная OPENDATA_PG_URL (например, jdbc:postgresql://localhost:54329/opendata).
 */
public class PostgresJdbcSmokeTest {
    private static String url, user, password;

    @BeforeClass
    public static void setUp() {
        url = System.getenv("OPENDATA_PG_URL");
        user = env("OPENDATA_PG_USER", "opendata");
        password = env("OPENDATA_PG_PASSWORD", "opendata");
        Assume.assumeTrue("OPENDATA_PG_URL not set — PostgreSQL integration tests skipped", url != null && !url.isBlank());
    }

    private static String env(String k, String def) {
        String v = System.getenv(k);
        return v == null || v.isBlank() ? def : v;
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Test
    public void connectionAndVersion() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT version()")) {
            assertTrue(rs.next());
            assertTrue(rs.getString(1), rs.getString(1).startsWith("PostgreSQL"));
        }
    }

    @Test
    public void generateSeriesReturns100Rows() throws SQLException {
        String sql = "SELECT generate_series AS id, md5(generate_series::text) AS value FROM generate_series(1,100)";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            assertEquals("id", rs.getMetaData().getColumnLabel(1));
            assertEquals("value", rs.getMetaData().getColumnLabel(2));
            int n = 0;
            while (rs.next()) n++;
            assertEquals(100, n);
        }
    }

    @Test
    public void metadataContainsTestTable() throws SQLException {
        try (Connection c = connect(); ResultSet rs = c.getMetaData().getColumns(null, "public", "opendata_test", null)) {
            int n = 0;
            while (rs.next()) n++;
            assertEquals("opendata_test must have id, name, created_at (see docker/postgres/init)", 3, n);
        }
    }

    @Test
    public void updateCommitAndRollback() throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            long id;
            try (PreparedStatement ins = c.prepareStatement("INSERT INTO opendata_test(name) VALUES ('before') RETURNING id")) {
                ResultSet rs = ins.executeQuery();
                rs.next();
                id = rs.getLong(1);
            }
            c.commit();

            update(c, id, "rolled-back");
            c.rollback();
            assertEquals("before", name(c, id));

            update(c, id, "committed");
            c.commit();
            try (Connection other = connect()) {
                assertEquals("committed", name(other, id));
            }
            try (PreparedStatement del = c.prepareStatement("DELETE FROM opendata_test WHERE id = ?")) {
                del.setLong(1, id);
                del.executeUpdate();
            }
            c.commit();
        }
    }

    @Test
    public void longQueryCanBeCancelled() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        try (Connection c = connect(); Statement s = c.createStatement()) {
            Future<?> f = ex.submit(() -> { s.execute("SELECT pg_sleep(30)"); return null; });
            Thread.sleep(500);
            long t0 = System.nanoTime();
            s.cancel();
            try {
                f.get(10, TimeUnit.SECONDS);
                fail("query was not cancelled");
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof SQLException);
                assertEquals("57014", ((SQLException) e.getCause()).getSQLState()); // query_canceled
            }
            assertTrue("cancel took too long", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - t0) < 10);
            // соединение остаётся работоспособным после отмены (ТЗ 25)
            try (ResultSet rs = s.executeQuery("SELECT 1")) {
                assertTrue(rs.next());
            }
        } finally {
            ex.shutdownNow();
        }
    }

    @Test
    public void sqlErrorHasStateAndPosition() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeQuery("SELECT * FROM no_such_table_xyz");
            fail("error expected");
        } catch (PSQLException e) {
            ServerErrorMessage m = e.getServerErrorMessage();
            assertNotNull(m);
            assertEquals("42P01", e.getSQLState());
            assertEquals("ERROR", m.getSeverity());
            assertTrue("position expected", m.getPosition() > 0);
        }
    }

    private static void update(Connection c, long id, String name) throws SQLException {
        try (PreparedStatement u = c.prepareStatement("UPDATE opendata_test SET name = ? WHERE id = ?")) {
            u.setString(1, name);
            u.setLong(2, id);
            assertEquals(1, u.executeUpdate());
        }
    }

    private static String name(Connection c, long id) throws SQLException {
        try (PreparedStatement q = c.prepareStatement("SELECT name FROM opendata_test WHERE id = ?")) {
            q.setLong(1, id);
            ResultSet rs = q.executeQuery();
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }
}
