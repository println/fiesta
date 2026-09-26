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

test('a page with no media is not a track', () => {
  const page = startPage({});
  page.fiesta.setAvailable({});
  const reading = page.lastReading();
  assert.equal(reading.isTrack, false);
  page.close();
});

test('a muted video playing is not a track', () => {
  const page = startPage({});
  const media = page.addMedia({});
  media.muted = true;
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, false);
  page.close();
});

test('an audible video playing is a track', () => {
  const { page, media } = playingPage();
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, true);
  page.close();
});

test('a track stays a track after it is paused, on the same URL', () => {
  const { page, media } = playingPage();
  readingAfterMediaEvent(page, media);
  media.paused = true;
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, true);
  page.close();
});

test('a track changing to another URL stays a track while the next one starts', () => {
  const { page, media } = playingPage();
  readingAfterMediaEvent(page, media);
  media.paused = true;
  page.navigateTo('https://m.youtube.com/watch?v=other');
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, true);
  page.close();
});

test('leaving a track for another URL where nothing plays is not a track once the change is over', () => {
  const { page, media } = playingPage();
  readingAfterMediaEvent(page, media);
  media.paused = true;
  page.navigateTo('https://m.youtube.com/@channel/videos');
  readingAfterMediaEvent(page, media);
  page.advanceClock(15000);
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, false);
  page.close();
});

test('the next track playing ends the change and stays a track', () => {
  const { page, media } = playingPage();
  readingAfterMediaEvent(page, media);
  page.navigateTo('https://m.youtube.com/watch?v=other');
  readingAfterMediaEvent(page, media);
  page.advanceClock(15000);
  const reading = readingAfterMediaEvent(page, media);
  assert.equal(reading.isTrack, true);
  page.close();
});

test('the site reporting playbackState playing is a track even without an element', () => {
  const page = startPage({});
  page.mediaSession.playbackState = 'playing';
  page.fiesta.setAvailable({});
  const reading = page.lastReading();
  assert.equal(reading.isTrack, true);
  page.close();
});

test('site metadata and action handlers with nothing actually playing is not a track', () => {
  const page = startPage({});
  page.setSiteMetadata({ title: 'A channel', artist: 'Someone', artwork: [] });
  page.mediaSession.setActionHandler('nexttrack', () => {});
  page.fiesta.setAvailable({});
  const reading = page.lastReading();
  assert.equal(reading.isTrack, false);
  page.close();
});

test('the generic plugin registering handlers with nothing playing is not a track', () => {
  const page = startPage({});
  page.fiesta.on('nextClick', () => {});
  page.fiesta.setAvailable({ next: true, previous: true });
  const reading = page.lastReading();
  assert.equal(reading.isTrack, false);
  page.close();
});

test('a channel videos page, full of muted previews, is not a track', () => {
  const page = startPage({});
  page.addMedia({ width: 200, height: 120, paused: true });
  page.addMedia({ width: 200, height: 120, paused: true });
  const mutedPreview = page.addMedia({ width: 200, height: 120 });
  mutedPreview.muted = true;
  const reading = readingAfterMediaEvent(page, mutedPreview);
  assert.equal(reading.isTrack, false);
  page.close();
});
