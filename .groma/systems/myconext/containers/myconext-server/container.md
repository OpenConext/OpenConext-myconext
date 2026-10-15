---
type: C4 Container
title: eduID server
status: stable
groma:
  id: myconext-server
  parent: myconext
  technology: Java 21, Spring Boot
description: Spring Boot identity provider and account API.
---

Handles SAML sign-in, the account and service-desk APIs, Tiqr enrollment, and the partner APIs used by SURFconext, Studielink, and eduBadges. Accounts and sign-in requests are stored in MongoDB. SAML responses are signed with the configured RSA key.
