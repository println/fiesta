import { adb, pidOf } from './adb.mjs';
import { config } from './config.mjs';

export async function pageSession(urlPattern = /youtube/) {
  const pid = await pidOf();
  if (!pid) throw new Error('the app is not running');
  await adb('forward', `tcp:${config.devtoolsPort}`, `localabstract:webview_devtools_remote_${pid}`);
  const pages = await (await fetch(`http://127.0.0.1:${config.devtoolsPort}/json`)).json();
  const page = pages.find((candidate) => urlPattern.test(candidate.url));
  if (!page) throw new Error(`no page matching ${urlPattern} in ${pages.map((p) => p.url).join(', ')}`);
  return PageSession.open(page.webSocketDebuggerUrl);
}

class PageSession {
  static open(url) {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(url);
      socket.addEventListener('open', () => resolve(new PageSession(socket)));
      socket.addEventListener('error', () => reject(new Error(`could not open ${url}`)));
    });
  }

  constructor(socket) {
    this.socket = socket;
    this.nextId = 1;
    this.pending = new Map();
    socket.addEventListener('message', (message) => {
      const reply = JSON.parse(message.data);
      const waiter = this.pending.get(reply.id);
      if (!waiter) return;
      this.pending.delete(reply.id);
      if (reply.error || reply.result?.exceptionDetails) {
        waiter.reject(new Error(JSON.stringify(reply.error ?? reply.result.exceptionDetails)));
      } else {
        waiter.resolve(reply.result.result.value);
      }
    });
  }

  evaluate(expression) {
    const id = this.nextId++;
    this.socket.send(JSON.stringify({
      id,
      method: 'Runtime.evaluate',
      params: { expression, returnByValue: true, awaitPromise: true }
    }));
    return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }));
  }

  media() {
    return this.evaluate(`(() => {
      const media = [...document.querySelectorAll('video, audio')]
        .sort((a, b) => (b.duration || 0) - (a.duration || 0))[0];
      return {
        url: location.href,
        hidden: document.hidden,
        screen: screen.width + 'x' + screen.height,
        found: !!media,
        paused: media ? media.paused : null,
        muted: media ? media.muted : null,
        time: media ? media.currentTime : null,
        duration: media ? media.duration : null
      };
    })()`);
  }

  playerInView() {
    return this.evaluate(`(() => {
      const player = document.querySelector('#player-container-id') || document.querySelector('video');
      const bar = document.querySelector('ytm-mobile-topbar-renderer, #masthead-container');
      const fixed = bar && ['fixed', 'sticky'].includes(getComputedStyle(bar).position);
      const barBottom = fixed ? Math.max(0, bar.getBoundingClientRect().bottom) : 0;
      const rect = player ? player.getBoundingClientRect() : null;
      return {
        found: !!rect && rect.height > 0,
        top: rect ? Math.round(rect.top) : null,
        bottom: rect ? Math.round(rect.bottom) : null,
        barBottom: Math.round(barBottom),
        viewport: innerHeight,
        visible: !!rect && rect.top >= barBottom - 1 && rect.bottom <= innerHeight + 1
      };
    })()`);
  }

  scrollPageBy(pixels) {
    return this.evaluate(`window.scrollBy(0, ${pixels}), window.scrollY`);
  }

  seekToEnd(secondsBefore) {
    return this.evaluate(`(() => {
      const media = [...document.querySelectorAll('video, audio')]
        .sort((a, b) => (b.duration || 0) - (a.duration || 0))[0];
      media.currentTime = media.duration - ${secondsBefore};
      return media.currentTime;
    })()`);
  }

  close() {
    this.socket.close();
  }
}
