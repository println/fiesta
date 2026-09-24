(function() {
  var RESULT_TIMEOUT_MILLIS = 10000;

  function progress(stage) {
    var context = window.__fiestaVoiceSearch;
    if (context && window.fiestaplugins) {
      window.fiestaplugins.onVoiceSearchProgress(String(context.id), String(context.generation), stage);
    }
  }

  function isSponsored(link) {
    var sponsoredContainer = [
      'ytd-promoted-video-renderer',
      'ytd-ad-slot-renderer',
      'ytd-in-feed-ad-layout-renderer',
      'ytd-display-ad-renderer',
      'ytd-promoted-sparkles-web-renderer',
      'ytm-promoted-video-renderer',
      'ytm-companion-ad-renderer',
      'ytm-ad-slot-renderer',
      '[is-ad]',
      '[data-ad-slot-id]'
    ].join(',');
    if (link.closest(sponsoredContainer)) { return true; }

    var result = link.closest('ytd-video-renderer, ytd-rich-item-renderer, ytm-video-with-context-renderer');
    return !!result && !!result.querySelector(
      '#ad-badge, ytd-ad-badge-renderer, .badge-style-type-ad, [aria-label="Sponsored"], [aria-label="Patrocinado"]'
    );
  }

  function firstVideoLink() {
    var links = document.querySelectorAll('a[href*="/watch?v="]');
    for (var i = 0; i < links.length; i++) {
      if (links[i].offsetParent !== null && !isSponsored(links[i])) { return links[i]; }
    }
    return null;
  }

  function playFirstResult() {
    progress('confirmed');
    window.__webviewex.waitUntil(firstVideoLink, RESULT_TIMEOUT_MILLIS).then(function(link) {
      fiesta.reportSearchResult(!!link);
      if (!link) {
        progress('failed');
        return;
      }
      progress('result');
      location.href = link.href;
    });
  }

  playFirstResult();
})();
