# The media server

The whole integration goes through the media server. It is here, next to the player, because
it is the piece that makes the player work at all — not an area of its own.

## MS-1 — One piece integrates the others

> **I want** one piece in the middle that translates commands and events both ways
> **So that** whatever plays the media and whatever drives it never have to know each other.

## MS-2 — The renderer is whatever plays the media

> **I want** the thing that actually plays the media to be a renderer of the server
> **So that** what plays can change without anything else changing.

The WebView is a renderer. There is only one renderer today.

## MS-3 — Clients are whatever drives the media

> **I want** the things that drive the media to be clients of the server
> **So that** more than one of them can exist at the same time.

The player is a client. There can be many.

## MS-4 — The state lives in the server

> **I want** the state to be the server's
> **So that** one place decides what happens when everything starts again.

> **Given** the server starts and it has state, meaning playback was interrupted
> **Then** it tells the renderer to play, as soon as the renderer is up.

> **Given** the server starts and it has no state, meaning playback was not interrupted
> **Then** it tells the renderer nothing.

**Not met yet.** The decision to play again is taken on the renderer's side today, not by the
server.

## MS-5 — A renderer is also driven from inside

> **I want** what changes inside a renderer to reach the server
> **So that** the server stays the one place that knows the state.

> **Given** the media is changed from inside the renderer, by its own controls
> **Then** the server learns of it exactly as it would from a client's command.

**There.**

## MS-6 — The pieces talk through the server's contract

> **I want** the server to have a contract, and the pieces to talk only through it
> **So that** a renderer or a client can be replaced without touching anything else.

The contract is what a renderer must offer, what a client may ask, and the commands and events
that travel between them. Nothing crosses the server any other way.

## MS-7 — The media server is a library of its own

> **I want** the media server to be a library
> **So that** it can be used outside this app.

It knows nothing about this app: what it needs from the outside comes in through its own
contracts.
