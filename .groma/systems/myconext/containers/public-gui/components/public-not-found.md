---
type: C4 Component
title: Page not found
status: stable
groma:
  id: public-not-found
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/routes/NotFound.jsx
      symbol: NotFound
  group: Information pages
description: Shown when a public URL does not match a page.
---

Fallback route for the public site.
