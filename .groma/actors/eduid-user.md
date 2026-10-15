---
type: C4 Actor
title: eduID user
status: stable
groma:
  id: eduid-user
description: Person who registers an eduID and signs in with it.
---

A person who creates an eduID and uses it to sign in. They need to register, receive a one-time code, choose a password, passkey, or the eduID app, and later manage the account in My eduID.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [eduid-user](eduid-user.md) | [account-gui/src/main.js](../../account-gui/src/main.js) | Signs in | Browser |
| [eduid-user](eduid-user.md) | [myconext-gui/src/main.js](../../myconext-gui/src/main.js) | Manages the eduID | Browser |
| [eduid-user](eduid-user.md) | [myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java](../../myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java) | Opens the magic link | Browser |
