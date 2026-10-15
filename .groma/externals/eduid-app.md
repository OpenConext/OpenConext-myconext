---
type: C4 System
title: eduID app
status: stable
groma:
  id: eduid-app
  technology: Tiqr
description: Mobile app that confirms a Tiqr login.
---

The eduID mobile app. It enrolls with the server and confirms a login or step-up over the Tiqr protocol.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [eduid-app](eduid-app.md) | [myconext-server/src/main/java/myconext/tiqr/TiqrController.java](../../myconext-server/src/main/java/myconext/tiqr/TiqrController.java) | Confirms the login | Tiqr |
