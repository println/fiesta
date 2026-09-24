(function() {
  function shortsButton(index) {
    return document.querySelectorAll('.ytShortsCarouselShortsA11yNavButton')[index];
  }

  fiesta.on('nextClick', function() {
    var nextShort = shortsButton(1);
    if (nextShort && !nextShort.disabled) { nextShort.click(); }
  });

  fiesta.on('previousClick', function() {
    var previousShort = shortsButton(0);
    if (previousShort && !previousShort.disabled) { previousShort.click(); }
  });

  fiesta.setInterval(function() {
    var nextShort = shortsButton(1);
    var previousShort = shortsButton(0);
    fiesta.setAvailable({
      next: !!nextShort && !nextShort.disabled,
      previous: !!previousShort && !previousShort.disabled
    });
  }, 500);
  fiesta.setQueueProvider(function() { return { shape: 'stream' }; });
  fiesta.setMetadataProvider(function() {
    var match = location.pathname.match(/\/shorts\/([^/?#]+)/);
    return match ? { id: match[1] } : null;
  });
})();
