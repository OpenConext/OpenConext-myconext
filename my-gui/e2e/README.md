# e2e tests

Playwright tests for my-gui. Controls are selected with `getByTestId` (`data-testid`, kebab-case `<page>-<element>`).

## Running the tests (step by step)

The tests need the complete stack below. Open **four terminal windows** (A to D) and run everything from the root of the checked-out repository (the folder that contains `docker-compose.yaml`). Do not close the windows while testing.

### 0. Prerequisites (once)

| Tool | Version | Check |
| --- | --- | --- |
| Git | any | `git --version` |
| Docker with Compose v2 (Docker Desktop, or Colima + the compose plugin) | any recent | `docker compose version` |
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | see `my-gui/.nvmrc` (`nvm use`) | `node -v` |
| Yarn | 1.x | `yarn -v` |

If `docker compose version` says `unknown command` (typical with Colima or a plain `brew install docker`), install the Compose plugin:

```bash
brew install docker-compose
```

```bash
mkdir -p ~/.docker/cli-plugins && ln -sfn "$(brew --prefix)/opt/docker-compose/bin/docker-compose" ~/.docker/cli-plugins/docker-compose
```

These ports must be free: `27017` (MongoDB), `1025` and `8025` (Mailpit), `8081` (myconext-server), `8088` (tiqr-mock) and `3003` (my-gui). Stop a locally installed MongoDB, Mailpit or an IDE run configuration of the server that uses them, otherwise the tests talk to the wrong service.

### 1. Install the dependencies (once, terminal A)

```bash
yarn
yarn --cwd my-gui playwright install chromium
mvn -q -DskipTests -pl myconext-server,tiqr-mock -am install
```

The first run takes a few minutes (downloads). `yarn` installs all JavaScript dependencies of the repository, the second command downloads the browser that Playwright drives, the `mvn` command builds the Java servers.

### 2. Start MongoDB and Mailpit (terminal A)

```bash
docker compose up -d mongo mailpit
```

- `mongo`: the database (a replica set that initialises itself). Wait until `docker compose ps` shows `mongo` as `healthy`, this takes some seconds.
- `mailpit`: receives the mails of the server (SMTP :1025), the tests read the codes from it (web UI http://localhost:8025, user `user`, password `password`).

### 3. Start the tiqr-mock (terminal B)

```bash
mvn -pl tiqr-mock spring-boot:run
```

Plays the eduID app and the institution login (`/oidc`) on http://localhost:8088. It is ready when the log shows `Started TiqrApplication`.

### 4. Start the myconext-server with the e2e profile (terminal C)

```bash
mvn -pl myconext-server spring-boot:run -Dspring-boot.run.profiles=test,e2e
```

The server runs on http://localhost:8081. It is ready when the log shows `Started MyConextServerApplication`.

- `test`: every request is authenticated as the user with uid 1234567890
- `e2e`: seeds that user (`E2eSeed`), exposes `/e2e/reset` and `/e2e/sms-code` (`E2eController`) and uses the OIDC mock of the tiqr-mock as institution login

### 5. Run the tests (terminal D)

Playwright starts the my-gui dev server (:3003) by itself when it is not running. The first start compiles the app and can take a minute.

```bash
cd ./my-gui
yarn e2e:ui     # opens the Playwright window, press the green "play" button (top left) to run all tests
```

All tests should turn green (32 tests, about 3 minutes). Other ways to run them:

```bash
cd ./my-gui
yarn e2e        # headless, prints the result in the terminal
```

If a test fails, the screenshot and trace are in `my-gui/test-results/` (open a trace with `yarn playwright show-trace <path>/trace.zip`).

### 6. Stopping everything

Press `Ctrl+C` in the windows of the tiqr-mock and the server, then:

```bash
docker compose down        # add -v to also remove the database volume
```

### Troubleshooting

- `address already in use` / tests fail immediately: one of the ports above is taken, see step 0.
- `no code mail for ...` (timeout): Mailpit is not running, or it is not the one from `docker-compose.yaml` (a locally installed Mailpit has different credentials, or is slow to accept mail). Use `docker compose up -d mailpit`.
- Every page returns 404 or the tests cannot reach the app: make sure `yarn` was run in the repository root and that nothing else uses port 3003.
- The server cannot connect to MongoDB: wait until `docker compose ps` reports `mongo` as `healthy`, then restart the server.

Environment overrides: `E2E_BASE_URL`, `SERVER_URL`, `TIQR_MOCK_URL`, `MAILPIT_URL`, `MAILPIT_AUTH`.

## Helpers (`support/`)

- `tiqr.ts`: `completeEnrollment`, `completeAuthentication`, `deleteRegistration` call the tiqr-mock GET endpoints.
- `@myconext/shared-gui/e2e` (`shared-gui/src/e2e/mailpit.ts`): `waitForCode` polls the Mailpit REST API for a mail to an address and returns the code.
- `sms.ts`: `expectSmsCode` reads the last SMS code from the e2e hook of the server.
- `app.ts`: `enrollApp` activates the app with a backup code or an SMS code.
- `fixtures.ts`: automatic `user` fixture, restores the seeded users through `/e2e/reset` before every test (the tests change and delete the user).
- `a11y.ts`: `expectNoA11yViolations` runs axe-core (`@axe-core/playwright`, WCAG 2.x A/AA rules) on the current page and fails with the violated rules and elements. See `accessibility.spec.ts`.
- `server.ts`: the server URL for the hooks that the dev server does not proxy.

All tests share one user, so they run serially (`workers: 1`).

The login domain (account-gui) is not part of the stack. Where a flow continues there (finishing the app enrollment, saving a password, deleting the account, registering a passkey) the tests stub the requests to `localhost:3004` and assert on them.
