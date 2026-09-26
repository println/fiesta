(() => {
  const root = document.documentElement;
  const toggle = document.querySelector('.akira-switch');
  if (!toggle) return;
  const themedImages = document.querySelectorAll('[data-akira-src]');
  const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;

  const GTR_LIGHTS = `
    <div class="gtr-scene" aria-hidden="true">
      <div class="gtr-frame">
        <svg class="taillights gtr" viewBox="0 0 1673 940">
          <defs><filter id="gtr-glow" x="-50%" y="-200%" width="200%" height="500%"><feGaussianBlur stdDeviation="5"/></filter></defs>
          <g filter="url(#gtr-glow)"><circle cx="193" cy="664.5" r="13.2" stroke-width="7"/><circle cx="225.5" cy="666.5" r="11.5" stroke-width="7"/><circle cx="385.5" cy="666.5" r="12" stroke-width="7"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="7"/></g>
          <g class="lamp"><circle cx="193" cy="664.5" r="13.2" stroke-width="1.6"/><circle cx="225.5" cy="666.5" r="11.5" stroke-width="1.6"/><circle cx="385.5" cy="666.5" r="12" stroke-width="1.6"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="1.6"/></g>
        </svg>
        <svg class="brake gtr" viewBox="0 0 1673 940">
          <defs>
            <filter id="gtr-brake-glow" x="-50%" y="-300%" width="200%" height="700%"><feGaussianBlur stdDeviation="8"/></filter>
            <filter id="gtr-brake-halo" x="-20%" y="-100%" width="140%" height="300%"><feGaussianBlur stdDeviation="2"/></filter>
          </defs>
          <g filter="url(#gtr-brake-glow)"><circle cx="193" cy="664.5" r="13.2" stroke-width="12"/><circle cx="225.5" cy="666.5" r="11.5" stroke-width="12"/><circle cx="385.5" cy="666.5" r="12" stroke-width="12"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="12"/><rect x="283" y="638" width="40" height="8" rx="2"/></g>
          <g filter="url(#gtr-brake-halo)"><circle cx="193" cy="664.5" r="13.2" stroke-width="4"/><circle cx="225.5" cy="666.5" r="11.5" stroke-width="4"/><circle cx="385.5" cy="666.5" r="12" stroke-width="4"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="4"/></g>
          <g class="brake-core"><circle cx="193" cy="664.5" r="13.2" stroke-width="2"/><circle cx="225.5" cy="666.5" r="11.5" stroke-width="2"/><circle cx="385.5" cy="666.5" r="12" stroke-width="2"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="2"/><rect x="286" y="641" width="34" height="3" rx="1"/></g>
        </svg>
        <svg class="hazard gtr" viewBox="0 0 1673 940">
          <defs>
            <filter id="gtr-hazard-glow" x="-50%" y="-300%" width="200%" height="700%"><feGaussianBlur stdDeviation="7"/></filter>
            <filter id="gtr-hazard-halo" x="-20%" y="-100%" width="140%" height="300%"><feGaussianBlur stdDeviation="2"/></filter>
          </defs>
          <g filter="url(#gtr-hazard-glow)"><circle cx="193" cy="664.5" r="13.2" stroke-width="10"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="10"/></g>
          <g filter="url(#gtr-hazard-halo)"><circle cx="193" cy="664.5" r="13.2" stroke-width="4"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="4"/></g>
          <g class="hazard-core"><circle cx="193" cy="664.5" r="13.2" stroke-width="2"/><circle cx="420.5" cy="664.5" r="13.5" stroke-width="2"/></g>
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
