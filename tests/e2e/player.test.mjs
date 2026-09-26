import test from 'node:test';
import assert from 'node:assert/strict';
import { CarSession, MediaAction, PlaybackStateCompat } from './lib/car.mjs';
import { pageSession } from './lib/devtools.mjs';
import { config } from './lib/config.mjs';
import { shell } from './lib/adb.mjs';

test('PL-2 and PL-3: while a playlist plays, the player offers position, skipping, Fiesta and the queue', async () => {
  const car = await CarSession.start();
  try {
    await car.playFromCar();
    const media = await car.waitForMedia((m) => m.found && !m.paused && m.duration > 0, { what: 'playback' });
    const session = await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.PLAYING, {
      what: 'the session publishing playback'
    });

    assert.equal(session.state, PlaybackStateCompat.PLAYING);
    assert.ok(session.actions & MediaAction.SEEK_TO, 'control over the position');
    assert.ok(session.actions & MediaAction.SKIP_TO_NEXT, 'forward button');
    assert.ok(session.actions & MediaAction.SKIP_TO_PREVIOUS, 'back button');
    assert.match(session.customActions, /Abrir Fiesta/, 'button that opens Fiesta');
    assert.ok(session.description, 'what is playing is described');

    if (new URL(media.url).searchParams.has('list')) {
      const queued = await car.waitUntil(() => car.sessionState(), (state) => state.queueSize > 1, {
        what: 'the playlist in the queue'
      });
      assert.notEqual(queued.activeItemId, -1, 'the current track is marked in the queue');
    }
  } finally {
    await car.stop();
  }
});

async function navigateTo(url) {
  const page = await pageSession(/./);
  await page.evaluate(`location.href = ${JSON.stringify(url)}`);
  page.close();
}

async function playAndPauseMutedVideoOnThePage() {
  const page = await pageSession(/./);
  await page.evaluate(`(async () => {
    const canvas = document.createElement('canvas');
    canvas.getContext('2d').fillRect(0, 0, 10, 10);
    const video = document.createElement('video');
    video.muted = true;
    video.srcObject = canvas.captureStream();
    document.body.appendChild(video);
    await video.play();
    await new Promise((resolve) => setTimeout(resolve, 3000));
    video.pause();
    await new Promise((resolve) => setTimeout(resolve, 3000));
  })()`);
  page.close();
}

async function focusHeldByFiesta() {
  const dump = await shell('dumpsys audio');
  const stack = dump.slice(dump.indexOf('Audio Focus stack entries'), dump.indexOf('No external focus policy'));
  return stack.includes(config.packageName);
}

function noTitle(description) {
  return description === null || description === 'null';
}

async function assertLeftMediaForPlainPage(car, mark, what) {
  const stopped = await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.STOPPED, {
    what: `${what} stopping the session`
  });
  assert.ok(noTitle(stopped.description), `${what}: no title`);
  assert.equal(stopped.queueSize, 0, `${what}: no queue`);

  await car.log.waitFor(/CarPlayer: left media for a plain page/, { from: mark, timeout: 30_000 });
  assert.deepEqual(car.linesSince(mark, /focus is held by someone else|CarPlayer: paused$/), [], `${what}: no pause, no focus request`);
  assert.equal(await focusHeldByFiesta(), false, `${what}: Fiesta is not on the audio focus stack`);

  const prefs = await car.carPrefs();
  assert.equal(prefs.playbackInterrupted, false, `${what}: nothing to resume later`);
  return prefs;
}

test('PL-7: a plain page is stopped, asks for no focus, and does not resume on its own', async () => {
  const car = await CarSession.start();
  try {
    await car.playFromCar();
    await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.PLAYING, {
      what: 'the video playing before leaving it'
    });

    let mark = car.log.mark();
    await navigateTo('https://www.google.com');
    let prefs = await assertLeftMediaForPlainPage(car, mark, 'google.com');
    assert.ok(!prefs.recentlyPlayedUrls.some((url) => url.includes('google.com')), 'google.com is not in the history');

    mark = car.log.mark();
    await playAndPauseMutedVideoOnThePage();
    assert.deepEqual(car.linesSince(mark, /CarPlayer: (playing|paused)$|focus is held by someone else/), [], 'a muted video on a plain page is not playback');
    assert.equal((await car.sessionState()).state, PlaybackStateCompat.STOPPED, 'a muted video keeps the plain page stopped');
    assert.equal((await car.carPrefs()).playbackInterrupted, false, 'a muted video leaves nothing to resume');

    await navigateTo(config.videoUrl);
    await car.waitForMedia((m) => m.found && !m.paused, { what: 'playback resuming after going back to the video' });
    const playing = await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.PLAYING, {
      what: 'the session playing again'
    });
    assert.ok(!noTitle(playing.description), 'a page where media plays shows up with its title');

    mark = car.log.mark();
    await navigateTo('https://www.youtube.com/@ancap_su/videos');
    prefs = await assertLeftMediaForPlainPage(car, mark, 'a channel videos page');
    assert.ok(!prefs.recentlyPlayedUrls.some((url) => url.includes('/@ancap_su')), 'the channel page is not in the history');
  } finally {
    await car.stop();
  }
});
