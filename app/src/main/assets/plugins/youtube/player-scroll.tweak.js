(function() {
  var state = window.__csPlayerScroll;
  if (!state) {
    state = window.__csPlayerScroll = { keeping: false, observedPlayer: null };
    install(state);
  }
  keepPlayerInView(state);

  function install(state) {
    var layout = new ResizeObserver(function() { follow(state); });
    layout.observe(document.documentElement);
    state.layout = layout;
    window.addEventListener('scroll', function() { follow(state); }, { passive: true });
    window.addEventListener('resize', function() { keepPlayerInView(state); });
    ['touchstart', 'wheel', 'keydown'].forEach(function(type) {
      window.addEventListener(type, function() { state.keeping = false; }, { capture: true, passive: true });
    });
  }

  function keepPlayerInView(state) {
    state.keeping = onWatch();
    follow(state);
  }

  function follow(state) {
    if (!state.keeping || !onWatch()) { return; }
    var player = playerElement();
    if (!player) { return; }
    if (state.observedPlayer !== player) {
      state.observedPlayer = player;
      state.layout.observe(player);
    }
    revealPlayer(player);
  }

  function onWatch() {
    return location.pathname.indexOf('/watch') === 0;
  }

  function playerElement() {
    return document.querySelector('#player-container-id') || document.querySelector('video');
  }

  function fixedTopBarBottom() {
    var bar = document.querySelector('ytm-mobile-topbar-renderer, #masthead-container');
    if (!bar) { return 0; }
    var position = getComputedStyle(bar).position;
    if (position !== 'fixed' && position !== 'sticky') { return 0; }
    return Math.max(0, bar.getBoundingClientRect().bottom);
  }

  function revealPlayer(player) {
    var rect = player.getBoundingClientRect();
    if (rect.height === 0) { return; }
    var hiddenAbove = rect.top - fixedTopBarBottom();
    var hiddenBelow = rect.bottom - window.innerHeight;
    if (hiddenAbove < 0) {
      window.scrollBy(0, Math.floor(hiddenAbove));
    } else if (hiddenBelow > 0) {
      window.scrollBy(0, Math.ceil(hiddenBelow));
    }
  }
})();
