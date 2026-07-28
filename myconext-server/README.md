# MyConext Server

Spring Boot backend implementing the eduID/SURFconext "MyConext" Identity Provider: SAML2 and
OIDC authentication flows, MongoDB-backed user/session persistence, RSA-based SAML request
signing, attribute manipulation/aggregation APIs, and the OpenAPI/Swagger documentation for all
of it.

## Overview

`myconext-server` is one of six modules in this repository (see the [root README](../README.md)
for the full picture):

| Module | Role |
|---|---|
| **`myconext-server`** | **This module** — Spring Boot backend: SAML/OIDC IdP logic, persistence, mail, cron |
| [`account-gui`](../account-gui) | The IdP frontend a user authenticates against |
| [`myconext-gui`](../myconext-gui) | Example Service Provider used for local development/testing |
| [`servicedesk-gui`](../servicedesk-gui) | Service-desk Service Provider (support staff tooling) |
| [`public-gui`](../public-gui) | Public content/marketing website |
| `tiqr-mock` | Mock Tiqr backend used for local development of the app-based (Tiqr) login flow |

All four GUI projects talk to this server over HTTP. Locally it listens on port `8081`
(see [`application.yml`](src/main/resources/application.yml)); in production/Docker it's proxied
by each GUI's Apache config to `myconextserver:8080`.

A one-off static-analysis artifact, [`CODE_QUALITY_REPORT.md`](CODE_QUALITY_REPORT.md), lives in
this directory (untracked in git). It documents wildcard-import usage and some redundant
`UserDetails`/mixin-interface patterns found in the codebase as of 2026-07-28 — useful background
if you're touching `model/User.java`, `model/ExternalUser.java`, `security/RemoteUser.java`, or the
`security/UserAuthentication.java` / `api/HasUserRepository.java` interfaces, but it's a snapshot,
not living documentation.

## Tech stack

Taken from [`pom.xml`](pom.xml) and the parent [`../pom.xml`](../pom.xml):

- **Java 21** (`<release>21</release>` enforced by `maven-enforcer-plugin` in the parent POM) with
  **Maven 3.8.4+**.
- **[Spring Boot 3.5.13](https://spring.io/projects/spring-boot)** (inherited as the parent
  `spring-boot-starter-parent`), with `spring-boot-starter-web`, `-mail`, `-validation`,
  `-actuator`, `-oauth2-client`, `-oauth2-resource-server` and `-data-mongodb`.
- **[`org.openconext:saml-idp` 3.1.1](pom.xml)** — the shared OpenConext SAML IdP library this
  server builds on (see [`SAML.md`](../SAML.md) at the repo root for background on the
  SAML implementation).
- **Spring Security 6.5.10** (`spring-security-core`, `-config`, `-crypto`,
  `-oauth2-jose`) — note `SecurityAutoConfiguration` is explicitly excluded in
  [`MyConextServerApplication`](src/main/java/myconext/MyConextServerApplication.java); security is
  wired manually in `security/SecurityConfiguration.java`.
- **MongoDB** via `spring-boot-starter-data-mongodb` + `spring-session-data-mongodb` (HTTP session
  storage) and **[Mongock](https://www.mongock.io/) 5.5.1** (`mongock-springboot` +
  `mongodb-springdata-v4-driver`) for schema/data migrations — see
  [`mongo/Migrations.java`](src/main/java/myconext/mongo/Migrations.java).
- **[`org.openconext:tiqr-java-connector` 3.1.4](pom.xml)** and **`org.openconext:openconext-oidc-client` 0.0.4**
  — OpenConext libraries for the Tiqr (mobile app) protocol and generic OIDC client support.
- **[Yubico `webauthn-server-core` 2.7.0](pom.xml)** — FIDO2/WebAuthn security-key registration and
  authentication (see `myconext/webauthn/`).
- **[`springdoc-openapi-starter-webmvc-ui` 2.8.9](pom.xml)** — generates the OpenAPI/Swagger docs
  (see [API documentation](#api-documentation) below).
- **`com.maxmind.geoip2` 3.0.1** — GeoIP lookups (`myconext/geo/`), can run against a mock in dev.
- **Lombok**, **Jackson** (`jackson-databind` 2.22.0 + `jdk8`/`jsr310` modules), **Mustache**
  compiler (renders the HTML mail templates in
  [`src/main/resources/mail_templates`](src/main/resources/mail_templates)), **Guava**,
  **`commons-email2-jakarta`** for sending mail, and **Micrometer + Prometheus registry** for
  metrics.
- **Logging**: Logback with `logstash-logback-encoder` and the OpenConext `threshold-logger`
  library, configured via
  [`logback-spring.xml`](src/main/resources/logback-spring.xml).
- **Test-only**: `spring-boot-starter-test`, `rest-assured` 5.5.6, `wiremock-spring-boot` 3.10.6,
  `awaitility` 4.3.0, `greenmail-junit4` 2.1.5, and the JUnit Vintage engine (the test suite mixes
  JUnit 4 and JUnit 5).
- **Build plugins**: `spring-boot-maven-plugin`, `git-commit-id-maven-plugin` (embeds git info into
  `/internal/info`, see [Configuration](#configuration)), `versions-maven-plugin`, and the
  `jacoco-maven-plugin` (coverage report on `mvn test`, excluding `myconext/mongo/Migrations.*` and
  `myconext/geo/**`).

## Architecture / key packages

All Java sources live under `src/main/java/myconext/`:

| Package | Responsibility |
|---|---|
| `api/` | REST controllers: `UserController` (account/registration/WebAuthn/session endpoints), `LoginController`, `AccountLinkerController`, `ServiceDeskController`, `EnvironmentController` (`/config` for the GUIs), `MetricsController`, `DefaultErrorController`, plus the `SamlAuthenticationCallBack` interface and the `HasUserRepository` mixin. |
| `security/` | Spring Security wiring: `SecurityConfiguration`, `FirewallConfiguration`, and **`GuestIdpAuthenticationRequestFilter`** — the central SAML authentication filter that handles SSO/cookie, magic-link, and Tiqr login (see [`../LOGIN.md`](../LOGIN.md) for a full walkthrough). Also cookie/CSRF helpers, `RemoteUser`, rate-limiting (`EmailGuessingPrevention`) and the OIDC-client user service. |
| `model/` | Domain + MongoDB document models: `User`, `ExternalUser`, `ServiceProvider`, `SamlAuthenticationRequest`, `LinkedAccount`, `OneTimeLoginCode`, `Token`, etc. |
| `repository/` | Spring Data MongoDB repositories (`UserRepository`, `AuthenticationRequestRepository`, `ChallengeRepository`, `EnrollmentRepository`, `MetricsRepository`, …), one per top-level document. |
| `manage/` | Client for OpenConext **Manage** (SP/IdP metadata registry) — `Manage` interface with `RemoteManage` (real HTTP client) and `MockManage` (used when `manage.enabled: False`, the local-dev default). |
| `oidcng/` | OIDC client integration with OpenConext's `oidcng` — `OpenIDConnect` interface, `OpenIDConnectRemote` / `OpenIDConnectMock`, selected via the `dev` Spring profile (`@Profile({"dev"})` / `@Profile({"!dev"})` in `OpenIDConnectConfiguration`). |
| `webauthn/` | FIDO2/WebAuthn credential storage (`UserCredentialRepository`) built on Yubico's `webauthn-server-core`. |
| `tiqr/` | Tiqr (mobile app) enrollment/authentication: `TiqrController`, `SURFSecureID`, `RateLimitEnforcer`, request/response DTOs. |
| `invite/` | Integration with OpenConext Invite for role/eduID provisioning (`InviteController`, `EduIDProvision`). |
| `remotecreation/` | Remote-creation API used by external systems (e.g. Studielink) to provision eduIDs (`RemoteCreationController`). |
| `eduid/` | `APIController` — token-introspection-backed API for other services to look up eduID data. |
| `aa/` | `AttributeAggregatorController` — the attribute-aggregation endpoint (see [API documentation](#api-documentation)). |
| `verify/` | ID-verification/attribute-mapping support for the iDIN flow (`AttributeMapper`, `VerifyState`). |
| `crypto/` | `KeyGenerator` / `HashGenerator` — RSA key handling and hashing helpers (see [Crypto / SAML signing](#crypto--saml-signing)). |
| `cron/` | Scheduled jobs — see [Cron jobs](#cron-jobs) below. |
| `mail/` | `MailBox` (sends magic-link/OTP/verification/nudge emails via Mustache templates) and `MailConfiguration`. |
| `mongo/` | `Migrations` (Mongock changesets), `MongoMapping`/`MongoConversions`/`DateConverter`, `ExtendedMongoHealthIndicator`. |
| `sms/`, `geo/`, `captcha/` | Pluggable services with a real + mock implementation each: `SMSServiceImpl`/`SMSServiceMock`, `MaxMindGeoLocation`/`MockGeoLocation`, `CaptchaVerifier`. |
| `session/`, `log/`, `validation/`, `exceptions/`, `util/`, `config/` | HTTP session config, MDC request logging, password-strength validation, the app's exception hierarchy, and misc. config/util classes (e.g. `CreateFromInstitutionProperties`). |
| `MyConextServerApplication` | The `@SpringBootApplication` entry point (excludes Spring's default `SecurityAutoConfiguration`; `@EnableScheduling` turns on the cron jobs). |
| `SwaggerOpenIdConfig` | OpenAPI bean definitions (security scheme, servers, info) consumed by springdoc. |

## Prerequisites

- **Java 21** and **Maven 3.8.4+** (enforced by `maven-enforcer-plugin`, see [`../pom.xml`](../pom.xml)).
- **MongoDB**, run as a single-node replica set — required because the app relies on Mongock
  migrations and change-stream-friendly features. The root [`docker-compose.yaml`](../docker-compose.yaml)
  provisions `mongo:7` with `--replSet openconext` and auto-initiates the replica set via its
  healthcheck. (Note: the root README's System Requirements list `MongoDB 3.4.x`, but the actual
  Docker Compose service pins `mongo:7` — that root doc looks stale on this point.)
- **[Mailpit](https://mailpit.axllent.org/)** for capturing outgoing mail locally (SMTP on `1025`,
  web UI on `8025`); also provisioned by the root `docker-compose.yaml`.

## Getting started

1. From the repo root, start MongoDB and Mailpit:
   ```shell
   docker compose up -d
   ```
2. Run the server with the `dev` Spring profile:
   ```shell
   cd myconext-server
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   The `dev` profile matters beyond convenience: `oidcng/OpenIDConnectConfiguration` swaps in
   `OpenIDConnectMock`/`OpenIDConnectRemote` based on `@Profile({"dev"})` vs `@Profile({"!dev"})`.
3. Alternatively, run/debug
   [`MyConextServerApplication`](src/main/java/myconext/MyConextServerApplication.java)'s
   `main()` method directly from your IDE — just make sure the `dev` profile is active there too.
4. The server listens on **http://localhost:8081** (`server.port` in
   [`application.yml`](src/main/resources/application.yml)). On its own it isn't very useful — pair
   it with `account-gui` (port 3000) and at least one of `myconext-gui` (3001) or
   `servicedesk-gui` (3003); see the [root README's full walkthrough](../README.md#how-to-use).

There's also an `application-servicedesk.yml` profile
([`src/main/resources/application-servicedesk.yml`](src/main/resources/application-servicedesk.yml))
that overrides `host_headers.active` to `servicedesk.myconext.nl` and enables
`service_desk_role_auto_provisioning`, for running the backend against `servicedesk-gui` instead of
the default `mijn.myconext.nl` host.

## Configuration

Default configuration lives in
[`src/main/resources/application.yml`](src/main/resources/application.yml) (360 lines). Notable
sections:

- **`server.port: 8081`**, error path `/error`.
- **`springdoc`** — restricts generated OpenAPI docs to `/api/remote-creation/**`,
  `/myconext/api/invite/**` and `/mobile/**`; docs served at `/myconext/api/api-docs`, Swagger UI
  at `/myconext/api/` (see [API documentation](#api-documentation)).
- **`cron.*`** — cron expressions and toggles for every scheduled job (see
  [Cron jobs](#cron-jobs)); `cron.node-cron-job-responsible` gates whether *this* node runs cron
  jobs at all, for multi-node deployments.
- **`manage.*`** — credentials/base URL for the OpenConext Manage registry; `manage.enabled: False`
  by default locally, which routes through `MockManage` instead of `RemoteManage`.
- **`mongodb_db`**, `spring.data.mongodb.uri` — database name/connection string (defaults to
  `mongodb://127.0.0.1:27017/surf_id_test`).
- **`feature.*`** — a large block of feature flags (WebAuthn, captcha, ID-verify, allow/deny lists,
  service-desk activation, app-login, account linking, etc.) most of which are also surfaced to the
  GUIs via `EnvironmentController`'s `/config` endpoint.
- **`external-api-configuration.remote-users`** — in-memory Basic-Auth users/scopes for
  server-to-server callers (`aa`, `oidcng`, `studielink`, `invite`, `internal`) used by the
  attribute-aggregation, attribute-manipulation, remote-creation, invite and actuator endpoints.
- **`private_key_path` / `certificate_path`** — the RSA key pair used to sign SAML requests/responses
  (see [Crypto / SAML signing](#crypto--saml-signing)).
- **`spring.mail.host` / `spring.mail.port`** — points at Mailpit (`localhost:1025`) by default.
- **`management.endpoints.web.base-path: "/internal"`** — actuator/Prometheus metrics are exposed
  under `/internal` (health, info, prometheus), not the default `/actuator`.

Most of these are also overridable via environment variables (Spring Boot's relaxed binding) in
non-local (Ansible-managed) deployments.

## API documentation

Swagger UI and the raw OpenAPI spec (paths configured in `application.yml`'s `springdoc` section):

- Local: http://localhost:8081/myconext/api/swagger-ui/index.html and
  http://localhost:8081/myconext/api/api-docs
- Test environment: https://login.test2.eduid.nl/myconext/api/swagger-ui/index.html and
  https://login.test2.eduid.nl/myconext/api/api-docs

Two server-to-server endpoints documented in the [root README](../README.md#attribute-manipulation)
are worth calling out directly since they aren't part of the generated Swagger set:

```shell
# Attribute manipulation (oidcng)
curl -u oidcng:secret "http://login.test2.eduid.nl/myconext/api/attribute-manipulation?sp_entity_id=https://test.okke&uid=0eaa7fb2-4f94-476f-b3f6-c8dfc4115a87&sp_institution_guid=null"

# Attribute aggregation (aa)
curl -u aa:secret "https://login.test2.eduid.nl/myconext/api/attribute-aggregation?sp_entity_id=https://mijn.test2.eduid.nl/shibboleth&eduperson_principal_name=j.doe@example.com"
```

Both endpoints are implemented in `myconext/aa/AttributeAggregatorController.java` and referenced
from `security/ExternalApiConfiguration.java`'s Basic-Auth scopes.

## Cron jobs

Scheduled jobs run under `@EnableScheduling` (set on `MyConextServerApplication`) and each extends
[`myconext/cron/AbstractNodeLeader.java`](src/main/java/myconext/cron/AbstractNodeLeader.java),
which uses a MongoDB `distributed_locks` collection (with a TTL index) to ensure only one node in a
multi-node deployment executes a given job at a time — see `cron.node-cron-job-responsible` in
[Configuration](#configuration). Jobs found under `myconext/cron/`:

- `InactivityMail` — mails users about account inactivity (`cron.inactivity-users-expression`).
- `InstitutionMailUsage` — periodic mail to users about institution-account usage
  (`cron.mail-institution-mail-usage-expression`, batched via `cron.mail-institution-batch-size`).
- `NudgeAppMail` — nudges users who haven't installed the eduID app
  (`cron.nudge-app-mail-expression`, `cron.nudge-app-mail-days-after-creation`).
- `ResourceCleaner` — cleans up expired tokens/resources (`cron.token-cleaner-expression`).
- `TiqrCleaner` — cleans up stale Tiqr enrollment/authentication state.
- `DisposableEmailProviders` — refreshes/loads the disposable-email-provider deny list used by
  `feature.deny_disposable_email_providers`.

## Crypto / SAML signing

The server signs outgoing SAML requests/responses with a private RSA key
(`private_key_path`/`certificate_path` in `application.yml`). Integration tests generate a
throwaway key pair on the fly (see `crypto/KeyGenerator.java`), so no default key ships with the
repo. For generating and registering a real key pair for a non-local deployment, follow the
[Crypto section of the root README](../README.md#crypto) rather than duplicating those `openssl`
commands here.

## Testing

```shell
cd myconext-server
mvn test
```

- 75 test files under `src/test/java/myconext/`, mirroring the main package layout (`api/`, `aa/`,
  `cron/`, `crypto/`, `eduid/`, `geo/`, `invite/`, `mail/`, `manage/`, `model/`, `oidcng/`,
  `remotecreation/`, `repository/`, `security/`, `sms/`, `tiqr/`, `verify/`, `webauthn/`).
- Most integration tests extend
  [`AbstractIntegrationTest`](src/test/java/myconext/AbstractIntegrationTest.java), which boots the
  full Spring context (`@SpringBootTest` on a random port), uses `rest-assured` for HTTP
  assertions, and `MockManage` in place of a real Manage instance. `AbstractMailBoxTest` layers
  GreenMail (an in-JVM SMTP server) on top for asserting on sent emails instead of relying on
  Mailpit.
- WireMock (`wiremock-spring-boot`) stubs outbound calls to external services (e.g. `oidcng`,
  MaxMind) in tests that need them.
- The test suite mixes JUnit 4 (`org.junit.Test`/`@RunWith(SpringRunner.class)`, via the
  `junit-vintage-engine`) and JUnit 5 — don't assume one style throughout.
- `mvn test` also runs the `jacoco-maven-plugin`, producing a coverage report (excluding
  `myconext/mongo/Migrations.*` and `myconext/geo/**`); the repo-root badge
  (`.github/badges/jacoco.svg`) is generated from this in CI.

## Build & deployment

```shell
mvn package     # builds myconext-server.jar under target/
mvn deploy       # from the repo root, builds/deploys all modules' production artifacts
```

Containerization is a two-line [`docker/Dockerfile`](docker/Dockerfile): it copies the built
`target/*.jar` into an `eclipse-temurin:21-jdk-alpine` base image and runs it with
`java -jar /app.jar`. There is no `docker-compose` service for the server itself in this repo (only
Mongo + Mailpit) — it's expected to be run directly via Maven locally, or deployed as this
container image elsewhere.

## Related documentation

- [Root README](../README.md) — overall system, System Requirements, running MongoDB/Mailpit, the
  full IdP flow walkthrough, [Crypto](../README.md#crypto), and
  [Attribute Manipulation/Aggregation](../README.md#attribute-manipulation) curl examples.
- [`../LOGIN.md`](../LOGIN.md) — detailed walkthrough of the three login routes (SSO/cookie, magic
  link, Tiqr) implemented in `security/GuestIdpAuthenticationRequestFilter.java`.
- [`../SAML.md`](../SAML.md) — background/links on the SAML implementation approach.
- [`../TIQR.md`](../TIQR.md) — sequence diagram of the Tiqr enrollment/authentication protocol
  implemented in `tiqr/`.
- [`account-gui`](../account-gui/README.md) — the IdP frontend this server authenticates users for.
- [`myconext-gui`](../myconext-gui) — example Service Provider for local testing.
- [`servicedesk-gui`](../servicedesk-gui/README.md) — service-desk Service Provider.
- [`public-gui`](../public-gui/README.md) — public content website.
- [`CODE_QUALITY_REPORT.md`](CODE_QUALITY_REPORT.md) — one-off static-analysis snapshot (untracked),
  see [Overview](#overview).

---
_Last updated: 2026-07-28_
