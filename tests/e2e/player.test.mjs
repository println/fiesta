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

async function focusHeldByFiesta() {
  const dump = await shell('dumpsys audio');
  const stack = dump.slice(dump.indexOf('Audio Focus stack entries'), dump.indexOf('No external focus policy'));
  return stack.includes(config.packageName);
}

function noTitle(description) {
  return description === null || description === 'null';
}

test('PL-7: a plain page is stopped, asks for no focus, and does not resume on its own', async () => {
  const car = await CarSession.start();
  try {
    await car.playFromCar();
    await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.PLAYING, {
      what: 'the video playing before leaving it'
    });

    const mark = car.log.mark();
    await navigateTo('https://www.google.com');
    const stopped = await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.STOPPED, {
      what: 'the session stopping on a plain page'
    });
    assert.ok(noTitle(stopped.description), 'no title for a plain page');
    assert.equal(stopped.queueSize, 0, 'no queue for a plain page');

    await car.log.waitFor(/CarPlayer: left media for a plain page/, { from: mark, timeout: 20_000 });
    assert.deepEqual(car.linesSince(mark, /focus is held by someone else/), [], 'never asks for the audio focus');
    assert.equal(await focusHeldByFiesta(), false, 'Fiesta is not on the audio focus stack');

    const prefs = await car.carPrefs();
    assert.equal(prefs.playbackInterrupted, false, 'a plain page does not resume playback later');

    await navigateTo(config.videoUrl);
    await car.waitForMedia((m) => m.found && !m.paused, { what: 'playback resuming after going back to the video' });
    await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.PLAYING, {
      what: 'the session playing again with the video metadata'
    });

    await navigateTo('https://www.youtube.com/@ancap_su/videos');
    const channelPage = await car.waitUntil(() => car.sessionState(), (state) => state.state === PlaybackStateCompat.STOPPED, {
      what: 'a channel videos page, with nothing playing, is stopped'
    });
    assert.ok(noTitle(channelPage.description), 'no title for a page whose media never starts by itself');
  } finally {
    await car.stop();
  }
});
