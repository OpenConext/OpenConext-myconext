---
type: C4 Component
title: eduID app API
status: stable
groma:
  id: tiqr
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/TiqrController.java
      symbol: TiqrController
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/DeactivateRequest.java
      symbol: DeactivateRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/EnrollmentVerificationKey.java
      symbol: EnrollmentVerificationKey
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/FinishEnrollment.java
      symbol: FinishEnrollment
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/GeneratedBackupCode.java
      symbol: GeneratedBackupCode
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/ManualResponse.java
      symbol: ManualResponse
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/PhoneCode.java
      symbol: PhoneCode
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/PhoneVerification.java
      symbol: PhoneVerification
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/PollAuthenticationResult.java
      symbol: PollAuthenticationResult
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/RateLimitEnforcer.java
      symbol: RateLimitEnforcer
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/StartAuthentication.java
      symbol: StartAuthentication
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/StartEnrollment.java
      symbol: StartEnrollment
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/SURFSecureID.java
      symbol: SURFSecureID
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/TiqrConfiguration.java
      symbol: TiqrConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/TiqrRequest.java
      symbol: TiqrRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/tiqr/VerifyPhoneCode.java
      symbol: VerifyPhoneCode
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/EnrollmentRepository.java
      symbol: EnrollmentRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/RegistrationRepository.java
      symbol: RegistrationRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/cron/TiqrCleaner.java
      symbol: TiqrCleaner
  group: Sign-in
description: Tiqr enrollment and sign-in for the eduID mobile app.
---

Enrolls the app, verifies a phone number, issues backup codes, and starts or polls an app authentication, including step-up. The eduID app posts the Tiqr enrollment and authentication results here. Phone codes go out through SMS, and expired Tiqr sessions are removed on a schedule.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/tiqr/TiqrController.java](../../../../../../myconext-server/src/main/java/myconext/tiqr/TiqrController.java) | [myconext-server/src/main/java/myconext/sms/SMSService.java](../../../../../../myconext-server/src/main/java/myconext/sms/SMSService.java) | Sends a phone code | Java |
