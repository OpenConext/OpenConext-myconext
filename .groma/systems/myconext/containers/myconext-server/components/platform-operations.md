---
type: C4 Component
title: Platform operations
status: stable
groma:
  id: platform-operations
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/SystemController.java
      symbol: SystemController
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ServiceMigration.java
      symbol: ServiceMigration
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/MetricsController.java
      symbol: MetricsController
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/MetricsRepository.java
      symbol: MetricsRepository
  group: Service APIs
description: Internal reports for duplicate eduIDs, service moves, and usage counts.
---

Exposes the duplicate-eduID report and the dry-run or live migration of a service to another institution. It also stores the counts behind the metrics endpoint. Callers use the internal API account.
