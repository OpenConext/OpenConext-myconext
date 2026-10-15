---
type: C4 Component
title: Manage client
status: stable
groma:
  id: manage-client
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/manage/Manage.java
      symbol: Manage
    - scanner: java
      file: myconext-server/src/main/java/myconext/manage/ManageConfiguration.java
      symbol: ManageConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/manage/MockManage.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/manage/RemoteManage.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ServiceProvider.java
      symbol: ServiceProvider
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/IdentityProvider.java
      symbol: IdentityProvider
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/RemoteProvider.java
      symbol: RemoteProvider
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/AllowedDomain.java
      symbol: AllowedDomain
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/IdentityProviderNotFoundException.java
      symbol: IdentityProviderNotFoundException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/ServiceProviderNotFoundException.java
      symbol: ServiceProviderNotFoundException
  group: Runtime
description: Reads service-provider and institution metadata from OpenConext Manage.
---

Caches the service providers and identity providers the server needs to name a relying party, find an institution by domain or BRIN, and scope account linking. A mock implementation replaces the remote client in local development. Manage is polled on a schedule when it is enabled.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/manage/RemoteManage.java](../../../../../../myconext-server/src/main/java/myconext/manage/RemoteManage.java) | [openconext-manage](../../../../../externals/openconext-manage.md) | Loads metadata | HTTPS |
