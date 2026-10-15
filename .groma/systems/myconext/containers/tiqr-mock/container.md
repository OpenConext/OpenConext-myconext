---
type: C4 Container
title: Tiqr stand-in
status: stable
groma:
  id: tiqr-mock
  parent: myconext
  technology: Java, Spring Boot
description: Local stand-in for the eduID app during Tiqr development.
---

A small Spring Boot program that plays the eduID app's side of Tiqr enrollment and authentication against a local eduID server. It is development tooling, not the mobile app and not part of the deployed identity provider. Scanner exclusion does not drop the component the first scan already recorded.
