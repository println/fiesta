(function() {
  function video() {
    return document.querySelector('video');
  }
  function seekBy(seconds) {
    var current = video();
    if (current) { current.currentTime = Math.max(0, current.currentTime + seconds); }
  }

  fiesta.on('nextClick', function() { fiesta.toast('nextClick'); });
  fiesta.on('previousClick', function() { fiesta.toast('previousClick'); });
  fiesta.on('nextLongPress', function() { seekBy(10); fiesta.toast('nextLongPress'); });
  fiesta.on('previousLongPress', function() { seekBy(-10); fiesta.toast('previousLongPress'); });
  fiesta.setAvailable({ next: true, previous: true });
})();
