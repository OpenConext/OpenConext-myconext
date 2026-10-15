---
type: C4 Component
title: API errors
status: stable
groma:
  id: api-errors
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/AbstractBaseException.java
      symbol: AbstractBaseException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/ForbiddenException.java
      symbol: ForbiddenException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/NotAcceptableException.java
      symbol: NotAcceptableException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/UserNotFoundException.java
      symbol: UserNotFoundException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/RemoteException.java
      symbol: RemoteException
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/DefaultErrorController.java
      symbol: DefaultErrorController
  group: Runtime
description: Shared failure types and the fallback error response.
---

Maps expected failures such as a missing user, a forbidden call, or a remote outage onto HTTP responses. The fallback error controller handles anything that escapes a specific API.
