import test from 'node:test';
import assert from 'node:assert/strict';
import { CarSession, MediaAction, PlaybackStateCompat } from './lib/car.mjs';

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
