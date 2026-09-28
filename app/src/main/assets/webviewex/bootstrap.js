(function() {
  var documentId = '__DOCUMENT_ID__';
  if (window.__webviewexDocument === documentId) { return; }
  window.__webviewexDocument = documentId;

  var IDLE_QUIET_MILLIS = 400;
  var IDLE_MAX_WAIT_MILLIS = 8000;
  var bridge = window.webviewexdoc;
  var reported = {};
  var once = {};

  function report(stage) {
    if (!bridge || (stage !== 'route' && reported[stage])) { return; }
    reported[stage] = true;
    try { bridge.onStage(documentId, stage); } catch (e) {}
  }

  function trackReadyState() {
    var state = document.readyState;
    if (state === 'interactive' || state === 'complete') { report('domReady'); startIdleWatch(); }
    if (state === 'complete') { report('loaded'); }
  }

  var idleTimer = null;
  var idleWatching = false;
  function startIdleWatch() {
    if (idleWatching) { return; }
    idleWatching = true;
    var observer = new MutationObserver(scheduleIdle);
    observer.observe(document.documentElement, { childList: true, subtree: true });
    var hardStop = setTimeout(function() { finishIdle(observer); }, IDLE_MAX_WAIT_MILLIS);
    function finishIdle(activeObserver) {
      clearTimeout(idleTimer);
      clearTimeout(hardStop);
      activeObserver.disconnect();
      report('idle');
    }
    function scheduleIdle() {
      clearTimeout(idleTimer);
      idleTimer = setTimeout(function() { finishIdle(observer); }, IDLE_QUIET_MILLIS);
    }
    scheduleIdle();
  }

  function hookRoutes() {
    ['pushState', 'replaceState'].forEach(function(name) {
      var original = history[name];
      history[name] = function() {
        var result = original.apply(this, arguments);
        report('route');
        return result;
      };
    });
    window.addEventListener('popstate', function() { report('route'); });
    window.addEventListener('hashchange', function() { report('route'); });
  }

  var DEFAULT_WAIT_MILLIS = 10000;

  function waitUntil(condition, timeoutMillis) {
    return new Promise(function(resolve) {
      var initial = condition();
      if (initial) { resolve(initial); return; }
      var observer = new MutationObserver(function() {
        var value = condition();
        if (value) { finish(value); }
      });
      var timer = setTimeout(function() { finish(null); }, timeoutMillis || DEFAULT_WAIT_MILLIS);
      function finish(value) {
        observer.disconnect();
        clearTimeout(timer);
        resolve(value);
      }
      observer.observe(document.documentElement, { childList: true, subtree: true });
    });
  }

  function waitFor(selector, timeoutMillis) {
    return waitUntil(function() { return document.querySelector(selector); }, timeoutMillis);
  }

  window.__webviewex = {
    documentId: documentId,
    waitFor: waitFor,
    waitUntil: waitUntil,
    once: function(key, fn) {
      if (once[key]) { return false; }
      once[key] = true;
      fn();
      return true;
    }
  };

  var mediaActionHandlers = {};
  if (navigator.mediaSession && navigator.mediaSession.setActionHandler) {
    var originalSetActionHandler = navigator.mediaSession.setActionHandler.bind(navigator.mediaSession);
    navigator.mediaSession.setActionHandler = function(action, handler) {
      if (handler) { mediaActionHandlers[action] = handler; } else { delete mediaActionHandlers[action]; }
      return originalSetActionHandler(action, handler);
    };
  }

  function electMedia() {
    var current = window.__webviewexActiveMedia;
    if (current && document.contains(current) && !current.ended) { return current; }
    var all = Array.prototype.slice.call(document.querySelectorAll('video, audio'));
    function area(media) { var rect = media.getBoundingClientRect(); return rect.width * rect.height; }
    function audible(media) { return !media.paused && !media.muted && media.volume > 0; }
    function largest(list) { return list.reduce(function(best, media) { return (!best || area(media) > area(best)) ? media : best; }, null); }
    var elected = largest(all.filter(audible)) || largest(all.filter(function(media) { return media.readyState > 0; }));
    window.__webviewexActiveMedia = elected;
    return elected;
  }

  window.__webviewexMedia = {
    play: function() {
      var handler = mediaActionHandlers.play;
      if (handler) { handler(); return; }
      var media = electMedia();
      if (media) { media.play(); }
    },
    pause: function() {
      var handler = mediaActionHandlers.pause;
      if (handler) { handler(); return; }
      var media = electMedia();
      if (media) { media.pause(); }
    },
    seekTo: function(seconds) {
      var handler = mediaActionHandlers.seekto;
      if (handler) { handler({ action: 'seekto', seekTime: seconds }); return; }
      var media = electMedia();
      if (media) { media.currentTime = seconds; }
    },
    seekBy: function(seconds) {
      var media = electMedia();
      window.__webviewexMedia.seekTo((media ? media.currentTime : 0) + seconds);
    },
    markInterrupted: function() {
      window.__webviewexInterrupted = true;
      window.__webviewexMedia.pause();
    },
    resumeIfInterrupted: function() {
      if (window.__webviewexInterrupted) {
        window.__webviewexInterrupted = false;
        window.__webviewexMedia.play();
      }
    },
    observe: function() {
      window.__webviewex.waitUntil(electMedia, 30000).then(function(media) {
        if (media && window.nativecallbacks) { window.nativecallbacks.onVideoDiscovered(); }
      });
    }
  };

  var MEDIA_EVENTS = ['emptied', 'loadstart', 'loadedmetadata', 'seeked', 'waiting', 'playing', 'pause', 'ended', 'timeupdate'];
  var TIME_UPDATE_INTERVAL_MILLIS = 2000;
  var lastTimeUpdate = 0;

  function reportMedia(event) {
    var media = event.target;
    if (!bridge || !media || !bridge.onMediaEvent || !/^(VIDEO|AUDIO)$/.test(media.tagName)) { return; }
    if (event.type === 'timeupdate') {
      var now = Date.now();
      if (now - lastTimeUpdate < TIME_UPDATE_INTERVAL_MILLIS) { return; }
      lastTimeUpdate = now;
    }
    try { bridge.onMediaEvent(documentId, event.type, media.currentTime || 0); } catch (e) {}
  }

  MEDIA_EVENTS.forEach(function(name) { document.addEventListener(name, reportMedia, true); });

  hookRoutes();
  trackReadyState();
  document.addEventListener('readystatechange', trackReadyState);
})();
