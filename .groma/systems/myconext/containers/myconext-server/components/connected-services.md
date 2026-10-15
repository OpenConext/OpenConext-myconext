---
type: C4 Component
title: Connected services
status: stable
groma:
  id: connected-services
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/oidcng/OpenIDConnect.java
      symbol: OpenIDConnect
    - scanner: java
      file: myconext-server/src/main/java/myconext/oidcng/OpenIDConnectConfiguration.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/oidcng/OpenIDConnectMock.java
      symbol: OpenIDConnectMock
    - scanner: java
      file: myconext-server/src/main/java/myconext/oidcng/OpenIDConnectRemote.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/oidcng/ResilientErrorHandler.java
      symbol: ResilientErrorHandler
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/Token.java
      symbol: Token
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/TokenRepresentation.java
      symbol: TokenRepresentation
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/TokenType.java
      symbol: TokenType
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/Scope.java
      symbol: Scope
  group: Account
description: Lists and revokes a person's SURFconext service tokens.
---

Talks to the SURFconext token API for the services a person has used. My eduID shows and deletes those tokens through the account API, which delegates the remote call here. A mock stands in when the token API is disabled.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/oidcng/OpenIDConnectRemote.java](../../../../../../myconext-server/src/main/java/myconext/oidcng/OpenIDConnectRemote.java) | [surfconext-connect](../../../../../externals/surfconext-connect.md) | Lists service tokens | HTTPS |
