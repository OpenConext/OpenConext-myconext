---
type: C4 Component
title: Privacy policy
status: stable
groma:
  id: privacy-policy
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/routes/Privacy.jsx
      symbol: Privacy
    - scanner: javascript
      file: public-gui/src/terms/PrivacyNL.jsx
      symbol: PrivacyNL
    - scanner: javascript
      file: public-gui/src/terms/PrivacyEN.jsx
      symbol: PrivacyEN
  group: Information pages
description: Publishes the eduID privacy policy in Dutch and English.
---

Legal text for the public site, with a language-specific page and a selector.
