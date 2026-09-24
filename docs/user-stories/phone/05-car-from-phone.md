# Driving the car from the phone

## PH-2 — Open a web page in the car from a button

> **As a** phone user
> **I want** to send the page I am looking at to the car with one button
> **So that** I do not have to type it on the head unit.

> **Given** a page is open
> **When** the send-to-car button is pressed
> **Then** a confirmation dialog shows the URL, and confirming opens it in the car.

Any address goes; there are no restrictions on what can be sent.

**Today.** The button sits in the address bar and only appears with a page open. The app checks
first whether the car is up: if it is not, it says so instead of sending into the void.

## PH-8 — Set the car's options from the phone, live

> **As a** phone user
> **I want** to set the car's options from the phone and see them applied in the car right away
> **So that** I do not have to fiddle with settings while driving.

> **Given** the car screen is up
> **When** one of these is changed on the phone
> **Then** the car reflects it immediately:
>
> | Option | Values | Default |
> | --- | --- | --- |
> | Toolbar position | left, right, top, bottom | left |
> | Bookmarks bar | on, off | off |
> | Zoom | percentage | 100% |
> | Auto-hide the toolbar | on, off | off |

**Today.** It holds both ways: a change made on the car screen is reflected on the phone's
settings screen while it is open.

## PH-9 — Set the home page from the phone

> **As a** phone user
> **I want** to set the home page from the phone
> **So that** the car starts where I want it.

**Today.** It is in the car's settings screen on the phone, and starts at
`https://m.youtube.com`.

## PH-19 — Speak to the assistant on the phone and have the car obey

> **As a** phone user
> **I want** to speak to the assistant on the phone and have the request reach the car
> **So that** I can start something without touching the head unit.

> **Given** a request spoken to the assistant on the phone
> **Then** it behaves exactly as the same request spoken in the car does.

**Not there yet.** Wanted for the future.
