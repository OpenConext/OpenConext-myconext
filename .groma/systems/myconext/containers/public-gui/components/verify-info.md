---
type: C4 Component
title: Verification information
status: stable
groma:
  id: verify-info
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/routes/Verify_NL.jsx
      symbol: Verify_NL
    - scanner: javascript
      file: public-gui/src/routes/Verify_EN.jsx
      symbol: Verify_EN
    - scanner: javascript
      file: public-gui/src/routes/Verify.jsx
      symbol: Verify
  group: Information pages
description: Explains iDIN and eHerkenning verification.
---

Dutch and English pages about verifying an eduID online. Starting the check happens in the login or My eduID flow, not on this page.
