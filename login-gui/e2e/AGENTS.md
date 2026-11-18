# login-gui e2e: rules for new tests

Playwright tests against the real server (`--spring.profiles.active=test,e2e`), mongo and mailpit. Canonical examples: `create-user.spec.ts`, `login-email-code.spec.ts`, `login-password.spec.ts`. (`home.spec.ts` and `language-toggle.spec.ts` are simple page checks, not the pattern for flows.)

**Never start, stop or restart the server (or mongo, mailpit) yourself.** If a change needs a server restart (e.g. `users.json`, any `myconext-server` change), tell the user a restart is needed.

## Run
`yarn e2e` in `login-gui` (one spec: `yarn e2e e2e/<file>.spec.ts`, UI: `yarn e2e:ui`). It only starts the Vite dev server (`:3004`). The stack must already run: server on `:8081`, mongo, mailpit. Overrides: `E2E_BASE_URL`, `E2E_SERVER_URL`. If the stack is down, ask the user.

## Structure
- One spec per flow, named `<flow>.spec.ts`, with `test.describe('<flow>')` using the same name.
- Constants go in `constants.ts`, API helpers in `utils/`. Always ask before changing anything in `shared-gui` or `my-gui`.
- No shared "common steps" layer. Specs read top to bottom, some repetition is fine.

## Helpers
| Helper | From | Use |
|---|---|---|
| `createAuthenticationRequestId(request)` | `utils/authnRequest` | Start a SAML request, returns the id for `/login/:id` or `/request/:id` |
| `addBrowserSessionCookie(context)` | `utils/authnRequest` | Needed for password or passkey logins (see Test data) |
| `waitForCode(request, email, { since? })` | `@myconext/shared-gui/e2e` | One-time code from mailpit. `since` = epoch ms, use it if the address got mail in earlier runs |
| `resetSeededUsers(request)` / `deleteUserByEmail(request, email)` | `utils/e2eServer` | Restore seeded users / remove a user the test created |
| `findEduIdByEmail(request, email)` | `utils/remoteCreation` | Check a user exists (`/sp/me` always returns the seeded `test` user, don't use it) |
| `getTranslation(key, lang)` | `utils/translations` | Expected copy from the locale files |

## Steps
- Every phase is a `test.step('<what the user does or sees>', ...)`. No loose actions or assertions.
- Reuse step names where the phase is the same: `create authentication request`, `fill in the email address`, `submit the form`, `read one-time code from mailpit`, `enter the code`.
- A step returns what later steps need (`const id = await test.step(...)`).
- A step that moves to another page asserts the new URL, e.g. `toHaveURL(new RegExp(`/code/${id}`))`.
- Don't assert the same fact twice in a row.

## Locators
- Always `getByTestId`. Never use HTML ids or class names. If an element has no `data-testid`, add one in `login-gui/src` on the clicked, filled or asserted element (kebab-case, prefixed with the route or component, e.g. `login-submit`, `nudge-app-skip`). No other app change.
- Wrapped inputs need a pass-through prop (`OtpInput` has `testId`). Ask before changing `shared-gui`.
- `getByRole` or text only when a test id is truly unreachable (content from the server or the ACS stub, e.g. `getByText('ACS stub')`).
- Never match translated copy, use `getTranslation`.

## Assertions
- Web-first assertions only, no `waitForTimeout`.
- Assert outcomes: URL, visible test ids, content.
- A flow that ends in SAML stubs `ACS_URL_PATTERN` (from `constants.ts`) and asserts a `POST` with `SAMLResponse=`, see `login-email-code.spec.ts`. Never call the real ACS host.

## Test data
- Users live in `myconext-server/src/main/resources/e2e/users.json` (seeded on server start): `e2e-email-code` (code only), `e2e-password` (password), `e2e-app-nudge` (app nudge), `e2e-other` (linked account). Reuse one when it fits and the test doesn't change it. Add a new one only when the scenario needs a different state: uid `e2e-<scenario>`, email `e2e-<scenario>@example.com`. Never use `jdoe@example.com` (my-gui user).
- Tests that create users use a unique email and call `deleteUserByEmail` in `afterEach`.
- Tests that change or consume seeded state call `resetSeededUsers` in `beforeEach`.
- Password or passkey logins need `addBrowserSessionCookie(page.context())` before `page.goto`, otherwise the server ends on `/success`. The one-time-code flow doesn't.
- Tests must not depend on each other.

## Comments
Only explain odd situations (the why). Never describe what the next line does. No `console.log`, no commented-out code.

## Done when
The test passes twice in a row and every element it uses has a `data-testid` (or a justified exception above).
