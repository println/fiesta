package proto.media.fiesta.support.webviewex.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTrackerTest {

    private val tracker = NavigationTracker()

    @Test
    fun redirectsAccumulateInTheChain() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        tracker.redirect("https://b.com/")
        val transaction = tracker.redirect("https://c.com/")
        assertEquals(listOf("https://b.com/", "https://c.com/"), transaction?.redirects)
        assertEquals("https://c.com/", transaction?.currentUrl)
        assertTrue(transaction!!.involves("https://a.com"))
    }

    @Test
    fun beginningANewNavigationAbortsThePreviousOne() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        tracker.begin(NavigationOrigin.APP, "b.com", "https://b.com/")
        assertNull(tracker.mainFrameFailure("https://a.com/"))
        assertEquals(NavigationState.PENDING, tracker.current?.state)
    }

    @Test
    fun errorOfAReplacedNavigationIsIgnored() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        tracker.begin(NavigationOrigin.APP, "b.com", "https://b.com/")
        assertNull(tracker.strictFailure("https://a.com/"))
    }

    @Test
    fun strictFailureRejectsUrlsOutsideTheChain() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        assertNull(tracker.strictFailure("https://iframe.example/"))
        assertEquals(NavigationState.PENDING, tracker.current?.state)
    }

    @Test
    fun mainFrameFailureAcceptsUnreportedRedirectTarget() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        assertNotNull(tracker.mainFrameFailure("https://elsewhere.example/"))
        assertEquals(NavigationState.FAILED, tracker.current?.state)
    }

    @Test
    fun pageStartedForTheRequestedUrlCommitsTheSameTransaction() {
        val begun = tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com")
        val started = tracker.onPageStarted("https://a.com/")
        assertEquals(begun.id, started.id)
        assertEquals(NavigationState.COMMITTED, started.state)
    }

    @Test
    fun pageStartedForUnknownUrlCreatesPageTransaction() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        val started = tracker.onPageStarted("https://other.com/")
        assertEquals(NavigationOrigin.PAGE, started.origin)
    }

    @Test
    fun pageFinishedSucceedsACommittedTransaction() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        tracker.onPageStarted("https://a.com/")
        assertEquals(NavigationState.SUCCEEDED, tracker.onPageFinished("https://a.com/")?.state)
    }

    @Test
    fun pageFinishedDoesNothingAfterFailure() {
        tracker.begin(NavigationOrigin.APP, "a.com", "https://a.com/")
        tracker.mainFrameFailure("https://a.com/")
        assertNull(tracker.onPageFinished("https://a.com/"))
    }

    @Test
    fun normalizeAddsPathAndDropsFragment() {
        assertEquals("https://a.com/", NavigationTransaction.normalize("https://a.com#top"))
        assertEquals("https://a.com/?q=1", NavigationTransaction.normalize("https://a.com?q=1"))
    }

    @Test
    fun pageStartedDoesNotCountAsPaintedDocument() {
        tracker.begin(NavigationOrigin.APP, "example.com", "https://example.com/")
        tracker.onPageStarted("https://example.com/")

        assertFalse(tracker.current!!.documentCommitted)

        tracker.onContentPainted("https://example.com/")

        assertTrue(tracker.current!!.documentCommitted)
    }
}
