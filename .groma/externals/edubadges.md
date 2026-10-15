---
type: C4 System
title: eduBadges
status: stable
groma:
  id: edubadges
  technology: OIDC
description: Reads linked accounts for a signed-in person.
---

Calls the linked-account lookup with an access token to read a person's institution links and validated name.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [edubadges](edubadges.md) | [myconext-server/src/main/java/myconext/eduid/APIController.java](../../myconext-server/src/main/java/myconext/eduid/APIController.java) | Reads linked accounts | OIDC |
