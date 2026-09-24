import test from 'node:test';
import assert from 'node:assert/strict';
import { startPage } from './harness.mjs';

function playingPage() {
  const page = startPage({});
  const media = page.addMedia({ currentTime: 12, duration: 200 });
  return { page, media };
}

function readingAfterMediaEvent(page, media) {
  page.fireMediaEvent(media, 'playing');
  return page.lastReading();
}

test('with neither plugin nor site, skipping is not offered', () => {
  const { page, media } = playingPage();
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.canSkipNext, false);
  assert.equal(reading.canSkipPrevious, false);
  page.close();
});

test('the site registering nexttrack is enough to offer skipping', () => {
  const { page, media } = playingPage();
  page.mediaSession.setActionHandler('nexttrack', () => {});
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.canSkipNext, true);
  assert.equal(reading.canSkipPrevious, false);
  page.close();
});

test('the plugin wins over the site, key by key', () => {
  const { page, media } = playingPage();
  page.mediaSession.setActionHandler('nexttrack', () => {});
  page.mediaSession.setActionHandler('previoustrack', () => {});
  page.fiesta.setAvailable({ next: false });
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.canSkipNext, false, 'the plugin said there is no next track');
  assert.equal(reading.canSkipPrevious, true, 'the plugin said nothing about previous');
  page.close();
});

test('an omitted key is not an answer of false', () => {
  const { page, media } = playingPage();
  page.fiesta.setAvailable({ next: true });
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.canSkipNext, true);
  assert.equal(reading.canSkipPrevious, false, 'nobody claimed a previous track');
  page.close();
});

test('metadata resolves field by field, plugin then site then page', () => {
  const { page, media } = playingPage();
  page.setSiteMetadata({ title: 'site title', artist: 'site artist', artwork: [{ src: 'site.jpg', sizes: '96x96' }] });
  page.fiesta.setMetadataProvider(() => ({ title: 'plugin title' }));
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.title, 'plugin title');
  assert.equal(reading.artist, 'site artist');
  assert.equal(reading.artworkUrl, 'site.jpg');
  page.close();
});

test('with no plugin and no site metadata, the page title and host answer', () => {
  const { page, media } = playingPage();
  page.document.title = 'a page';
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.title, 'a page');
  assert.equal(reading.artist, 'm.youtube.com');
  page.close();
});

test('seekTo reaches the handler the site registered, in seconds', () => {
  const { page } = playingPage();
  const seeks = [];
  page.mediaSession.setActionHandler('seekto', (details) => seeks.push(details));
  page.fiesta.dispatch('seekTo', '42');
  assert.equal(seeks.length, 1);
  assert.equal(seeks[0].action, 'seekto');
  assert.equal(seeks[0].seekTime, 42);
  page.close();
});

test('a plugin handler shadows the one the site registered', () => {
  const { page } = playingPage();
  let siteCalls = 0;
  let pluginCalls = 0;
  page.mediaSession.setActionHandler('nexttrack', () => { siteCalls += 1; });
  page.fiesta.on('nextClick', () => { pluginCalls += 1; });
  page.fiesta.dispatch('nextClick');
  assert.equal(pluginCalls, 1);
  assert.equal(siteCalls, 0);
  page.close();
});

test('the reading is published only when it changes', () => {
  const { page, media } = playingPage();
  page.fireMediaEvent(media, 'playing');
  const afterFirst = page.readings.length;
  page.fireMediaEvent(media, 'playing');
  assert.equal(page.readings.length, afterFirst, 'the same reading is not republished');
  media.currentTime = 30;
  page.fireMediaEvent(media, 'seeked');
  assert.equal(page.readings.length, afterFirst + 1);
  page.close();
});

test('a page with no media at all still reads as not playing', () => {
  const page = startPage({});
  page.fiesta.setAvailable({ next: true });
  const reading = page.lastReading();
  assert.equal(reading.hasMedia, false);
  assert.equal(reading.playing, false);
  page.close();
});
