(() => {
  const root = document.documentElement;
  const toggle = document.querySelector('.akira-switch');
  if (!toggle) return;
  const themedImages = document.querySelectorAll('[data-akira-src]');
  const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;

  const GTR_LIGHTS = `
    <div class="gtr-scene" aria-hidden="true">
      <div class="gtr-frame">
        <svg class="taillights gtr" viewBox="0 0 1376 768">
          <defs><filter id="gtr-glow" x="-50%" y="-200%" width="200%" height="500%"><feGaussianBlur stdDeviation="4"/></filter></defs>
          <g filter="url(#gtr-glow)"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="6"/><circle cx="186" cy="545.5" r="9.5" stroke-width="6"/><circle cx="317.5" cy="545.5" r="10" stroke-width="6"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="6"/></g>
          <g class="lamp"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="1.4"/><circle cx="186" cy="545.5" r="9.5" stroke-width="1.4"/><circle cx="317.5" cy="545.5" r="10" stroke-width="1.4"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="1.4"/></g>
        </svg>
        <svg class="brake gtr" viewBox="0 0 1376 768">
          <defs>
            <filter id="gtr-brake-glow" x="-50%" y="-300%" width="200%" height="700%"><feGaussianBlur stdDeviation="6.5"/></filter>
            <filter id="gtr-brake-halo" x="-20%" y="-100%" width="140%" height="300%"><feGaussianBlur stdDeviation="1.7"/></filter>
          </defs>
          <g filter="url(#gtr-brake-glow)"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="10"/><circle cx="186" cy="545.5" r="9.5" stroke-width="10"/><circle cx="317.5" cy="545.5" r="10" stroke-width="10"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="10"/><rect x="233" y="522" width="33" height="7" rx="2"/></g>
          <g filter="url(#gtr-brake-halo)"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="3.5"/><circle cx="186" cy="545.5" r="9.5" stroke-width="3.5"/><circle cx="317.5" cy="545.5" r="10" stroke-width="3.5"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="3.5"/></g>
          <g class="brake-core"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="1.7"/><circle cx="186" cy="545.5" r="9.5" stroke-width="1.7"/><circle cx="317.5" cy="545.5" r="10" stroke-width="1.7"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="1.7"/><rect x="236" y="524.5" width="27" height="2.5" rx="1"/></g>
        </svg>
        <svg class="hazard gtr" viewBox="0 0 1376 768">
          <defs>
            <filter id="gtr-hazard-glow" x="-50%" y="-300%" width="200%" height="700%"><feGaussianBlur stdDeviation="6"/></filter>
            <filter id="gtr-hazard-halo" x="-20%" y="-100%" width="140%" height="300%"><feGaussianBlur stdDeviation="1.7"/></filter>
          </defs>
          <g filter="url(#gtr-hazard-glow)"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="8"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="8"/></g>
          <g filter="url(#gtr-hazard-halo)"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="3.5"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="3.5"/></g>
          <g class="hazard-core"><circle cx="159.3" cy="544.5" r="11.3" stroke-width="1.7"/><circle cx="346.5" cy="544.5" r="11.3" stroke-width="1.7"/></g>
        </svg>
      </div>
    </div>
  `;

  const addGtrLights = () => {
    if (document.querySelector('.gtr-scene')) return;
    document.querySelector('.hero')?.insertAdjacentHTML('afterbegin', GTR_LIGHTS);
  };

  const isFirstActivation = () => {
    try {
      if (localStorage.getItem('akira-seen')) return false;
      localStorage.setItem('akira-seen', '1');
    } catch {}
    return true;
  };

  const flashCueUrl = new URL('cue.wav', document.currentScript.src).href;
  let flashCue;

  const playFlashCue = () => {
    flashCue ??= new Audio(flashCueUrl);
    flashCue.volume = 0.7;
    flashCue.currentTime = 0;
    flashCue.play().catch(() => {});
  };

  const showKanjiFlash = () => {
    document.querySelector('.akira-flash')?.remove();
    const flash = document.createElement('div');
    flash.className = 'akira-flash';
    flash.setAttribute('aria-hidden', 'true');
    flash.innerHTML = '<span class="glow">走</span><span class="glyph">走</span>';
    flash.addEventListener('animationend', event => {
      if (event.target === flash) flash.remove();
    });
    document.body.append(flash);
  };

  const switchTheme = () => {
    const akira = root.classList.toggle('akira');
    toggle.setAttribute('aria-pressed', akira);
    themedImages.forEach(image => {
      const other = image.dataset.akiraSrc;
      image.dataset.akiraSrc = image.getAttribute('src');
      image.src = other;
    });
    if (!akira) {
      document.querySelector('.akira-flash')?.remove();
      flashCue?.pause();
      return;
    }
    addGtrLights();
    if (!isFirstActivation()) return;
    playFlashCue();
    if (!reducedMotion) showKanjiFlash();
  };

  toggle.addEventListener('click', switchTheme);
  toggle.addEventListener('keydown', event => {
    if (event.key !== 'Enter' && event.key !== ' ') return;
    event.preventDefault();
    switchTheme();
  });
})();
