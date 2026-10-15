---
type: C4 Actor
title: Service desk staff
status: stable
groma:
  id: service-desk-staff
description: Employee who verifies an eduID against an identity document.
---

A SURF service-desk employee who checks an eduID in person. They look up the person's verification code, compare an identity document with the account, and approve the check.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [service-desk-staff](service-desk-staff.md) | [servicedesk-gui/src/pages/Login.jsx](../../servicedesk-gui/src/pages/Login.jsx) | Signs in | Browser |
| [service-desk-staff](service-desk-staff.md) | [servicedesk-gui/src/tabs/VerifyWizard.jsx](../../servicedesk-gui/src/tabs/VerifyWizard.jsx) | Checks the document | Browser |
