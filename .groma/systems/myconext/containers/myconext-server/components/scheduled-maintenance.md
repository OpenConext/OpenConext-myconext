---
type: C4 Component
title: Expiry cleanup
status: stable
groma:
  id: scheduled-maintenance
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/ResourceCleaner.java
      symbol: ResourceCleaner
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/AbstractNodeLeader.java
      symbol: AbstractNodeLeader
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/Executable.java
      symbol: Executable
  group: Runtime
description: Deletes expired sign-in and linking data on one node.
---

Takes a database lock so only one server runs the job, then removes expired password and email hashes, unfinished registrations, stale link requests, old verification codes, and linked accounts that have passed their expiry.
