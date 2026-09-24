# Address bar

## PH-4 — See the connection and the blocker at a glance

> **As a** phone user
> **I want** the address bar to tell me whether the site is http or https, and whether
> tracker/ad blocking is on for it
> **So that** I know where I am without opening anything.

**Today.** One shield icon in the bar carries it: crossed out when the address is not https,
a check when blocking is on for that host, a cross when it is off, and an exclamation mark when
the page failed to load. Tapping it opens the site panel with the host, the connection state
with an explanation, the blocking switch and how much was blocked on this page.

## PH-6 — See that the site is in desktop mode

> **As a** phone user
> **I want** an indicator in the address bar when the site is in desktop mode
> **So that** I know why the page looks the way it does.

> **Given** desktop mode is turned on for the site
> **Then** a desktop icon appears in the address bar;
> **and** the choice is saved per host and shared with the car.

**Not there yet: the icon.** Desktop mode itself works — it is a switch in the overflow menu,
saved per host, reloading the page when flipped, and the car reads the same setting. Only the
address bar indicator is missing.

## PH-7 — Bookmark the page and see that it is bookmarked

> **As a** phone user
> **I want** to bookmark the open page
> **So that** I can reach it again, in the phone and in the car.

> **Given** a page is open
> **When** it is bookmarked
> **Then** the bookmark is saved and shared with the car;
> **and** I can see that the page is bookmarked.

**Today.** The overflow menu carries it: the icon is filled when the page is already a
bookmark, and the same entry removes it.
