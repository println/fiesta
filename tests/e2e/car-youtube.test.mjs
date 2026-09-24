import test from 'node:test';
import assert from 'node:assert/strict';
import { setTimeout as sleep } from 'node:timers/promises';
import { CarSession } from './lib/car.mjs';
import { spokenWav } from './lib/voice.mjs';

const FOCUS_HELD_BY_THE_HEAD_UNIT_MILLIS = 5_000;

async function playing() {
  const car = await CarSession.start();
  await car.playFromCar();
  await car.waitForMedia((m) => m.found && !m.paused && m.time > 2, { what: 'playback' });
  return car;
}

test('CR-1: on the car screen the player stays in view below the top bar, also on the next video and after the page scrolls itself', async () => {
  const car = await playing();
  try {
    await car.openCarScreen();
    await car.waitUntil(() => car.playerInView(), (p) => p.found && p.visible, { what: 'the player in view on the car screen' });

    const page = await car.page();
    await page.scrollPageBy(300);
    page.close();
    await car.waitUntil(() => car.playerInView(), (p) => p.visible, { what: 'the player back in view after the page scrolled' });

    const before = await car.media();
    car.unit.key('media_next');
    await car.waitForMedia((m) => m.url !== before.url && !m.paused, { what: 'the next video' });
    await car.waitUntil(() => car.playerInView(), (p) => p.found && p.visible, { what: 'the player in view on the next video' });
  } finally {
    await car.stop();
  }
});

test('CR-2: the next button of the car goes to the next one', async () => {
  const car = await playing();
  try {
    const before = await car.media();
    car.unit.key('media_next');
    const next = await car.waitForMedia((m) => m.url !== before.url && !m.paused, { timeout: 30_000, what: 'the next video playing' });
    assert.notEqual(next.url, before.url);
  } finally {
    await car.stop();
  }
});

test('CR-2: the previous button of the car restarts the video, and pressed again goes back to the previous one', async () => {
  const car = await playing();
  try {
    const first = await car.media();
    car.unit.key('media_next');
    const second = await car.waitForMedia((m) => m.url !== first.url && !m.paused, { what: 'the next video' });
    await car.playedAtLeast(4);
    car.unit.key('media_previous');
    await car.waitForMedia((m) => m.url === second.url && m.time < 3, { what: 'the video restarting' });
    car.unit.key('media_previous');
    const back = await car.waitForMedia((m) => m.url !== second.url && !m.paused, { what: 'the previous video' });
    assert.equal(new URL(back.url).searchParams.get('v'), new URL(first.url).searchParams.get('v'));
  } finally {
    await car.stop();
  }
});

test('CR-2: asking the assistant opens the first video and plays it, without the car screen', async () => {
  const car = await CarSession.start();
  try {
    const wav = await spokenWav('play-legiao-urbana', 'Toque Legião Urbana no Fiesta');
    const mark = await car.ask(wav);
    await car.log.waitFor(/command playFromSearch/, { from: mark, timeout: 30_000 });
    await car.log.waitFor(/voiceSearch id=\d+ completed/, { from: mark, timeout: 60_000 });
    const media = await car.waitForMedia((m) => m.found && !m.paused, { what: 'the searched video playing' });
    const url = new URL(media.url);
    assert.equal(url.pathname, '/watch', `opened ${media.url}`);
    assert.deepEqual(car.linesSince(mark, /CarPlayer: attach/), [], 'the car screen stayed closed');
  } finally {
    await car.stop();
  }
});

test('CR-4: the video plays with sound', async () => {
  const car = await playing();
  try {
    const media = await car.media();
    assert.equal(new URL(media.url).pathname, '/watch');
    assert.equal(media.muted, false);
  } finally {
    await car.stop();
  }
});

for (const [story, focus] of [['CR-5', 'video'], ['CR-6', 'nav']]) {
  test(`${story}: the video keeps playing after the head unit takes the ${focus} focus`, async () => {
    const car = await playing();
    try {
      car.unit.focus(focus);
      await sleep(FOCUS_HELD_BY_THE_HEAD_UNIT_MILLIS);
      car.unit.focus(focus);
      const media = await car.waitForMedia((m) => !m.paused, { timeout: 15_000, what: 'playback after the focus came back' });
      assert.equal(media.paused, false);
    } finally {
      await car.stop();
    }
  });
}
