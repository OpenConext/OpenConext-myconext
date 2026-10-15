---
type: C4 System
title: Studielink
status: stable
groma:
  id: studielink
  technology: HTTPS
description: Creates institution eduIDs through the remote API.
---

Configured client of the remote-creation API. It creates and updates eduIDs for its institution and binds them to an institution pseudonym.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [studielink](studielink.md) | [myconext-server/src/main/java/myconext/remotecreation/RemoteCreationController.java](../../myconext-server/src/main/java/myconext/remotecreation/RemoteCreationController.java) | Provisions eduIDs | HTTPS |
