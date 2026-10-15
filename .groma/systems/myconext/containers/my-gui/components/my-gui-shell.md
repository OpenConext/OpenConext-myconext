---
type: C4 Component
title: My GUI shell
status: stable
groma:
  id: my-gui-shell
  parent: my-gui
  code:
    - scanner: react
      file: my-gui/src/App.tsx
    - scanner: typescript
      file: my-gui/src/App.tsx
      symbol: App
    - scanner: react
      file: my-gui/src/routes/_auth.$.tsx
    - scanner: typescript
      file: my-gui/src/routes/_auth.$.tsx
      symbol: NotFound
    - scanner: react
      file: my-gui/src/components/NavigationBar.tsx
    - scanner: typescript
      file: my-gui/src/components/NavigationBar.tsx
      symbol: NavigationBar
    - scanner: typescript
      file: my-gui/src/api/index.ts
      symbol: redirectToLogin
    - scanner: react
      file: my-gui/src/routes/_auth.tsx
    - scanner: typescript
      file: my-gui/src/routes/_auth.tsx
      symbol: ProtectedLayout
    - scanner: typescript
      file: my-gui/src/api/config.ts
    - scanner: react
      file: my-gui/src/routes/_auth.security/route.tsx
    - scanner: typescript
      file: my-gui/src/routes/_auth.security/route.tsx
      symbol: Dashboard
    - scanner: react
      file: my-gui/src/routes/_auth.personal/route.tsx
    - scanner: typescript
      file: my-gui/src/routes/_auth.personal/route.tsx
      symbol: Personal
    - scanner: typescript
      file: my-gui/src/store/store.ts
      symbol: useAppStore
    - scanner: react
      file: my-gui/src/routes/landing/route.tsx
    - scanner: typescript
      file: my-gui/src/routes/landing/route.tsx
      symbol: Landing
    - scanner: react
      file: my-gui/src/root.tsx
    - scanner: typescript
      file: my-gui/src/root.tsx
    - scanner: typescript
      file: my-gui/src/api/logout.ts
    - scanner: react
      file: my-gui/src/routes/about/route.tsx
    - scanner: typescript
      file: my-gui/src/routes/about/route.tsx
      symbol: About
    - scanner: typescript
      file: my-gui/src/routes.ts
    - scanner: react
      file: my-gui/src/routes/_auth._index.tsx
    - scanner: typescript
      file: my-gui/src/routes/_auth._index.tsx
      symbol: Home
description: Placeholder React shell for the future My eduID pages.
---

Fetches `/config`, redirects an anonymous visitor to login, and reserves routes for landing, personal details, and security. Those routes are not the working self-service pages.

## Derived relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [my-gui/src/api/config.ts](../../../../../../my-gui/src/api/config.ts) | [myconext-server/src/main/java/myconext/api/LoginController.java](../../../../../../myconext-server/src/main/java/myconext/api/LoginController.java) | Calls HTTP endpoint: GET /config | java, typescript |
| [my-gui/src/api/logout.ts](../../../../../../my-gui/src/api/logout.ts) | [myconext-server/src/main/java/myconext/api/UserController.java](../../../../../../myconext-server/src/main/java/myconext/api/UserController.java) | Calls HTTP endpoint: GET /myconext/api/sp/logout | java, typescript |

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [my-gui/src/api/index.ts](../../../../../../my-gui/src/api/index.ts) | [myconext-server/src/main/java/myconext/api/LoginController.java](../../../../../../myconext-server/src/main/java/myconext/api/LoginController.java) | Loads configuration | HTTPS |
