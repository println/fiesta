package proto.media.fiesta.support.webviewex.gearhead

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEffect.HOST_IN_BACKGROUND_WINDOW
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEffect.NONE
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEffect.RELEASE_BACKGROUND_WINDOW
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEvent.ATTACHED_TO_SCREEN
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEvent.LEFT_WITHOUT_SCREEN
import proto.media.fiesta.support.webviewex.gearhead.AutoHostEvent.VIEW_DESTROYED
import proto.media.fiesta.support.webviewex.gearhead.AutoHostState.IN_BACKGROUND_WINDOW
import proto.media.fiesta.support.webviewex.gearhead.AutoHostState.NO_VIEW
import proto.media.fiesta.support.webviewex.gearhead.AutoHostState.ON_SCREEN

class AutoHostMachineTest {

    private fun run(vararg events: AutoHostEvent): List<AutoHostTransition> {
        var state = NO_VIEW
        return events.map { event -> AutoHostMachine.next(state, event).also { state = it.state } }
    }

    @Test
    fun coldStartWithoutScreenGoesStraightToTheBackgroundWindow() {
        assertEquals(listOf(AutoHostTransition(IN_BACKGROUND_WINDOW, HOST_IN_BACKGROUND_WINDOW)), run(LEFT_WITHOUT_SCREEN))
    }

    @Test
    fun openingTheScreenMovesTheSameViewWithoutRecreating() {
        val last = run(LEFT_WITHOUT_SCREEN, ATTACHED_TO_SCREEN).last()
        assertEquals(AutoHostTransition(ON_SCREEN, NONE), last)
    }

    @Test
    fun losingTheScreenWhilePlayingFallsBackToTheBackgroundWindow() {
        val last = run(ATTACHED_TO_SCREEN, LEFT_WITHOUT_SCREEN).last()
        assertEquals(AutoHostTransition(IN_BACKGROUND_WINDOW, HOST_IN_BACKGROUND_WINDOW), last)
    }

    @Test
    fun aWarmViewStaysInTheBackgroundWindowAcrossScreenRoundTrips() {
        val states = run(ATTACHED_TO_SCREEN, LEFT_WITHOUT_SCREEN, ATTACHED_TO_SCREEN, LEFT_WITHOUT_SCREEN).map { it.state }
        assertEquals(listOf(ON_SCREEN, IN_BACKGROUND_WINDOW, ON_SCREEN, IN_BACKGROUND_WINDOW), states)
    }

    @Test
    fun destroyingTheViewReleasesTheBackgroundWindow() {
        assertEquals(AutoHostTransition(NO_VIEW, RELEASE_BACKGROUND_WINDOW), run(LEFT_WITHOUT_SCREEN, VIEW_DESTROYED).last())
        assertEquals(AutoHostTransition(NO_VIEW, RELEASE_BACKGROUND_WINDOW), run(ATTACHED_TO_SCREEN, VIEW_DESTROYED).last())
    }

    @Test
    fun destroyingWithoutAViewHasNothingToRelease() {
        assertEquals(AutoHostTransition(NO_VIEW, NONE), run(VIEW_DESTROYED).last())
    }

    @Test
    fun aRecreatedViewAfterTheRendererDiesIsHostedAgain() {
        val last = run(LEFT_WITHOUT_SCREEN, VIEW_DESTROYED, LEFT_WITHOUT_SCREEN).last()
        assertEquals(AutoHostTransition(IN_BACKGROUND_WINDOW, HOST_IN_BACKGROUND_WINDOW), last)
    }

    @Test
    fun aLiveViewIsNeverLeftWithoutAWindow() {
        val events = AutoHostEvent.values()
        for (state in AutoHostState.values()) {
            for (event in events) {
                val next = AutoHostMachine.next(state, event)
                if (next.state == IN_BACKGROUND_WINDOW) assertEquals(HOST_IN_BACKGROUND_WINDOW, next.effect)
                if (next.state == NO_VIEW) assertEquals(event, VIEW_DESTROYED)
            }
        }
    }
}
