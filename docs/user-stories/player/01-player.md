# The player

The player is not a feature that was asked for. It exists because a media session was the way
to have Android Auto accept the app at all, and the way the assistant reaches YouTube without
the car screen. These stories say what it must be, now that it is there.

## PL-1 — The player is like Spotify's

> **As a** driver
> **I want** the player to work the way Spotify's does
> **So that** I already know how to use it without learning anything.

Spotify is the reference for the whole player: the same screens, in the same places, doing the
same things.

## PL-2 — The now playing screen

> **As a** driver
> **I want** a now playing screen like Spotify's
> **So that** everything I need while a track plays is on one screen.

> **Given** something is playing
> **Then** the screen has:
>
> - a button that opens the queue screen (PL-3);
> - a button that opens Fiesta, the browser;
> - control over the position in the video or track;
> - an image;
> - forward and back buttons.

**There, and it does what I want.**

## PL-3 — The queue screen

> **As a** driver
> **I want** a queue screen
> **So that** I see what is playing and what is around it.

> **Given** the queue screen
> **Then** it shows what is playing and marks it as the current one.

The queue is whatever fits what is playing:

- a set of tracks that stand together for now, like a CD;
- a queue of tracks thrown in to play one after the other;
- the tracks of a playlist, when a playlist is what is playing.

On YouTube:

- for a video or a short, the previous, the current and the next one — when the video that is
  playing offers forward and back at all;
- for a playlist, the whole playlist that is running.

**There, and it does what I want.**

## PL-4 — The playlist screen

> **As a** driver
> **I want** a playlist screen
> **So that** I can see the playlist that is running.

> **Given** a playlist is playing
> **Then** the playlist screen shows it;
> **and** once it stops playing, the playlist is gone from there.

On YouTube, videos and shorts have no playlist, so nothing shows for them.

**There, and it does what I want.**

## PL-5 — History

> **As a** driver
> **I want** a history of what was played
> **So that** I can go back to something without searching for it again.

**There, and it does what I want.**

## PL-6 — The home screen

> **As a** driver
> **I want** a home screen
> **So that** the two things I reach for are the first ones I see.

> **Given** the home screen
> **Then** it shows the link to Fiesta, the browser,
> **and** what is playing now.

**There, and it does what I want.**

## PL-7 — The player only shows media

> **As a** driver
> **I want** the player to show only pages that have media
> **So that** the sites I just browse, like google.com, do not show up as if they were playing.

> **Given** a page with no media, like google.com
> **Then** the player shows nothing as playing: no title, no image, nothing on the home screen,
> nothing in the queue or in the history.

A page is media when media plays on it — by itself when I open it, or because I told it to play.
Having media that *could* play is not enough. A YouTube channel's videos page, like
`https://www.youtube.com/@ancap_su/videos`, is full of videos, but they only preview when
touched and nothing starts when I open it: it does not show up in the player.

> **Given** a page with media that does not start by itself
> **When** I open it
> **Then** it does not show up in the player;
> **and** once I make it play, it does.

> **Given** something is playing
> **When** I go to a page with no media
> **Then** it is as if I had pressed stop: the player stops, Fiesta does not ask for the audio
> focus, and it does not resume that playback on its own later.

**Not there yet.**

How the player, the WebView and the media server fit together is in
[the media server](02-media-server.md).
