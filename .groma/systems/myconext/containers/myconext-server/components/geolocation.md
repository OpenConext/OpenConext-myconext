---
type: C4 Component
title: Login location
status: stable
groma:
  id: geolocation
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/geo/GeoLocation.java
      symbol: GeoLocation
    - scanner: java
      file: myconext-server/src/main/java/myconext/geo/MaxMindGeoLocation.java
      symbol: MaxMindGeoLocation
    - scanner: java
      file: myconext-server/src/main/java/myconext/geo/MockGeoLocation.java
      symbol: MockGeoLocation
  group: Runtime
description: Resolves a sign-in IP address to a place name.
---

Guest IdP login asks for a location when it records the sign-in. Production can use a MaxMind GeoLite2 city database downloaded with the configured license; local development uses the mock.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/geo/MaxMindGeoLocation.java](../../../../../../myconext-server/src/main/java/myconext/geo/MaxMindGeoLocation.java) | [maxmind](../../../../../externals/maxmind.md) | Downloads city data | HTTPS |
