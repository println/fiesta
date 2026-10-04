package proto.media.fiezta.shared.plugins

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiezta.support.plugins.model.PageStage
import proto.media.fiezta.support.plugins.model.PageVisibility
import proto.media.fiezta.support.webviewex.document.DocumentStage
import proto.media.fiezta.support.webviewex.presentation.RenderMode

class PluginLifecycleAlignmentTest {

    @Test
    fun everyDocumentStageMapsToThePageStageWithTheSameJsName() {
        for (stage in DocumentStage.values()) {
            assertEquals(stage.jsName, stage.toPageStage().jsName)
        }
    }

    @Test
    fun everyPageStageIsReachableFromADocumentStage() {
        assertEquals(PageStage.values().toSet(), DocumentStage.values().map { it.toPageStage() }.toSet())
    }

    @Test
    fun everyRenderModeMapsToThePageVisibilityWithTheSameJsName() {
        for (mode in RenderMode.values()) {
            assertEquals(mode.jsName, mode.toPageVisibility().jsName)
        }
    }

    @Test
    fun everyPageVisibilityIsReachableFromARenderMode() {
        assertEquals(PageVisibility.values().toSet(), RenderMode.values().map { it.toPageVisibility() }.toSet())
    }
}
