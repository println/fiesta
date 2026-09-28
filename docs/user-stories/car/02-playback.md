# Playback while the car does something else

## CR-5 — The video does not stop for the side camera

> **As a** driver
> **I want** the video to keep playing when I put the turn signal on and the head unit shows
> the side camera
> **So that** a turn does not cost me the playback.

**Not verified.**

## CR-6 — The video keeps playing while I navigate in Waze

> **As a** driver
> **I want** playback to survive me navigating in Waze
> **So that** the map and the audio work together.

**Not verified.**

For both: the WebView belongs to the player and not to the car screen, so nothing that merely
takes the screen away stops the audio. Audio focus is the open question — the app deliberately
does not hold it, Chromium asks for it inside the WebView and pauses when it loses it, so
anything on the head unit that takes audio focus can pause the video. That is what to check
first, on the car, one variable at a time.
