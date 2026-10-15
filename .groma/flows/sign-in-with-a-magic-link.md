---
type: Groma Flow
title: Sign in with a magic link
groma:
  id: sign-in-with-a-magic-link
---

The usual eduID sign-in. The person enters an email address, opens the one-time message, and the identity provider returns a SAML assertion to SURFconext.

## Steps

| From | To | Action |
| --- | --- | --- |
| [eduID user](../actors/eduid-user.md) | [Login client](../systems/myconext/containers/account-gui/components/account-client.md) | Choose email sign-in |
| [Login client](../systems/myconext/containers/account-gui/components/account-client.md) | [Account API](../systems/myconext/containers/myconext-server/components/account-api.md) | Ask for a one-time code |
| [Account API](../systems/myconext/containers/myconext-server/components/account-api.md) | [Mail](../systems/myconext/containers/myconext-server/components/mail.md) | Send the code |
| [Mail](../systems/myconext/containers/myconext-server/components/mail.md) | [Mail server](../externals/mail-server.md) | Deliver the message |
| [eduID user](../actors/eduid-user.md) | [Guest IdP login](../systems/myconext/containers/myconext-server/components/guest-idp-login.md) | Open the magic link |
| [Guest IdP login](../systems/myconext/containers/myconext-server/components/guest-idp-login.md) | [SURFconext engine](../externals/surfconext-engine.md) | Return the SAML assertion |
