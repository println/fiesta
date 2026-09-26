# Settings

## PH-16 — Legacy CarStream hack for unlocked phones

> **As a** phone user with a rooted phone
> **I want** the legacy CarStream patch
> **So that** the app is accepted by Android Auto on an unlocked device.

**Today, and it is what I want.** The option applies the patch and requires root; with no root
it says so and does nothing.

## PH-17 — See the about screen

> **As a** phone user
> **I want** an about screen
> **So that** I know what the app is and where to follow it.

> **Given** the about screen
> **Then** it shows the logo, the name and the tagline, what the app is, the version with its codename, a link to
> the GitHub project, the license and the third-party credits.

**Today, and it is what I want.** Donating is its own entry in Settings, see PH-20.

## PH-20 — Donate from Settings

> **As a** phone user who likes the app
> **I want** a donate screen in Settings
> **So that** I understand why the project asks for support and can give it.

> **Given** the Settings screen
> **Then** there is a donate entry
> **When** I open it
> **Then** a screen explains why donating matters
> **When** I tap its button
> **Then** the donate page opens in my phone's default browser, not inside Fiesta,
> **and** it lists the donation links, each one opening its page.

> **Given** my phone is in Portuguese
> **When** I tap the button
> **Then** the Portuguese donate page opens.

> **Given** my phone is in any other language, like English or Spanish
> **When** I tap the button
> **Then** the English donate page opens.

**Partly there.** The button opens the donate page in the default browser, in Portuguese or in English; the page is not published on the site yet.

## PH-21 — Know when a new version is out

> **As a** phone user
> **I want** the app to tell me when a new version is out
> **So that** I don't have to check GitHub to stay up to date.

> **Given** a newer release is published on GitHub
> **When** I open the app on the phone
> **Then** a dialog says which version is out, with "Download", "Cancel" and a "Don't show again" box
> **When** I tap "Download"
> **Then** the new APK downloads through my phone's browser
> **and** the app itself never installs anything.

> **When** I tap "Cancel"
> **Then** the dialog closes, and it shows again the next time I open the app.

> **Given** I tick "Don't show again" in that dialog
> **When** I open the app again
> **Then** no dialog shows and the app does not contact GitHub.

> **Given** Settings → General
> **Then** "Check for new versions" is there, unticked after "Don't show again"
> **When** I tick it
> **Then** the dialog comes back when a newer release is out.

> **Given** I am in the car
> **Then** nothing about new versions shows on the car screen.

**Today, and it is what I want.**
