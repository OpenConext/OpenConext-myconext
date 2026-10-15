---
type: C4 Component
title: Tiqr client stand-in
status: stable
groma:
  id: tiqr-mock-app
  parent: tiqr-mock
  code:
    - scanner: java
      file: tiqr-mock/src/main/java/tiqr/org/TiqrApplication.java
      symbol: TiqrApplication
    - scanner: java
      file: tiqr-mock/src/main/java/tiqr/org/TiqrEndpoint.java
      symbol: TiqrEndpoint
description: Development endpoint that behaves like the eduID app.
---

Offers a local page and HTTP endpoint so a developer can complete Tiqr enrollment and authentication without the mobile app.
