package proto.media.fiesta.support.webviewex.gearhead

import android.view.KeyEvent
import android.webkit.WebView
import org.json.JSONObject

private const val ACTIVE_MEDIA_JS = "(window.__fiestaMedia && window.__fiestaMedia()) || document.querySelector('video')"
private const val ACTIVATION_KEY_CODE = KeyEvent.KEYCODE_F10

fun WebView.showCarKeyboardIfInput() {
    evaluateJavascript(
        "setTimeout(function() {" +
            "if (!window.nativecallbacks || !document.activeElement) { return; }" +
            "if (document.activeElement instanceof HTMLInputElement || document.activeElement instanceof HTMLTextAreaElement) {" +
            "window.nativecallbacks.showKeyboard(document.activeElement.value);" +
            "} else if (document.activeElement.getAttribute('contenteditable') == 'true') {" +
            "window.nativecallbacks.showKeyboard(document.activeElement.innerText);" +
            "}" +
            "}, 500);",
        null
    )
}

fun WebView.enterCarKeyboardText(text: String) {
    evaluateJavascript(
        "(function(t){" +
            "  var a = document.activeElement;" +
            "  if (!a) { return; }" +
            "  if (a.isContentEditable) { a.innerText = t; } else { a.value = t; }" +
            "  a.dispatchEvent(new Event('change'));" +
            "})(" + JSONObject.quote(text) + ")",
        null
    )
}

fun WebView.sendCarKeyboardEnter() {
    evaluateJavascript(
        "(function() {" +
            "  var opts = {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true, cancelable:true};" +
            "  var el = document.activeElement;" +
            "  el.dispatchEvent(new KeyboardEvent('keydown', opts));" +
            "  el.dispatchEvent(new KeyboardEvent('keyup', opts));" +
            "})();",
        null
    )
}

fun WebView.scrollCarActiveElementIntoView(alignToTop: Boolean) {
    evaluateJavascript(
        "setTimeout(function() { var element = document.activeElement;" +
            "if (!element) { return; }" +
            "var elementRect = element.getBoundingClientRect();" +
            "var absoluteElementTop = elementRect.top + window.pageYOffset;" +
            "var middle = absoluteElementTop - (window.innerHeight / 2);" +
            "window.scrollTo(0, middle); }, 500);",
        null
    )
}

fun WebView.setCarAspectRatio(mode: String) {
    evaluateJavascript(
        "var v = $ACTIVE_MEDIA_JS;" +
            "if (v) { v.style.objectFit = '" + mode + "'; }",
        null
    )
}

// Chromium only honours requestFullscreen() inside a transient user activation, and a tap on a
// native View never reaches the page. A key event dispatched into the WebView is an activation
// triggering event, unlike a synthesized touch, and activates no link. Removing it makes the
// control fail silently with "Permissions check failed".
private fun WebView.grantCarUserActivation() {
    dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, ACTIVATION_KEY_CODE))
    dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, ACTIVATION_KEY_CODE))
}

fun WebView.requestCarFullScreen() {
    grantCarUserActivation()
    evaluateJavascript(
        "(function() {" +
            "  function area(v) { var r = v.getBoundingClientRect(); return r.width * r.height; }" +
            "  function playable(v) { return area(v) > 0 && v.readyState > 0; }" +
            "  function largest(list) {" +
            "    return list.reduce(function(best, v) { return (!best || area(v) > area(best)) ? v : best; }, null);" +
            "  }" +
            "  var all = Array.prototype.slice.call(document.querySelectorAll('video'));" +
            "  var active = window.__fiestaActiveMedia;" +
            "  var target = (active && document.contains(active) && playable(active)) ? active : null;" +
            "  if (!target) { target = largest(all.filter(function(v) { return !v.paused && playable(v); })); }" +
            "  if (!target) { target = largest(all.filter(playable)); }" +
            "  if (!target) {" +
            "    if (window.nativecallbacks) { window.nativecallbacks.onFullScreenUnavailable(); }" +
            "    return;" +
            "  }" +
            "  var request = target.requestFullscreen || target.webkitRequestFullscreen;" +
            "  if (!request) {" +
            "    if (window.nativecallbacks) { window.nativecallbacks.onFullScreenUnavailable(); }" +
            "    return;" +
            "  }" +
            "  var result = request.call(target);" +
            "  if (result && result.catch) {" +
            "    result.catch(function() {" +
            "      if (window.nativecallbacks) { window.nativecallbacks.onFullScreenUnavailable(); }" +
            "    });" +
            "  }" +
            "})();",
        null
    )
}
