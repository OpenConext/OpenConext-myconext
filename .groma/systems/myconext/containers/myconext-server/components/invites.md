---
type: C4 Component
title: eduID invites
status: stable
groma:
  id: invites
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/invite/InviteController.java
      symbol: InviteController
    - scanner: java
      file: myconext-server/src/main/java/myconext/invite/EduIDProvision.java
      symbol: EduIDProvision
  group: Service APIs
description: Provisions an eduID for a person invited from an institution.
---

A trusted invite client posts the institution identity. The server finds that institution in Manage metadata and creates or updates the eduID. The calling product is only identified as the invite API account in configuration.
