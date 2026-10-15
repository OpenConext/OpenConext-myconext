---
type: Groma Flow
title: Sign in with the eduID app
groma:
  id: sign-in-with-the-eduid-app
---

The person chooses the app on the login screen. The app confirms the Tiqr challenge, and the identity provider returns the assertion.

## Steps

| From | To | Action |
| --- | --- | --- |
| [eduID user](../actors/eduid-user.md) | [Login client](../systems/myconext/containers/account-gui/components/account-client.md) | Choose app sign-in |
| [Login client](../systems/myconext/containers/account-gui/components/account-client.md) | [eduID app API](../systems/myconext/containers/myconext-server/components/tiqr.md) | Start the challenge |
| [eduID app](../externals/eduid-app.md) | [eduID app API](../systems/myconext/containers/myconext-server/components/tiqr.md) | Confirm in the app |
| [Guest IdP login](../systems/myconext/containers/myconext-server/components/guest-idp-login.md) | [SURFconext engine](../externals/surfconext-engine.md) | Return the SAML assertion |
