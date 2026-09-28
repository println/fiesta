package proto.media.fiesta.support.webviewex.document

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentLifecycleTest {

    private val lifecycle = DocumentLifecycle()

    @Test
    fun stageOfAnOldDocumentIsIgnored() {
        val old = lifecycle.begin()
        lifecycle.begin()
        assertFalse(lifecycle.enter(old, DocumentStage.DOM_READY))
    }

    @Test
    fun stickyStageIsReportedOnce() {
        val id = lifecycle.begin()
        assertTrue(lifecycle.enter(id, DocumentStage.DOM_READY))
        assertFalse(lifecycle.enter(id, DocumentStage.DOM_READY))
        assertTrue(lifecycle.hasReached(DocumentStage.DOM_READY))
    }

    @Test
    fun routeChangeCanRepeat() {
        val id = lifecycle.begin()
        assertTrue(lifecycle.enter(id, DocumentStage.ROUTE_CHANGED))
        assertTrue(lifecycle.enter(id, DocumentStage.ROUTE_CHANGED))
    }

    @Test
    fun actionWaitsForTheStageAndRunsOnce() {
        val id = lifecycle.begin()
        var runs = 0
        lifecycle.whenReached(id, DocumentStage.IDLE) { runs++ }
        assertEquals(0, runs)
        lifecycle.enter(id, DocumentStage.IDLE)
        lifecycle.enter(id, DocumentStage.IDLE)
        assertEquals(1, runs)
    }

    @Test
    fun actionRunsAtOnceWhenTheStageWasAlreadyReached() {
        val id = lifecycle.begin()
        lifecycle.enter(id, DocumentStage.LOADED)
        var runs = 0
        lifecycle.whenReached(id, DocumentStage.LOADED) { runs++ }
        assertEquals(1, runs)
    }

    @Test
    fun actionQueuedForADeadDocumentIsDropped() {
        val id = lifecycle.begin()
        var runs = 0
        lifecycle.whenReached(id, DocumentStage.IDLE) { runs++ }
        val next = lifecycle.begin()
        lifecycle.enter(next, DocumentStage.IDLE)
        assertEquals(0, runs)
    }

    @Test
    fun scriptHostRejectsScriptsForAnotherDocument() {
        val evaluated = mutableListOf<String>()
        val host = ScriptHost(lifecycle) { evaluated.add(it); true }
        val old = lifecycle.begin()
        lifecycle.begin()
        assertFalse(host.evaluate("x", forDocument = old))
        assertTrue(host.evaluate("y"))
        assertEquals(listOf("y"), evaluated)
    }
}
