package proto.media.fiesta.features.domain.car.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSearchRequestTest {

    @Test
    fun requestRejectsCallbacksFromAnotherDocument() {
        val request = VoiceSearchRequest.create(7, "music", 1_000).withDocument(3)

        assertTrue(request.belongsTo(7, 3))
        assertFalse(request.belongsTo(7, 2))
        assertFalse(request.belongsTo(8, 3))
    }

    @Test
    fun requestKeepsItsAbsoluteDeadlineAcrossDocuments() {
        val request = VoiceSearchRequest.create(1, "music", 1_000)
        val nextDocument = request.withDocument(4)

        assertEquals(31_000, nextDocument.deadlineAtMillis)
        assertTrue(nextDocument.hasExpired(31_000))
        assertFalse(nextDocument.hasExpired(30_999))
    }

    @Test
    fun requestAdvancesWithoutChangingItsIdentity() {
        val request = VoiceSearchRequest.create(4, "music", 1_000).withDocument(2)
        val waitingForPlayback = request.advance(VoiceSearchRequest.Stage.WAITING_FOR_PLAYBACK)

        assertEquals(4, waitingForPlayback.id)
        assertEquals(2, waitingForPlayback.documentGeneration)
        assertEquals(VoiceSearchRequest.Stage.WAITING_FOR_PLAYBACK, waitingForPlayback.stage)
    }
}
