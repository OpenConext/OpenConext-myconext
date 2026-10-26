# e2e tests

Playwright tests for my-gui. Controls are selected with `getByTestId` (`data-testid`, kebab-case `<page>-<element>`).

## Stack that must be running

```bash
docker compose up -d mongo mailpit        # from the repo root
# myconext-server (:8081) with --spring.profiles.active=test,e2e
#   test: every request is authenticated as the user with uid 1234567890
#   e2e: seeds that user (E2eSeed), exposes /e2e/reset and /e2e/sms-code (E2eController)
#         and uses the OIDC mock of the tiqr-mock as institution login
# tiqr-mock (:8088), plays the eduID app and the institution login (/oidc)
```

The my-gui dev server (:3003) is started by Playwright when it is not running.

```bash
cd ./my-gui
yarn e2e        # headless
yarn e2e:ui     # Playwright UI mode
```

Environment overrides: `E2E_BASE_URL`, `SERVER_URL`, `TIQR_MOCK_URL`, `MAILPIT_URL`, `MAILPIT_AUTH`.

## Helpers (`support/`)

- `tiqr.ts`: `completeEnrollment`, `completeAuthentication`, `deleteRegistration` call the tiqr-mock GET endpoints.
- `mailpit.ts`: `waitForCode` polls the Mailpit REST API for a mail to an address and returns the code.
- `sms.ts`: `expectSmsCode` reads the last SMS code from the e2e hook of the server.
- `app.ts`: `enrollApp` activates the app with a backup code or an SMS code.
- `fixtures.ts`: automatic `user` fixture, restores the seeded users through `/e2e/reset` before every test (the tests change and delete the user).
- `server.ts`: the server URL for the hooks that the dev server does not proxy.

All tests share one user, so they run serially (`workers: 1`).

The login domain (account-gui) is not part of the stack. Where a flow continues there (finishing the app enrollment, saving a password, deleting the account, registering a passkey) the tests stub the requests to `localhost:3000` and assert on them.
