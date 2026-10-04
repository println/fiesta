(function() {
  function shortsButton(index) {
    return document.querySelectorAll('.ytShortsCarouselShortsA11yNavButton')[index];
  }

  fiezta.on('nextClick', function() {
    var nextShort = shortsButton(1);
    if (nextShort && !nextShort.disabled) { nextShort.click(); }
  });

  fiezta.on('previousClick', function() {
    var previousShort = shortsButton(0);
    if (previousShort && !previousShort.disabled) { previousShort.click(); }
  });

  fiezta.setInterval(function() {
    var nextShort = shortsButton(1);
    var previousShort = shortsButton(0);
    fiezta.setAvailable({
      next: !!nextShort && !nextShort.disabled,
      previous: !!previousShort && !previousShort.disabled
    });
  }, 500);
  fiezta.setQueueProvider(function() { return { shape: 'stream' }; });
  fiezta.setMetadataProvider(function() {
    var match = location.pathname.match(/\/shorts\/([^/?#]+)/);
    return match ? { id: match[1] } : null;
  });
})();
