package proto.media.fiesta.support.plugins.injection

import proto.media.fiesta.support.plugins.activation.PluginSource
import proto.media.fiesta.support.plugins.contract.PluginLog
import proto.media.fiesta.support.plugins.contract.PluginPageListener
import proto.media.fiesta.support.plugins.model.CarEvent
import proto.media.fiesta.support.plugins.model.PageStage
import proto.media.fiesta.support.plugins.model.PageVisibility
import proto.media.fiesta.support.plugins.model.PluginEnv
import proto.media.fiesta.support.plugins.model.Requirement
import proto.media.fiesta.support.plugins.model.Rerun
import proto.media.fiesta.support.plugins.model.ResolvedEvents
import proto.media.fiesta.support.plugins.model.ResolvedTweak
import proto.media.fiesta.support.plugins.model.TweakOption

class PluginInjector(
    private val evaluate: (String) -> Boolean,
    private val source: PluginSource,
    private val env: PluginEnv,
    private val isRequirementMet: (Requirement, String) -> Boolean,
    private val isOptionEnabled: (String, String, TweakOption?) -> Boolean,
    private val onEventsCleared: () -> Unit,
    private val log: PluginLog
) : PluginPageListener {

    private var activeEvents: ResolvedEvents? = null
    private var handlers: Set<String> = emptySet()
    private var voiceSearchContext: VoiceSearchContext? = null
    private val tweaksRunThisLoad = mutableSetOf<Pair<String, String>>()
    private val lastUrlRunForTweak = mutableMapOf<Pair<String, String>, String>()

    fun onPageStage(stage: PageStage, url: String?) {
        when (stage) {
            PageStage.COMMITTED -> beginDocument(url)
            PageStage.ROUTE_CHANGED -> enterRoute(url)
            else -> {
                updateEvents(url)
                runTweaks(url, stage)
            }
        }
    }

    fun onStackChanged(url: String?) {
        updateEvents(url)
    }

    fun onPageVisibilityChanged(visibility: PageVisibility) {
        if (PAGE_VISIBILITY_EVENT !in handlers) return
        evaluate("fiesta.dispatch('$PAGE_VISIBILITY_EVENT', '${visibility.jsName}');")
    }

    fun onViewDestroyed() {
        handlers = emptySet()
        activeEvents = null
    }

    private fun beginDocument(url: String?) {
        tweaksRunThisLoad.clear()
        lastUrlRunForTweak.clear()
        handlers = emptySet()
        activeEvents = null
        updateEvents(url)
        publishVoiceSearchContext()
        runTweaks(url, PageStage.COMMITTED)
    }

    private fun enterRoute(url: String?) {
        updateEvents(url)
        runTweaks(url, PageStage.ROUTE_CHANGED)
        runTweaks(url, PageStage.LOADED)
    }

    fun runVoiceScript(pluginId: String, script: String) {
        if (activeEvents == null) evaluate(source.runtime())
        log.debug("voice script -> $pluginId/$script")
        evaluate(source.script(pluginId, script))
    }

    fun dispatch(event: CarEvent, argument: String? = null): Boolean {
        if (event.jsName !in handlers) return false
        val jsArgument = argument?.let { ", " + JsString.quote(it) } ?: ""
        evaluate("fiesta.dispatch('${event.jsName}'$jsArgument);")
        return true
    }

    fun setVoiceSearchContext(id: Long, generation: Long, query: String) {
        voiceSearchContext = VoiceSearchContext(id, generation, query)
        publishVoiceSearchContext()
    }

    fun clearVoiceSearchContext() {
        voiceSearchContext = null
        evaluate("window.__fiestaVoiceSearch = null;")
    }

    override fun onHandlersChanged(events: Set<String>) {
        handlers = events
    }

    override fun onVoiceSearchProgress(id: Long, generation: Long, stage: String) {
        voiceSearchProgressListener?.invoke(id, generation, stage)
    }

    var voiceSearchProgressListener: ((Long, Long, String) -> Unit)? = null

    private fun publishVoiceSearchContext() {
        val context = voiceSearchContext ?: return
        evaluate(
            "window.__fiestaVoiceSearch = { id: ${context.id}, generation: ${context.generation}, " +
                "query: ${JsString.quote(context.query)} };"
        )
    }

    private fun updateEvents(url: String?) {
        if (env != PluginEnv.CAR || url == null) return
        val resolved = PluginResolver(source.stack()).eventsFor(url)
        if (resolved == activeEvents) return
        if (activeEvents != null) {
            evaluate("fiesta.reset();")
            onEventsCleared()
        }
        activeEvents = resolved
        handlers = emptySet()
        if (resolved != null) {
            log.debug("events -> ${resolved.pluginId}/${resolved.script}")
            evaluate(source.runtime())
            evaluate(source.script(resolved.pluginId, resolved.script))
        } else {
            log.debug("events -> none")
        }
    }

    private fun runTweaks(url: String?, runAt: PageStage) {
        if (url == null) return
        val tweaks = PluginResolver(source.stack()).tweaksFor(url, env)
        for (tweak in tweaks) {
            if (tweak.spec.runAt != runAt) continue
            if (!isEligible(url, tweak)) continue
            if (evaluate(source.script(tweak.pluginId, tweak.spec.script))) markRan(url, tweak)
        }
    }

    private fun isEligible(url: String, tweak: ResolvedTweak): Boolean {
        val requirement = tweak.spec.requires
        if (requirement != null && !isRequirementMet(requirement, url)) return false
        if (!isOptionEnabled(tweak.pluginId, tweak.spec.script, tweak.spec.option)) return false

        val key = tweak.pluginId to tweak.spec.script
        return when (tweak.spec.rerun) {
            Rerun.LOAD -> key !in tweaksRunThisLoad
            Rerun.URL_CHANGE -> lastUrlRunForTweak[key] != url
        }
    }

    private fun markRan(url: String, tweak: ResolvedTweak) {
        val key = tweak.pluginId to tweak.spec.script
        when (tweak.spec.rerun) {
            Rerun.LOAD -> tweaksRunThisLoad.add(key)
            Rerun.URL_CHANGE -> lastUrlRunForTweak[key] = url
        }
    }

    companion object {
        private const val PAGE_VISIBILITY_EVENT = "renderModeChanged"
    }

    private data class VoiceSearchContext(val id: Long, val generation: Long, val query: String)
}
