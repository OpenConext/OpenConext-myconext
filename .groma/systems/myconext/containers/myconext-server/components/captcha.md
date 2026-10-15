---
type: C4 Component
title: Captcha check
status: stable
groma:
  id: captcha
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/captcha/CaptchaVerifier.java
      symbol: CaptchaVerifier
    - scanner: java
      file: myconext-server/src/main/java/myconext/captcha/ApiHeaderInterceptor.java
      symbol: ApiHeaderInterceptor
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/CaptchInvalidException.java
      symbol: CaptchInvalidException
  group: Sign-in
description: Checks a registration captcha when that feature is enabled.
---

The account API submits the browser response to the configured siteverify endpoint before creating an account. The feature flag can turn the check off.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/captcha/CaptchaVerifier.java](../../../../../../myconext-server/src/main/java/myconext/captcha/CaptchaVerifier.java) | [captcha-service](../../../../../externals/captcha-service.md) | Checks the response | HTTPS |
