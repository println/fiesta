package proto.media.fiesta.features.domain.core.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackPublicationTest {

    private fun playing(positionMillis: Long, updatedAtMillis: Long) = PublishedPlayback(
        playbackState = 3,
        actions = 8063L,
        activeQueueId = 2L,
        errorMessage = "",
        positionMillis = positionMillis,
        speed = 1f,
        updatedAtMillis = updatedAtMillis
    )

    @Test
    fun theFirstStateIsAlwaysPublished() {
        assertTrue(PlaybackPublication.worthPublishing(null, playing(0, 1_000)))
    }

    @Test
    fun aPositionTheCarCanExtrapolateOnItsOwnIsNotPublishedAgain() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        val fiveSecondsLater = playing(positionMillis = 15_000, updatedAtMillis = 6_000)
        assertFalse(PlaybackPublication.worthPublishing(published, fiveSecondsLater))
    }

    @Test
    fun aSeekIsPublishedBecauseThePositionLeftTheExtrapolation() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        val seeked = playing(positionMillis = 120_000, updatedAtMillis = 6_000)
        assertTrue(PlaybackPublication.worthPublishing(published, seeked))
    }

    @Test
    fun pausingIsPublished() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        assertTrue(PlaybackPublication.worthPublishing(published, published.copy(playbackState = 2, speed = 0f)))
    }

    @Test
    fun anotherQueueItemIsPublished() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        assertTrue(PlaybackPublication.worthPublishing(published, published.copy(activeQueueId = 3L)))
    }

    @Test
    fun aNewSetOfActionsIsPublished() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        assertTrue(PlaybackPublication.worthPublishing(published, published.copy(actions = 4L)))
    }

    @Test
    fun anErrorIsPublished() {
        val published = playing(positionMillis = 10_000, updatedAtMillis = 1_000)
        assertTrue(PlaybackPublication.worthPublishing(published, published.copy(errorMessage = "no page")))
    }
}
