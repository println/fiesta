# Privacy

## PH-5 — Tracker/ad blocking per site

> **As a** phone user
> **I want** to turn tracker/ad blocking on for any site on its own
> **So that** I block where it helps and allow where the site breaks.

> **Given** a page is open
> **When** blocking is turned on for that site
> **Then** the choice is saved and shared with the car;
> **and** going back to the same site offers the same switch, so it can be turned off there.

**Today.** The switch is in the site panel behind the shield. It takes effect on the page at
once, and the panel counts what was blocked since the page started loading. Blocking is on by
default; the per-host choice overrides it either way, and the car reads the same value.

## PH-14 — Manage site data

> **As a** phone user
> **I want** a screen that manages what sites stored on the device
> **So that** I can see and remove it.

**Today.** The screen is reachable from the site panel and from Settings → privacy. It lists
every host, with how many cookies it holds and whether it kept web storage, ordered by how much
it stored; it can be filtered by host, and it deletes one site or all of them, each behind a
confirmation.

## PH-15 — Manage the privacy options

> **As a** phone user
> **I want** one place that holds the privacy options
> **So that** I decide what the browser is allowed to do.

> **Given** the privacy settings
> **Then** these can be turned on and off, all on by default:
>
> | Option | Default |
> | --- | --- |
> | Allow cookies | on |
> | Block trackers and ads | on |
> | Search suggestions | on |
>
> **and** the site data screen (PH-14) is reached from there.

Blocking trackers and ads is a **master switch**. Turning it off lets everything through,
without changing the per-site choices already made (PH-5): those keep their values, are frozen
while the master switch is off, and come back as they were when it is turned on again.

**Today.** The three switches are there, all default to on, and the site data screen is reached
from the same place. The cookies switch is about third-party cookies.

**Not met yet: the master switch.** Today the global switch is only the default for sites that
were never touched — a site explicitly set to block keeps blocking with the global off, and a
site explicitly set to allow keeps allowing with it on. Nothing is frozen either: the per-site
switch stays editable in the site panel.
