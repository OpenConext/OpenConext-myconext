---
type: C4 Component
title: Identity verification
status: stable
groma:
  id: identity-verification
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/verify/AttributeMapper.java
      symbol: AttributeMapper
    - scanner: java
      file: myconext-server/src/main/java/myconext/verify/VerifyState.java
      symbol: VerifyState
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/IdinIssuers.java
      symbol: IdinIssuers
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/VerifyIssuer.java
      symbol: VerifyIssuer
  group: Account
description: Applies a successful identity check to the eduID.
---

Maps attributes returned by iDIN, eHerkenning, Studielink, or the service desk onto the account and records who verified the name. The linking and service-desk APIs call it after the external check or the in-person approval.
