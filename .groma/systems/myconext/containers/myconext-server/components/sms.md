---
type: C4 Component
title: SMS
status: stable
groma:
  id: sms
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/sms/SMSService.java
      symbol: SMSService
    - scanner: java
      file: myconext-server/src/main/java/myconext/sms/SMSServiceImpl.java
      symbol: SMSServiceImpl
    - scanner: java
      file: myconext-server/src/main/java/myconext/sms/SMSServiceMock.java
      symbol: SMSServiceMock
    - scanner: java
      file: myconext-server/src/main/java/myconext/sms/SMSConfiguration.java
      symbol: SMSConfiguration
  group: Sign-in
description: Sends the phone code used while enrolling the eduID app.
---

The app API asks this service to text a verification code. Production uses the configured SMS gateway; tests use the mock implementation.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/sms/SMSServiceImpl.java](../../../../../../myconext-server/src/main/java/myconext/sms/SMSServiceImpl.java) | [sms-gateway](../../../../../externals/sms-gateway.md) | Delivers the SMS | HTTPS |
