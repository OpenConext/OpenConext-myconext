---
type: C4 Component
title: Terms of use
status: stable
groma:
  id: terms-of-use
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/terms/TermsEN.jsx
      symbol: TermsEN
    - scanner: javascript
      file: public-gui/src/terms/TermsNL.jsx
      symbol: TermsNL
    - scanner: javascript
      file: public-gui/src/routes/Terms.jsx
      symbol: Terms
  group: Information pages
description: Publishes the eduID terms of use in Dutch and English.
---

Legal text for the public site, with a language-specific page and a selector.
