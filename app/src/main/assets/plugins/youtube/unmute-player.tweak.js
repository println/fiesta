(function() {
  function unmuteIfPlayerVideo(video) {
    if (!(video instanceof HTMLVideoElement) || video.paused || !video.muted || location.pathname.indexOf('/watch') !== 0) { return; }
    var player = document.getElementById('movie_player');
    if (!player || !player.contains(video)) { return; }
    if (typeof player.unMute === 'function') { player.unMute(); }
    video.muted = false;
    if (video.volume === 0) { video.volume = 1; }
  }

  unmuteIfPlayerVideo(document.querySelector('#movie_player video'));
  if (window.__fiestaUnmutePlayer) { return; }
  window.__fiestaUnmutePlayer = true;
  document.addEventListener('playing', function(e) { unmuteIfPlayerVideo(e.target); }, true);
})();
