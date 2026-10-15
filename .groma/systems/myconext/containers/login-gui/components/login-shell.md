---
type: C4 Component
title: Login shell
status: stable
groma:
  id: login-shell
  parent: login-gui
  code:
    - scanner: react
      file: login-gui/src/App.tsx
    - scanner: typescript
      file: login-gui/src/App.tsx
      symbol: App
    - scanner: react
      file: login-gui/src/root.tsx
    - scanner: typescript
      file: login-gui/src/root.tsx
    - scanner: react
      file: login-gui/src/routes/_index.tsx
    - scanner: typescript
      file: login-gui/src/routes/_index.tsx
      symbol: Home
    - scanner: typescript
      file: login-gui/src/routes.ts
    - scanner: typescript
      file: login-gui/src/store/store.ts
      symbol: useAppStore
    - scanner: react
      file: login-gui/src/routes/$.tsx
    - scanner: typescript
      file: login-gui/src/routes/$.tsx
      symbol: NotFound
    - scanner: typescript
      file: login-gui/src/api/index.ts
    - scanner: react
      file: login-gui/src/routes/about/route.tsx
    - scanner: typescript
      file: login-gui/src/routes/about/route.tsx
      symbol: About
description: Placeholder React shell that loads server configuration.
---

Fetches `/config`, keeps a small UI store, and routes an about page and a catch-all. It does not yet implement magic-link, password, passkey, or app sign-in.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [login-gui/src/api/index.ts](../../../../../../login-gui/src/api/index.ts) | [myconext-server/src/main/java/myconext/api/LoginController.java](../../../../../../myconext-server/src/main/java/myconext/api/LoginController.java) | Loads configuration | HTTPS |

## Derived relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [login-gui/src/api/index.ts](../../../../../../login-gui/src/api/index.ts) | [myconext-server/src/main/java/myconext/api/LoginController.java](../../../../../../myconext-server/src/main/java/myconext/api/LoginController.java) | Calls HTTP endpoint: GET /config | java, typescript |
