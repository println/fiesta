# Browsing

## PH-1 — Open the app the way Firefox Focus does

> **As a** phone user
> **I want** the app to open ready for me to type
> **So that** I can reach a page without any extra tap.

> **Given** the app is opened
> **Then** the address bar is empty and the keyboard is already up on it.

**Today, and it is what I want.** Typing completes inline against 16 built-in domains and the
hosts already visited, leaving the completed part selected so the next keystroke replaces it;
it never completes while the text is shrinking. Up to four suggestion rows show under the bar:
the first is what was typed, the rest come from the network. Text with a space, or with no `.`
and no `:` (except `localhost`), is treated as a search; anything else as an address.

## PH-3 — Erase the page from the bin button

> **As a** phone user
> **I want** to wipe the page I am looking at, the way Firefox Focus does
> **So that** nothing of that session is left behind.

> **Given** a page is open
> **Then** the bin button is the leftmost item of the toolbar;
> **when** it is pressed
> **Then** the page is erased and the app goes back to the empty address bar.

> **Given** no page is open
> **Then** the bin button is not shown.

**Today.** Erasing stops the load, drops the history and the saved form data, forgets the last
page and confirms with a message.

## PH-10 — Back, forward, share and reload

> **As a** phone user
> **I want** the same page actions Firefox Focus offers
> **So that** the browser behaves the way I already expect.

> **Given** a page is open
> **Then** back, forward, share and reload are available.

**Today.** They live in the overflow menu, which only shows them with a page open. Back and
forward are greyed out when there is nowhere to go. Share hands the URL to the phone's share
sheet.

## PH-12 — Open in another browser

> **As a** phone user
> **I want** to hand the current page to another browser on the phone
> **So that** I can keep reading where the app is not the right tool.

> **Given** a page is open
> **When** "open in" is chosen
> **Then** the phone's app chooser opens with the current URL.

## PH-18 — Browse without redirect errors

> **As a** phone user
> **I want** to reach pages the way any ordinary browser does
> **So that** redirects, http to https and the like just work, with no error in my way.

> **Given** any address
> **When** it is opened
> **Then** it loads like it would in an ordinary browser, without errors the browser itself caused.

**Not verified.** What exists today: an address with no scheme is tried on https first and falls
back to http only once, asking before it does and offering to stay on https; a failure that
looks transient is retried, waiting for the network when there is none, and only for a
navigation that never committed; anything else lands on the app's own error page, and going
back skips it. Whether that adds up to "no errors in my way" has not been checked against real
browsing.
