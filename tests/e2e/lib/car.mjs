import { execFile } from 'node:child_process';
import { setTimeout as sleep } from 'node:timers/promises';
import { promisify } from 'node:util';
import { Logcat, prefValue, readPrefs, shell } from './adb.mjs';
import { config } from './config.mjs';
import { pageSession } from './devtools.mjs';
import { HeadUnit } from './dhu.mjs';

const run = promisify(execFile);

const LAUNCHER_FIESTA_ICON = { x: 633, y: 70 };
const LAUNCHER_COMPONENT = 'com.google.android.projection.gearhead/com.google.android.projection.gearhead.system.applauncher.GhAppLauncherService';
const FIESTA_CAR_COMPONENT = `${config.packageName}/proto.media.fiesta.features.domain.car.app.CarService`;
const FOREGROUND = /CAR\.CAM\s*: makeForeground for component ComponentInfo\{([^}]+)\}/;

const TAGS = ['CarPlayer', 'WebViewExGearhead', 'PlaybackSession', 'WebViewEx', 'MediaServer', 'CarScreenLauncher', 'CAR.CAM'];

export class CarSession {
  constructor(unit, log) {
    this.unit = unit;
    this.log = log;
  }

  static async start() {
    await stopStrayHeadUnits();
    const log = new Logcat(TAGS);
    const session = new CarSession(await HeadUnit.start(), log);
    await log.waitFor(/onLoadChildren car_root/, { timeout: 60_000 });
    await session.openLauncher();
    await session.unit.quit();
    await shell(`am force-stop ${config.packageName}`);
    await startFromKnownVideo();
    await session.reconnect();
    return session;
  }

  async openLauncher(attempts = 3) {
    const mark = this.log.mark();
    this.unit.key('home');
    const opened = await this.log.waitFor(/makeBackground for component ComponentInfo\{[^}]*ProjectionTrampolineFallbackService\}/, { from: mark, timeout: 5_000 })
      .catch(() => null);
    if (opened) return;
    if (attempts <= 1) throw new Error('the head unit never showed the app launcher');
    await this.openLauncher(attempts - 1);
  }

  async waitForForeground(accept, from, timeout = 15_000) {
    const line = await this.log.waitFor(FOREGROUND, { from, timeout });
    const component = line.match(FOREGROUND)[1];
    if (accept(component)) return component;
    return this.waitForForeground(accept, this.log.lines.indexOf(line) + 1, timeout);
  }

  async reconnect() {
    const mark = this.log.mark();
    this.unit = await HeadUnit.start();
    await this.log.waitFor(/onLoadChildren car_root/, { from: mark, timeout: 60_000 });
  }

  async disconnectAbruptly() {
    this.unit.kill();
    await this.unit.exited;
  }

  async pressPlay(attempts = 4) {
    const mark = this.log.mark();
    this.unit.key('media_play');
    const reached = await this.log.waitFor(/CarPlayer: command play/, { from: mark, timeout: 5_000 }).catch(() => null);
    if (reached) return mark;
    if (attempts <= 1) throw new Error('media_play from the head unit never reached the app');
    return this.pressPlay(attempts - 1);
  }

  async playFromCar() {
    const mark = await this.pressPlay();
    await this.waitForMedia((m) => m.found && !m.paused, { timeout: 60_000, what: 'playback after play from the car' });
    return mark;
  }

  async openCarScreen(attempts = 5) {
    await this.openLauncher();
    const mark = this.log.mark();
    this.unit.tap(LAUNCHER_FIESTA_ICON.x, LAUNCHER_FIESTA_ICON.y);
    const opened = await this.waitForForeground((component) => component === FIESTA_CAR_COMPONENT, mark, 5_000)
      .catch(() => null);
    if (!opened) {
      if (attempts <= 1) throw new Error('the Fiesta icon of the launcher never brought the car screen up');
      return this.openCarScreen(attempts - 1);
    }
    await this.log.waitFor(/CarPlayer: attach/, { from: mark, timeout: 30_000 });
    return mark;
  }

  async ask(wavPath) {
    const mark = this.log.mark();
    const everything = new Logcat([]);
    try {
      this.unit.send('mic begin');
      await everything.waitFor(/Sent microphone open request/, { timeout: 30_000 });
      this.unit.speak(wavPath);
    } finally {
      everything.stop();
    }
    return mark;
  }

  async page() {
    return pageSession();
  }

  async media() {
    const page = await this.page();
    try {
      return await page.media();
    } finally {
      page.close();
    }
  }

  waitForMedia(predicate, { timeout = 60_000, what = 'media state' } = {}) {
    return this.waitUntil(() => this.media(), (m) => m.found !== undefined && predicate(m), { timeout, what });
  }

  async waitUntil(read, predicate, { timeout = 60_000, what = 'condition' } = {}) {
    const deadline = Date.now() + timeout;
    let last;
    while (Date.now() < deadline) {
      last = await read().catch((error) => ({ error: error.message }));
      if (!last?.error && predicate(last)) return last;
      await sleep(1_000);
    }
    throw new Error(`${what} never happened, last seen ${JSON.stringify(last)}`);
  }

  async playedAtLeast(seconds) {
    const start = (await this.media()).time;
    return this.waitForMedia((m) => !m.paused && m.time >= start + seconds, {
      timeout: 120_000, what: `${seconds}s of playback`
    });
  }

  async playerInView() {
    const page = await this.page();
    try {
      return await page.playerInView();
    } finally {
      page.close();
    }
  }

  async carPrefs() {
    const xml = await readPrefs('car');
    return {
      videoTime: Number(prefValue(xml, 'video_time')),
      videoTimeUrl: prefValue(xml, 'video_time_url')?.replaceAll('&amp;', '&'),
      playbackInterrupted: prefValue(xml, 'playback_interrupted') === 'true'
    };
  }

  async sessionState() {
    const dump = await shell('dumpsys media_session');
    const block = dump.split(/\n(?=    \S)/).find((part) => part.includes(`package=${config.packageName}`)) ?? '';
    const state = block.match(/state=PlaybackState \{state=(\d+), position=(\d+).*?actions=(\d+), custom actions=\[(.*?)\], active item id=(-?\d+)/);
    const queue = block.match(/queueTitle=(.*), size=(\d+)/);
    return {
      state: state ? Number(state[1]) : null,
      positionMillis: state ? Number(state[2]) : null,
      actions: state ? Number(state[3]) : 0,
      customActions: state ? state[4] : '',
      activeItemId: state ? Number(state[5]) : null,
      active: /active=true/.test(block),
      description: block.match(/description=(.*)/)?.[1] ?? null,
      queueTitle: queue?.[1] ?? null,
      queueSize: queue ? Number(queue[2]) : 0
    };
  }

  async notificationShown() {
    const dump = await shell('dumpsys notification --noredact');
    return dump.includes(`pkg=${config.packageName}`);
  }

  linesSince(mark, pattern) {
    return this.log.since(mark).filter((line) => pattern.test(line));
  }

  async stop() {
    await this.openLauncher().catch(() => {});
    this.unit.key('media_pause');
    await this.waitForMedia((m) => !m.found || m.paused, { timeout: 10_000, what: 'pause before leaving' }).catch(() => {});
    await this.unit.quit();
    this.log.stop();
  }
}

async function startFromKnownVideo() {
  const url = config.videoUrl.replaceAll('&', '&amp;');
  const xml = [
    "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>",
    '<map>',
    `    <string name="home_url">${url}</string>`,
    '    <boolean name="playback_interrupted" value="false" />',
    '</map>'
  ].join('\n');
  const encoded = Buffer.from(xml).toString('base64');
  await shell(`run-as ${config.packageName} sh -c 'echo ${encoded} | base64 -d > shared_prefs/car.xml'`);
}

async function stopStrayHeadUnits() {
  await run('taskkill', ['/F', '/IM', 'desktop-head-unit.exe']).catch(() => {});
}

export const PlaybackStateCompat = { STOPPED: 1, PAUSED: 2, PLAYING: 3, BUFFERING: 6, CONNECTING: 8 };

export const MediaAction = {
  SKIP_TO_PREVIOUS: 16,
  SKIP_TO_NEXT: 32,
  SEEK_TO: 256,
  SKIP_TO_QUEUE_ITEM: 4096
};
