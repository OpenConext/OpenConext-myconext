---
type: C4 Component
title: HTTP security
status: stable
groma:
  id: http-security
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/SecurityConfiguration.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/FirewallConfiguration.java
      symbol: FirewallConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/ServicesConfiguration.java
      symbol: ServicesConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/ExternalApiConfiguration.java
      symbol: ExternalApiConfiguration
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/FeatureProperties.java
      symbol: FeatureProperties
    - scanner: java
      file: myconext-server/src/main/java/myconext/session/SessionConfig.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/EmailDomainGuard.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/EmailGuessingPrevention.java
      symbol: EmailGuessingPrevention
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/RateLimitedException.java
      symbol: RateLimitedException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/TooManyRequestsException.java
      symbol: TooManyRequestsException
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/HealthResponseLoggingFilter.java
      symbol: HealthResponseLoggingFilter
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/ExtendedInMemoryUserDetailsManager.java
      symbol: ExtendedInMemoryUserDetailsManager
    - scanner: java
      file: myconext-server/src/main/java/myconext/log/MDCContext.java
      symbol: MDCContext
    - scanner: java
      file: myconext-server/src/main/java/myconext/log/MDCFilter.java
      symbol: MDCFilter
  group: Runtime
description: Decides who may call the server and how sessions are held.
---

Configures Spring Security for the browser session, the basic-auth API accounts, and the OIDC resource server. It also holds feature flags, the email-domain allow list, the delay that slows email guessing, and request logging context.
