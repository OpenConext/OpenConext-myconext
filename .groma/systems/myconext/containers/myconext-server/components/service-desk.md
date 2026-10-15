---
type: C4 Component
title: Service desk API
status: stable
groma:
  id: service-desk
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/ServiceDeskController.java
      symbol: ServiceDeskController
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ControlCode.java
      symbol: ControlCode
  group: Account
description: Looks up a verification code and records the in-person approval.
---

Staff call this after they have matched an identity document to the account. Approving the check marks the identity verified. The numeric code itself is created from My eduID through the account API.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/api/ServiceDeskController.java](../../../../../../myconext-server/src/main/java/myconext/api/ServiceDeskController.java) | [myconext-server/src/main/java/myconext/verify/AttributeMapper.java](../../../../../../myconext-server/src/main/java/myconext/verify/AttributeMapper.java) | Records the verified name | Java |
