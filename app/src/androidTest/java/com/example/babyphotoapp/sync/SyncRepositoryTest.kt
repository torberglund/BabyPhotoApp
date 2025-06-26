// File: app/src/androidTest/java/com/example/babyphotoapp/sync/SyncRepositoryTest.kt
package com.example.babyphotoapp.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SyncRepositoryTest {
    private lateinit var repo: SyncRepository
    private lateinit var ctx: Context

    @Before
    fun setup() {
        // get the instrumentation‐provided app context
        ctx = ApplicationProvider.getApplicationContext()

        // nuke any prior index.json so tests start from empty
        File(ctx.filesDir, "index.json")
            .takeIf { it.exists() }
            ?.delete()

        // now create your repository
        repo = SyncRepository(ctx)
    }

    @Test
    fun firstShotBecomesActive() = runBlocking {
        val dateKey = repo.todayKey()
        val filename = "20250101_090000.jpg"

        val updated = repo.markActive(dateKey, filename, deviceId = "test-device")

        assertTrue(updated.days.containsKey(dateKey))
        val day = updated.days[dateKey]!!
        assertEquals(filename, day.active)
        assertEquals(1, day.shots.size)
        assertEquals(ShotStatus.ACTIVE, day.shots.first().status)
    }

    @Test
    fun reMarkingSwitchesActive() = runBlocking {
        val dateKey = repo.todayKey()
        val first  = "a.jpg"
        val second = "b.jpg"

        repo.markActive(dateKey, first,  "D1")
        val after = repo.markActive(dateKey, second, "D2")

        val shots = after.days[dateKey]!!.shots.associateBy { it.file }
        assertEquals(ShotStatus.PASSIVE, shots[first]!!.status)
        assertEquals(ShotStatus.ACTIVE,  shots[second]!!.status)
    }
}
