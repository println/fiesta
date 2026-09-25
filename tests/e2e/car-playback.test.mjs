import test from 'node:test';
import assert from 'node:assert/strict';
import { shell } from './lib/adb.mjs';
import { config } from './lib/config.mjs';
import { CarSession } from './lib/car.mjs';

const SECONDS_PLAYED_BEFORE_ACTING = 8;
const TRUSTED_POSITION_STEP_SECONDS = 3;

async function playingInBackground() {
  const car = await CarSession.start();
  await car.playFromCar();
  const media = await car.waitForMedia((m) => m.found && !m.paused && m.time > 2, { what: 'playback in the background' });
  assert.equal(media.screen, '320x180', 'without the car screen the page lives in the background window');
  return car;
}

test('opening the car screen while music plays in the background keeps playing from the same position', async () => {
  const car = await playingInBackground();
  try {
    await car.playedAtLeast(SECONDS_PLAYED_BEFORE_ACTING);
    const before = await car.media();
    const mark = await car.openCarScreen();
    const after = await car.waitForMedia((m) => m.screen !== '320x180' && !m.paused && m.time > before.time + 2, {
      timeout: 60_000, what: `playback on the car screen past ${before.time}s`
    });
    assert.equal(after.paused, false, 'still playing after the screen opened');
    assert.deepEqual(car.linesSince(mark, /CarPlayer: saving .* at 0s|CarPlayer: paused$/), []);
  } finally {
    await car.stop();
  }
});

test('opening the car screen while the page is still loading plays once it loads', async () => {
  const car = await CarSession.start();
  try {
    await car.pressPlay();
    await car.openCarScreen();
    const media = await car.waitForMedia((m) => m.found && !m.paused, { timeout: 40_000, what: 'playback on the car screen' });
    assert.notEqual(media.screen, '320x180', 'the page is on the car screen');
  } finally {
    await car.stop();
  }
});

test('the end of a track starts the next one without publishing a pause', async () => {
  const car = await playingInBackground();
  try {
    const page = await car.page();
    const before = await page.media();
    const mark = car.log.mark();
    await page.seekToEnd(5);
    page.close();
    const next = await car.waitForMedia(
      (m) => m.url !== before.url && !m.paused && m.time > 1,
      { timeout: 60_000, what: 'the next track playing' }
    );
    assert.ok(next.url !== before.url);
    assert.deepEqual(car.linesSince(mark, /destroying in|CarPlayer: paused$/), []);
    const saved = car.linesSince(mark, /CarPlayer: saving .* at \d+s/);
    saved.forEach((line) => {
      const [, url, seconds] = line.match(/saving (\S+) at (\d+)s/);
      assert.ok(url !== before.url || Number(seconds) >= before.time - 1, `saved ${seconds}s for ${url}`);
    });
  } finally {
    await car.stop();
  }
});

test('pausing from the car pauses, saves the position and, without a screen, schedules the destruction', async () => {
  const car = await playingInBackground();
  try {
    await car.playedAtLeast(SECONDS_PLAYED_BEFORE_ACTING);
    const mark = car.log.mark();
    car.unit.key('media_pause');
    await car.log.waitFor(/CarPlayer: paused$/, { from: mark, timeout: 10_000 });
    await car.log.waitFor(/destroying in 60s/, { from: mark, timeout: 5_000 });
    const media = await car.media();
    assert.equal(media.paused, true);
    const prefs = await car.carPrefs();
    assert.ok(Math.abs(prefs.videoTime - media.time) <= TRUSTED_POSITION_STEP_SECONDS, `saved ${prefs.videoTime}s, page at ${media.time}s`);
    assert.equal(prefs.playbackInterrupted, false);
  } finally {
    await car.stop();
  }
});

test('an app killed while playing comes back from the saved position', async () => {
  const car = await playingInBackground();
  try {
    const prefs = await car.waitUntil(() => car.carPrefs(), (p) => p.videoTime > 0, { what: 'a position saved while playing' });
    await shell(`am force-stop ${config.packageName}`);
    await car.log.waitFor(/onLoadChildren car_root/, { from: car.log.mark(), timeout: 60_000 });
    const mark = await car.playFromCar();
    const restored = await car.log.waitFor(/video discovered at .*, restoring \d+/, { from: mark, timeout: 30_000 });
    assert.match(restored, new RegExp(`restoring ${prefs.videoTime}$`));
    const media = await car.waitForMedia((m) => !m.paused && m.time >= prefs.videoTime, { what: 'playback from the saved position' });
    assert.ok(media.time >= prefs.videoTime);
  } finally {
    await car.stop();
  }
});

test('losing the car stops the music, and reconnecting plays it again from where it was', async () => {
  const car = await playingInBackground();
  try {
    await car.playedAtLeast(SECONDS_PLAYED_BEFORE_ACTING);
    const before = await car.media();
    const mark = car.log.mark();
    await car.disconnectAbruptly();
    await car.log.waitFor(/car disconnected/, { from: mark, timeout: 5_000 });
    await car.log.waitFor(/WebView destroyed/, { from: mark, timeout: 5_000 });
    const prefs = await car.carPrefs();
    assert.equal(prefs.playbackInterrupted, true, 'marked for resuming');
    assert.ok(prefs.videoTime >= before.time - TRUSTED_POSITION_STEP_SECONDS, `saved ${prefs.videoTime}s, was at ${before.time}s`);
    assert.equal((await car.sessionState()).active, false, 'no media session on the phone without the car');
    assert.equal(await car.notificationShown(), false, 'no playback notification on the phone without the car');

    const pressed = car.log.mark();
    await shell('cmd media_session dispatch play');
    await car.log.waitFor(/ignored outside the car/, { from: pressed, timeout: 5_000 });
    assert.deepEqual(car.linesSince(pressed, /CarPlayer: command play/), [], 'a play key on the phone does not play without the car');

    const reconnected = car.log.mark();
    await car.reconnect();
    const resumed = await car.waitForMedia((m) => m.found && !m.paused && m.time >= prefs.videoTime, {
      timeout: 60_000, what: `playback from the saved ${prefs.videoTime}s after reconnecting`
    });
    assert.equal(resumed.screen, '320x180', 'resumed in the background');
    assert.deepEqual(car.linesSince(reconnected, /CarPlayer: attach|Requested the car screen/), [], 'the car screen was never opened');
  } finally {
    await car.stop();
  }
});
