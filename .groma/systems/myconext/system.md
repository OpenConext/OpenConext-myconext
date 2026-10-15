---
type: C4 System
title: eduID
status: stable
groma:
  id: myconext
description: Identity provider where a person creates and manages their own eduID.
---

eduID is the OpenConext guest identity provider. A person registers once, then signs in to service providers with a magic link, a password, a passkey, or the eduID app. The same account can be linked to an institution, strengthened with iDIN or an in-person check, and managed from My eduID. The running system is one Spring Boot server and four web applications. Two React applications in this repository are early replacements and are not part of the Maven build that ships the Svelte login and My eduID apps.
