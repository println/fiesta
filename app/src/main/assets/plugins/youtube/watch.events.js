(function() {
  function requestVoicePlayback() {
    var context = window.__fiestaVoiceSearch;
    if (!context) { return; }
    var requested = false;
    var observer;
    function tryPlay() {
      if (requested) { return true; }
      var video = document.querySelector('#movie_player video, video');
      if (!video) { return false; }
      requested = true;
      if (observer) { observer.disconnect(); }
      var progress = function(stage) {
        if (window.fiestaplugins) {
          window.fiestaplugins.onVoiceSearchProgress(String(context.id), String(context.generation), stage);
        }
      };
      var result = video.play();
      if (result && typeof result.then === 'function') {
        result.then(function() { progress('playRequested'); }, function() { progress('failed'); });
      } else {
        progress('playRequested');
      }
      return true;
    }
    if (tryPlay()) { return; }
    observer = new MutationObserver(function() { tryPlay(); });
    observer.observe(document.documentElement, { childList: true, subtree: true });
    setTimeout(function() { if (!requested) { observer.disconnect(); } }, 10000);
  }

  function videoMetadata() {
    var player = document.getElementById('movie_player');
    var data = player && typeof player.getVideoData === 'function' ? player.getVideoData() : null;
    if (!data || !data.title) { return null; }
    var artwork = data.video_id ? 'https://i.ytimg.com/vi/' + data.video_id + '/hqdefault.jpg' : '';
    return { id: data.video_id || '', title: data.title, artist: data.author, artwork: artwork };
  }

  function skipAdButton() {
    return document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button, .ytp-ad-skip-button-modern');
  }
  function playerButton(desktopSelector, mobileIndex) {
    return document.querySelector(desktopSelector) ||
      document.querySelectorAll('.player-middle-controls-prev-next-button')[mobileIndex];
  }
  function isEnabled(button) {
    return button.getAttribute('aria-disabled') !== 'true';
  }
  function isShowingAd() {
    return !!document.querySelector('.ad-showing video, .ytp-skip-ad-button, .ytp-ad-skip-button, .ytp-ad-skip-button-modern');
  }

  fiesta.on('nextClick', function() {
    var skipAd = skipAdButton();
    if (skipAd) { skipAd.click(); return; }
    var adVideo = document.querySelector('.ad-showing video');
    if (adVideo && isFinite(adVideo.duration)) { adVideo.currentTime = adVideo.duration; return; }
    var next = playerButton('.ytp-next-button', 1);
    if (next) {
      if (isEnabled(next)) { next.click(); }
      return;
    }
    var player = document.getElementById('movie_player');
    if (player && typeof player.nextVideo === 'function') { player.nextVideo(); }
  });

  fiesta.on('previousClick', function() {
    var previous = playerButton('.ytp-prev-button', 0);
    if (previous && isEnabled(previous)) {
      previous.click();
    } else {
      var video = document.querySelector('video');
      if (video) { video.currentTime = 0; }
    }
  });

  function tapPlayerSide(forward) {
    var player = document.getElementById('movie_player');
    var surface = document.querySelector('.player-controls-background');
    if (!player || !surface) { return false; }
    var bounds = player.getBoundingClientRect();
    surface.dispatchEvent(new MouseEvent('click', {
      bubbles: true, cancelable: true, composed: true, view: window,
      clientX: bounds.left + bounds.width * (forward ? 0.9 : 0.1),
      clientY: bounds.top + bounds.height / 2
    }));
    return true;
  }
  function doubleTapSeek(forward) {
    if (!tapPlayerSide(forward)) {
      var video = document.querySelector('video');
      if (video) { video.currentTime += forward ? 10 : -10; }
      return;
    }
    setTimeout(function() { tapPlayerSide(forward); }, 200);
  }

  fiesta.on('nextLongPress', function() { doubleTapSeek(true); });
  fiesta.on('previousLongPress', function() { doubleTapSeek(false); });

  fiesta.setInterval(function() {
    var next = playerButton('.ytp-next-button', 1);
    var previous = playerButton('.ytp-prev-button', 0);
    var available = {};
    if (isShowingAd()) {
      available.next = true;
    } else if (next) {
      available.next = isEnabled(next);
    }
    if (previous) { available.previous = isEnabled(previous); }
    fiesta.setAvailable(available);
  }, 500);
  function videoIdOf(href) {
    var match = String(href || '').match(/[?&]v=([^&]+)/);
    return match ? match[1] : '';
  }
  function thumbnailOf(videoId) {
    return videoId ? 'https://i.ytimg.com/vi/' + videoId + '/mqdefault.jpg' : '';
  }
  function neighbourFromPlayerButton(selector) {
    var button = document.querySelector(selector);
    if (!button || !isEnabled(button)) { return null; }
    var videoId = videoIdOf(button.getAttribute('href'));
    if (!videoId) { return null; }
    return {
      title: button.getAttribute('data-tooltip-text') || '',
      subtitle: '',
      artwork: thumbnailOf(videoId)
    };
  }
  function nextFromRelatedList() {
    var item = document.querySelector('ytm-video-with-context-renderer');
    var link = item && item.querySelector('a[href*="v="]');
    var videoId = link && videoIdOf(link.getAttribute('href'));
    if (!videoId) { return null; }
    var headline = item.querySelector('.media-item-headline');
    var byline = item.querySelector('.ytmBadgeAndBylineRendererItemByline');
    return {
      title: headline ? headline.textContent.trim() : '',
      subtitle: byline ? byline.textContent.trim() : '',
      artwork: thumbnailOf(videoId)
    };
  }
  function currentEntry() {
    var metadata = videoMetadata();
    if (!metadata) { return null; }
    return { title: metadata.title, subtitle: metadata.artist || '', artwork: metadata.artwork };
  }

  var MOBILE_PANEL = {
    item: 'ytm-playlist-panel-video-renderer',
    title: '.YtmCompactMediaItemHeadline',
    byline: '.YtmCompactMediaItemByline',
    link: 'a.YtmCompactMediaItemMetadataContent',
    header: '.playlist-engagement-panel-header',
    isCurrent: function(item) { return item.getAttribute('aria-selected') === 'true'; }
  };
  var DESKTOP_PANEL = {
    item: 'ytd-playlist-panel-video-renderer',
    title: '#video-title',
    byline: '#byline',
    link: 'a#wc-endpoint',
    header: 'ytd-playlist-panel-renderer #playlist-title',
    isCurrent: function(item) { return item.hasAttribute('selected'); }
  };
  function panel() {
    return document.querySelector(DESKTOP_PANEL.item) ? DESKTOP_PANEL : MOBILE_PANEL;
  }
  function panelItems(layout) {
    return [].slice.call(document.querySelectorAll(layout.item));
  }
  function textOf(item, selector) {
    var element = item.querySelector(selector);
    if (!element) { return ''; }
    return (element.getAttribute('title') || element.textContent || '').trim();
  }
  function panelTitle(layout) {
    var header = document.querySelector(layout.header);
    if (!header) { return ''; }
    return (header.innerText || header.textContent || '').split(String.fromCharCode(10))[0].trim();
  }
  function playlistQueue() {
    var layout = panel();
    var items = panelItems(layout);
    if (!items.length) { return null; }
    return {
      shape: 'list',
      title: panelTitle(layout),
      cursor: items.findIndex(layout.isCurrent),
      entries: items.map(function(item) {
        var link = item.querySelector('a[href*="v="]');
        return {
          title: textOf(item, layout.title),
          subtitle: textOf(item, layout.byline),
          artwork: thumbnailOf(link ? videoIdOf(link.getAttribute('href')) : '')
        };
      })
    };
  }
  function streamQueue() {
    var current = currentEntry();
    if (!current) { return { shape: 'stream' }; }
    var next = neighbourFromPlayerButton('.ytp-next-button') || nextFromRelatedList();
    return { shape: 'stream', entries: next ? [current, next] : [current], cursor: 0 };
  }

  fiesta.setQueueProvider(function() {
    return playlistQueue() || streamQueue();
  });
  fiesta.on('queueItem', function(index) {
    var layout = panel();
    var item = panelItems(layout)[Number(index)];
    var link = item && item.querySelector(layout.link);
    if (link) {
      link.click();
    } else if (Number(index) > 0) {
      fiesta.dispatch('nextClick');
    }
  });
  fiesta.setMetadataProvider(videoMetadata);
  requestVoicePlayback();
})();
