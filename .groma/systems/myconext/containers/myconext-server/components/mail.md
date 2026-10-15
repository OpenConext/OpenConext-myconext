---
type: C4 Component
title: Mail
status: stable
groma:
  id: mail
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/mail/MailBox.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/mail/MailConfiguration.java
      symbol: MailConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/EmailsSend.java
      symbol: EmailsSend
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/EmailsSendRepository.java
      symbol: EmailsSendRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/InactivityMail.java
      symbol: InactivityMail
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/NudgeAppMail.java
      symbol: NudgeAppMail
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/InstitutionMailUsage.java
      symbol: InstitutionMailUsage
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/DisposableEmailProviders.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/DisposableEmailProviderException.java
      symbol: DisposableEmailProviderException
  group: Runtime
description: Sends sign-in codes and account notices.
---

Renders the Dutch and English templates for magic links, email changes, password resets, app nudges, inactivity warnings, and institution-link notices, then hands them to SMTP. Scheduled jobs also mail inactive people and people who have not installed the app. Disposable-email domains are refreshed for registration checks.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/mail/MailBox.java](../../../../../../myconext-server/src/main/java/myconext/mail/MailBox.java) | [mail-server](../../../../../externals/mail-server.md) | Delivers messages | SMTP |
