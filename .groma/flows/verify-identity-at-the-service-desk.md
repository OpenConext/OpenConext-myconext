---
type: Groma Flow
title: Verify identity at the service desk
groma:
  id: verify-identity-at-the-service-desk
---

The person gets a numeric code in My eduID. Service-desk staff compare an identity document with the account and approve the check.

## Steps

| From | To | Action |
| --- | --- | --- |
| [eduID user](../actors/eduid-user.md) | [My eduID client](../systems/myconext/containers/myconext-gui/components/my-eduid-client.md) | Ask for a verification code |
| [My eduID client](../systems/myconext/containers/myconext-gui/components/my-eduid-client.md) | [Account API](../systems/myconext/containers/myconext-server/components/account-api.md) | Create the control code |
| [Service desk staff](../actors/service-desk-staff.md) | [Verification wizard](../systems/myconext/containers/servicedesk-gui/components/desk-verification.md) | Compare the identity document |
| [Desk shell](../systems/myconext/containers/servicedesk-gui/components/desk-shell.md) | [Service desk API](../systems/myconext/containers/myconext-server/components/service-desk.md) | Approve the check |
