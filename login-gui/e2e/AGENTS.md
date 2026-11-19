# login-gui e2e: rules for new tests

Playwright tests against the real server (`--spring.profiles.active=test,e2e`), mongo and mailpit. Canonical examples: `create-user.spec.ts`, `login-email-code.spec.ts`, `login-password.spec.ts`. (`home.spec.ts` and `language-toggle.spec.ts` are simple page checks, not the pattern for flows.)

**Never start, stop or restart the server (or mongo, mailpit) yourself.** If a change needs a server restart (e.g. `users.json`, any `myconext-server` change), tell the user a restart is needed.

## Run
`yarn e2e` in `login-gui` (one spec: `yarn e2e e2e/<file>.spec.ts`, UI: `yarn e2e:ui`). It only starts the Vite dev server (`:3004`). The stack must already run: server on `:8081`, mongo, mailpit. Overrides: `E2E_BASE_URL`, `E2E_SERVER_URL`. If the stack is down, ask the user.

## Structure
- One spec per flow, named `<flow>.spec.ts`, with `test.describe('<flow>')` using the same name.
- Constants go in `constants.ts`, API helpers in `utils/`. Always ask before changing anything in `shared-gui` or `my-gui`.
- No shared "common steps" layer. Specs read top to bottom, some repetition is fine.
- Variants of one flow that only diverge at the end are one spec with an inline `(['a', 'b'] as const).forEach((variant) => ...)` generating a test each, with an `if` on one step (see `login-app-enrol.spec.ts`). Don't copy the shared steps.

## Helpers
| Helper | From | Use |
|---|---|---|
| `createAuthenticationRequestId(request)` | `utils/authnRequest` | Start a SAML request, returns the id for `/login/:id` or `/request/:id` |
| `createAuthenticationRequest(request, { acrValues })` | `utils/authnRequest` | Like the above, with requested ACRs. Returns `{ id, path }`: `page.goto(path)`, the query (`stepup`, `mfa`) steers the GUI |
| `enrollAppForUser(request, email)` | `utils/tiqrApp` | Gives a seeded user a working eduID app without a browser, returns the device |
| `authenticateWithApp(request, device, url)` | `utils/tiqrApp` | Plays the app answering a login QR (`url` from the `/tiqr/start-authentication` response) |
| `readSamlResponse(acsRequest)` | `utils/samlResponse` | Status codes and AuthnContextClassRef of the response posted to the ACS stub |
| `addBrowserSessionCookie(context)` | `utils/authnRequest` | Needed for password or passkey logins (see Test data) |
| `waitForCode(request, email, { since? })` | `@myconext/shared-gui/e2e` | One-time code from mailpit. `since` = epoch ms, use it if the address got mail in earlier runs |
| `deleteUserByEmail(request, email)` | `utils/e2eServer` | Remove a user the test created. `resetSeededUsers(request)` also lives here, but the global setup already calls it, don't use it in specs |
| `enrollAppDevice(request, enrollmentKey)` | `utils/tiqrApp` | Plays the eduID app: registers a device for an enrollment, so the page moves on to `/recovery` |
| `expectSmsCode(request)` | `utils/smsCode` | Call before the action that sends the SMS, the returned function waits for the new code from the mocked SMS service |
| `findEduIdByEmail(request, email)` | `utils/remoteCreation` | Check a user exists (`/sp/me` always returns the seeded `test` user, don't use it) |
| `getTranslation(key, lang, params)` | `utils/translations` | Expected copy from the locale files, `params` fills `{{serviceName}}` |
| `fetchServiceName(request, id)` | `utils/serviceName` | The service name the server shows for an authentication request |

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
- Users live in `myconext-server/src/main/resources/e2e/users.json` (seeded on server start): `e2e-email-code` (code only), `e2e-password` (password), `e2e-app-nudge` (app nudge), `e2e-app-enrol-backup-code` and `e2e-app-enrol-phone` (app enrolment), `e2e-passkey-login` and `e2e-passkey-register` (seeded passkey registration token, the spec registers a virtual passkey), `e2e-other` (linked account), `e2e-acr-<acr>-<satisfied|missing>` (plain ACR), `e2e-acr-mfa-<acr|profile>-<satisfied|missing>` (ACR with the app, the spec enrols the app) and `e2e-acr-mfa-no-app-<refuse|download>`. Linked accounts of ACR users are created in 2099, because those not validating names expire 180 days after `createdAt`. Never use `jdoe@example.com` (my-gui user).
- One seeded user per scenario (uid `e2e-<scenario>`, email `e2e-<scenario>@example.com`), never shared between specs or variants of a loop, so parallel runs can't conflict. Adding one needs a server restart.
- `e2e/global-setup.ts` resets the seeded users once per `playwright test` run. Specs don't reset, so a test must not need its user in a fresh state more than once per run (hence one user per scenario).
- Tests that create users use a unique email and call `deleteUserByEmail` in `afterEach`.
- Password or passkey logins need `addBrowserSessionCookie(page.context())` before `page.goto`, otherwise the server ends on `/success`. The one-time-code flow doesn't.
- Tests must not depend on each other.

## Comments
Only explain odd situations (the why). Never describe what the next line does. No `console.log`, no commented-out code.

## Done when
The test passes twice in a row and every element it uses has a `data-testid` (or a justified exception above).
