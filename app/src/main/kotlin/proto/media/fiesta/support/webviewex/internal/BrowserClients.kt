package proto.media.fiesta.support.webviewex.internal

import android.graphics.Bitmap
import android.net.http.SslError
import android.view.KeyEvent
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

internal interface BrowserClientHooks {
    fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?)
    fun onContentPainted(url: String?)
    fun onPageFinished(view: WebView, url: String?)
    fun onVisitedHistoryUpdated(url: String?, isReload: Boolean)
    fun onMainFrameError(url: String, errorCode: Int, description: String)
    fun onSslError(handler: SslErrorHandler, error: SslError)
    fun shouldBlockRequest(url: String): Boolean
    fun onMainFrameLink(url: String, isRedirect: Boolean, method: String): Boolean
    fun onUnhandledKeyEvent(event: KeyEvent)
    fun onRenderProcessGone(view: WebView)
}

internal class BrowserClients(private val hooks: BrowserClientHooks) : WebViewClient() {

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        hooks.onRenderProcessGone(view)
        return true
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        hooks.onPageStarted(view, url, favicon)
    }

    override fun onPageCommitVisible(view: WebView, url: String?) {
        super.onPageCommitVisible(view, url)
        hooks.onContentPainted(url)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        hooks.onPageFinished(view, url)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        hooks.onVisitedHistoryUpdated(url, isReload)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        if (request == null || !request.isForMainFrame) return
        hooks.onMainFrameError(request.url.toString(), error.errorCode, error.description.toString())
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        hooks.onSslError(handler, error)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (request.isForMainFrame) return null
        return if (hooks.shouldBlockRequest(request.url.toString())) blockedResponse() else null
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (!request.isForMainFrame) return false
        return hooks.onMainFrameLink(request.url.toString(), request.isRedirect, request.method)
    }

    @Deprecated("Called on API 23 only", ReplaceWith("shouldOverrideUrlLoading(view, request)"))
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
        hooks.onMainFrameLink(url, isRedirect = false, method = "GET")

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        super.onUnhandledKeyEvent(view, event)
        hooks.onUnhandledKeyEvent(event)
    }

    private fun blockedResponse() = WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
}
