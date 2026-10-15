---
type: C4 Component
title: Attribute exchange
status: stable
groma:
  id: attribute-exchange
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/aa/AttributeAggregatorController.java
      symbol: AttributeAggregatorController
    - scanner: java
      file: myconext-server/src/main/java/myconext/aa/UserAttribute.java
      symbol: UserAttribute
  group: Service APIs
description: Releases and adjusts eduID attributes for a service provider.
---

The SURFconext engine calls attribute aggregation to read what may be released for a person, and attribute manipulation to adjust that set for a service. Both endpoints are hidden from the public OpenAPI document and use the configured API accounts.
