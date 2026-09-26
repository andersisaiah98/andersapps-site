package app.jscookbook.core.data.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LastWriteWinsTest {

    @Test
    fun newRowIsAlwaysTaken() {
        assertTrue(shouldApplyRemote(localUpdatedAt = null, localDirty = false, remoteUpdatedAt = 1))
    }

    @Test
    fun unpushedLocalEditBeatsOlderOrEqualRemote() {
        assertFalse(shouldApplyRemote(localUpdatedAt = 200, localDirty = true, remoteUpdatedAt = 100))
        assertFalse(shouldApplyRemote(localUpdatedAt = 200, localDirty = true, remoteUpdatedAt = 200))
    }

    @Test
    fun newerRemoteEditBeatsUnpushedLocalEdit() {
        assertTrue(shouldApplyRemote(localUpdatedAt = 200, localDirty = true, remoteUpdatedAt = 201))
    }

    @Test
    fun syncedLocalRowTakesNewerOrSameRemote() {
        assertTrue(shouldApplyRemote(localUpdatedAt = 200, localDirty = false, remoteUpdatedAt = 300))
        assertTrue(shouldApplyRemote(localUpdatedAt = 200, localDirty = false, remoteUpdatedAt = 200))
    }

    @Test
    fun syncedLocalRowIgnoresStaleEcho() {
        assertFalse(shouldApplyRemote(localUpdatedAt = 300, localDirty = false, remoteUpdatedAt = 200))
    }
}
