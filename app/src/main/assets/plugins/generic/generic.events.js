(function() {
  function video() {
    return document.querySelector('video');
  }
  function seekBy(seconds) {
    var current = video();
    if (current) { current.currentTime = Math.max(0, current.currentTime + seconds); }
  }

  fiezta.on('nextClick', function() { fiezta.toast('nextClick'); });
  fiezta.on('previousClick', function() { fiezta.toast('previousClick'); });
  fiezta.on('nextLongPress', function() { seekBy(10); fiezta.toast('nextLongPress'); });
  fiezta.on('previousLongPress', function() { seekBy(-10); fiezta.toast('previousLongPress'); });
  fiezta.setAvailable({ next: true, previous: true });
})();
