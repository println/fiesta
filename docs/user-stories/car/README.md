# Car user stories

The car side of the app, as the user stated it. Ids are `CR-<n>` and follow the order the
stories were given.

| Story | File |
| --- | --- |
| CR-1 Watch YouTube in the car | [YouTube in the car](01-youtube.md) |
| CR-2 Drive YouTube from the car's controls | [YouTube in the car](01-youtube.md) |
| CR-3 Skip ads on their own | [YouTube in the car](01-youtube.md) |
| CR-4 The video plays with audio | [YouTube in the car](01-youtube.md) |
| CR-5 The video does not stop for the side camera | [Playback while the car does something else](02-playback.md) |
| CR-6 The video keeps playing while I navigate in Waze | [Playback while the car does something else](02-playback.md) |
| CR-7 Zoom | [What the car screen can do](03-car-screen.md) |
| CR-8 Bookmark a page from the car | [What the car screen can do](03-car-screen.md) |
| CR-9 Browse the bookmarks on the car screen | [What the car screen can do](03-car-screen.md) |
| CR-10 Make a bookmark the home page in one tap | [What the car screen can do](03-car-screen.md) |
| CR-11 Show the bookmarks bar | [What the car screen can do](03-car-screen.md) |
| CR-12 Search by voice and by text, on Google and on YouTube | [What the car screen can do](03-car-screen.md) |
| CR-13 Choose where the toolbar sits | [What the car screen can do](03-car-screen.md) |
| CR-14 Hide the toolbar | [What the car screen can do](03-car-screen.md) |
| CR-15 The buttons on the car toolbar | [What the car screen can do](03-car-screen.md) |
| CR-16 Wire plain pages to the car's controls | [Extending what the car can do](04-extending.md) |
| CR-17 Have other search engines | [Extending what the car can do](04-extending.md) |
| CR-18 Reach any site from the car | [Extending what the car can do](04-extending.md) |

## Not met yet

- **CR-1** — there, with problems not yet pinned down one by one.
- **CR-2** — on shorts the plugin handles only the short presses, so there is no long-press
  seek there. A channel does get opened by voice search: nothing rules one out today.
- **CR-3** — ads are only skipped while tracker/ad blocking is on for the site.
- **CR-4** — unmuting only acts on `/watch`, so a short that starts muted stays muted.
- **CR-5 and CR-6** — not verified on the car.
- **CR-17** — the structure is there, the installer is not: an engine is already data, but it
  can only be read from the APK, and its name and icon are still bound to a known id.

CR-7 to CR-16 and CR-18 are all there and doing what the user wants.
