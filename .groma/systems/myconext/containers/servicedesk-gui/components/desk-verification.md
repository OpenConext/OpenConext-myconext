---
type: C4 Component
title: Verification wizard
status: stable
groma:
  id: desk-verification
  parent: servicedesk-gui
  code:
    - scanner: javascript
      file: servicedesk-gui/src/tabs/Confirmation.jsx
      symbol: Confirmation
    - scanner: javascript
      file: servicedesk-gui/src/tabs/Verification.jsx
      symbol: Verification
    - scanner: javascript
      file: servicedesk-gui/src/tabs/VerifyWizard.jsx
      symbol: VerifyWizard
    - scanner: javascript
      file: servicedesk-gui/src/tabs/Control.jsx
      symbol: Control
  group: Desk screens
description: Walks staff through lookup, document check, and approval.
---

The wizard steps for finding the account by code, comparing the identity document, and confirming the decision.
