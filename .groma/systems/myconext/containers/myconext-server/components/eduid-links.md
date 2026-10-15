---
type: C4 Component
title: Linked-account lookup
status: stable
groma:
  id: eduid-links
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/eduid/APIController.java
      symbol: APIController
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/StatusResponse.java
      symbol: StatusResponse
  group: Service APIs
description: Returns linked institution accounts for a bearer token.
---

eduBadges calls `/myconext/api/eduid/links` with an OIDC access token. The response lists each linked account's principal name, home organization, preferred flag, and validated name.
