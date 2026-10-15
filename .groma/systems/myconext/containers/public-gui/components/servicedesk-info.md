---
type: C4 Component
title: Service desk information
status: stable
groma:
  id: servicedesk-info
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/routes/ServiceDesk_EN.jsx
      symbol: ServiceDesk_EN
    - scanner: javascript
      file: public-gui/src/routes/ServiceDesk_NL.jsx
      symbol: ServiceDesk_NL
    - scanner: javascript
      file: public-gui/src/routes/ServiceDesk.jsx
      symbol: ServiceDesk
  group: Information pages
description: Explains the in-person identity check.
---

Dutch and English pages that describe visiting or calling the service desk. The check itself is done in the service-desk application.
