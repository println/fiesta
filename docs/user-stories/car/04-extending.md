# Extending what the car can do

## CR-16 — Wire plain pages to the car's controls

> **As a** driver
> **I want** an ordinary web page to answer the car's controls
> **So that** a site that is not YouTube is usable while driving.

> **Given** a plugin for the site
> **Then** the car's controls reach the page through it.

**There, and it works.** This is what the plugin engine is for: the page's address picks the
plugin, and the plugin says what the wheel's events do there.

## CR-17 — Have other search engines

> **As a** driver
> **I want** to add search engines beyond the ones that ship
> **So that** I search where I want.

**The structure is there; the installer is not.** An engine is already a file and not code — an
OpenSearch description plus an entry in the lookup order — which is why `google` and `youtube`
are data today. What is missing is a way to put one there from outside: engines are read from
the APK only, never from the device, unlike plugins. Its name and icon are still bound to the
id inside the app, so an engine the app does not know is refused when loaded; that has to give
way too.

## CR-18 — Reach any site from the car

> **As a** driver
> **I want** to open any site from the car
> **So that** nothing is out of reach from the driver's seat.

> **Given** the car's search screen
> **Then** a full address opens that address,
> **and** a keyword searches it on the engine of my choice,
> **and** either can be given by typing or by voice.

**There.**
