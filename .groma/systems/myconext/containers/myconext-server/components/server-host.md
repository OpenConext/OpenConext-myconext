---
type: C4 Component
title: Server host
status: stable
groma:
  id: server-host
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/MyConextServerApplication.java
      symbol: MyConextServerApplication
    - scanner: java
      file: myconext-server/src/main/java/myconext/SwaggerOpenIdConfig.java
      symbol: SwaggerOpenIdConfig
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/ExtendedMongoHealthIndicator.java
      symbol: ExtendedMongoHealthIndicator
  group: Runtime
description: Starts the identity provider and publishes its API document.
---

The Spring Boot entry point. It also configures the OpenAPI UI and adds a MongoDB health check.
