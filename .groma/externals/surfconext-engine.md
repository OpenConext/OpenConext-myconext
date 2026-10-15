---
type: C4 System
title: SURFconext engine
status: stable
groma:
  id: surfconext-engine
  technology: SAML, HTTPS
description: Proxy that requests eduID sign-in and attributes.
---

The SURFconext proxy that sends people to this identity provider and receives the SAML assertion. It also calls attribute aggregation and attribute manipulation when a service provider needs eduID attributes.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [surfconext-engine](surfconext-engine.md) | [myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java](../../myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java) | Requests sign-in | SAML |
| [surfconext-engine](surfconext-engine.md) | [myconext-server/src/main/java/myconext/aa/AttributeAggregatorController.java](../../myconext-server/src/main/java/myconext/aa/AttributeAggregatorController.java) | Exchanges attributes | HTTPS |
