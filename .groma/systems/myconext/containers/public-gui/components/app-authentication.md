---
type: C4 Component
title: App return
status: stable
groma:
  id: app-authentication
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/routes/TiqrAuth.jsx
      symbol: TiqrAuth
  group: Information pages
description: Page the eduID app can return to during Tiqr authentication.
---

Public route that continues an app authentication in the browser.
