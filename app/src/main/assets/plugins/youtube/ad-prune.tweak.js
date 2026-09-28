(function() {
  if (window.__csAdPrune) { return; }
  window.__csAdPrune = true;

  var keys = ['adPlacements', 'playerAds', 'adSlots', 'adBreakHeartbeatParams'];
  var origParse = JSON.parse;
  JSON.parse = function() {
    var result = origParse.apply(this, arguments);
    if (result && typeof result === 'object') {
      for (var i = 0; i < keys.length; i++) {
        if (keys[i] in result) { delete result[keys[i]]; }
      }
    }
    return result;
  };

  setInterval(function() {
    var p = document.getElementById('movie_player');
    if (!p || !p.classList || !p.classList.contains('ad-showing')) { return; }
    var v = document.querySelector('video');
    if (v && isFinite(v.duration) && v.duration > 0) {
      try { v.currentTime = v.duration; } catch (e) {}
    }
    var b = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern');
    if (b) { b.click(); }
  }, 500);
})();
