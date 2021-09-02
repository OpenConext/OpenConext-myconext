# SAML migration

# Intro
See https://docs.spring.io/spring-security/site/docs/5.2.1.RELEASE/reference/htmlsingle/#saml2login-spring-security-saml2-history

# Implementation
1. Listens for SAML requests, e.g. Spring-boot filter, and parse / validate authnRequest
2. Present authentication pages to users the IdP manages.
3. Extract attributes for the authenticated user.
4. Server code to translate user attributes to SAML attributes and send to the SP.

# Libraries
https://mvnrepository.com/artifact/org.opensaml/opensaml-core/4.0.1
https://mvnrepository.com/artifact/org.opensaml/opensaml-saml-impl

# Reuse - inspiration
https://github.com/spring-projects/spring-security-saml/tree/develop

