# User stories

Living specification of the behaviour this app must have, written as the user states it. Split
by environment: [phone](phone/), [car](car/) and [player](player/).

**This is the contract.** Breaking a scenario here is a regression, even if the build is green
and the tests pass. Read the relevant file before touching a flow; update it in the same commit
when the behaviour changes on purpose. Each story says whether it is met today.

Only what the user can observe belongs here. The "how" lives in `CLAUDE.md`, `docs/plugins.md`
and `docs/plans/`.
