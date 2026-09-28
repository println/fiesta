# YouTube in the car

## CR-1 — Watch YouTube in the car

> **As a** driver
> **I want** to open YouTube on the car screen
> **So that** I can watch and listen from the head unit.

**There, but with problems.**

## CR-2 — Drive YouTube from the car's controls

> **As a** driver
> **I want** to control YouTube with the car's own controls
> **So that** I never have to touch the screen while driving.

> **Given** a short, a video or a playlist is playing
> **Then** the steering wheel controls do this:
>
> | Control | Short press | Long press |
> | --- | --- | --- |
> | `>\|` | go to the next one | seek forward in the video |
> | `\|<` | go to the previous one | seek backward in the video |

> **Given** the driver asks the assistant for something
> **When** the request arrives
> **Then** the YouTube search screen opens with the spoken words,
> **and** the first video or playlist is opened,
> **and** a channel is never opened,
> **and** a sponsored video is never opened.

**There, but with problems.**

What exists today: the four wheel events reach the page through the plugin, and the assistant
opens the results page for the spoken words and picks the first non-sponsored result.

Two gaps already located:

- **on shorts only the short presses are handled**; there is no long-press seek there, while
  videos and playlists have all four;
- **a channel does get opened** — reported from the car. Nothing rules a channel out today: the
  choice is "the first non-sponsored result", and sponsored is the only thing excluded.

## CR-3 — Skip ads on their own

> **As a** driver
> **I want** ads to be skipped automatically
> **So that** I never have to touch the screen to get past one.

**There, but with problems.** Today ads are pruned from the page's own data, and when one still
shows it is jumped to its end or its skip button is pressed, twice a second. It only runs while
tracker/ad blocking is on for the site, because the plugin declares that requirement.

## CR-4 — The video plays with audio

> **As a** driver
> **I want** the video to play with sound
> **So that** I hear what I opened.

**There, but with problems.** The player is unmuted whenever it starts playing — but **only on
`/watch`**: the plugin's unmute bails out on any other path, so a short that starts muted stays
muted.
