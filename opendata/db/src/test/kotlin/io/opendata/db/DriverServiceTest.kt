package io.opendata.db

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.drivers.DriverService
import io.opendata.db.model.DriverArtifact
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Драйверы JDBC: закреплённые суммы SHA-256 и загрузка из Maven Central (как при первом подключении в IDE). */
class DriverServiceTest : BasePlatformTestCase() {

    private fun sha256(p: Path) = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p)).joinToString("") { "%02x".format(it) }

    /** JAR в OPENDATA_DRIVERS_DIR получены Gradle из Maven Central: суммы в DbKind.kt должны с ними совпадать. */
    fun testPinnedChecksumsMatchMavenArtifacts() {
        val dir = DbTestSupport.env("OPENDATA_DRIVERS_DIR")?.let { Path.of(it) } ?: return
        for (jar in DriverArtifact.entries.flatMap { it.jars }) {
            val f = dir.resolve(jar.fileName)
            assertTrue("нет ${jar.fileName} в $dir", Files.isRegularFile(f))
            assertEquals(jar.fileName, jar.sha256, sha256(f))
        }
    }

    /** Настоящая загрузка всех драйверов, как в IDE (OPENDATA_TEST_DOWNLOAD=1, нужна сеть; включено в CI). */
    fun testDownloadAllDrivers() {
        if (DbTestSupport.env("OPENDATA_TEST_DOWNLOAD") == null) return
        val dir = Files.createTempDirectory("opendata-drivers")
        try {
            for (jar in DriverArtifact.entries.flatMap { it.jars }) {
                val f = DriverService.getInstance().downloadTo(jar, dir, null)
                assertEquals(jar.sha256, sha256(f))
            }
        } finally {
            dir.toFile().deleteRecursively()
        }
    }
}
