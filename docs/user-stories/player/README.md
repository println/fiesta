# Player user stories

The media player the app publishes to Android Auto, listed as **Fiesta Media**, and the media
server everything goes through. Ids are `PL-<n>` for the player and `MS-<n>` for the server.

| Story | File |
| --- | --- |
| PL-1 The player is like Spotify's | [The player](01-player.md) |
| PL-2 The now playing screen | [The player](01-player.md) |
| PL-3 The queue screen | [The player](01-player.md) |
| PL-4 The playlist screen | [The player](01-player.md) |
| PL-5 History | [The player](01-player.md) |
| PL-6 The home screen | [The player](01-player.md) |
| PL-7 The player only shows media | [The player](01-player.md) |
| MS-1 One piece integrates the others | [The media server](02-media-server.md) |
| MS-2 The renderer is whatever plays the media | [The media server](02-media-server.md) |
| MS-3 Clients are whatever drives the media | [The media server](02-media-server.md) |
| MS-4 The state lives in the server | [The media server](02-media-server.md) |
| MS-5 A renderer is also driven from inside | [The media server](02-media-server.md) |
| MS-6 The pieces talk through the server's contract | [The media server](02-media-server.md) |
| MS-7 The media server is a library of its own | [The media server](02-media-server.md) |

PL-1 to PL-7 are all there and doing what the user wants.

## Not met yet

- **MS-4** — on start, the decision to play again is taken on the renderer's side, not by the
  server.
