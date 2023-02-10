# MyConext
[![Build Status](https://travis-ci.org/OpenConext/OpenConext-myconext.svg)](https://travis-ci.org/OpenConext/OpenConext-myconext)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-myconext/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-myconext)

An IdP for OpenConext. A user can create and manage his own identity. Authentication uses a magic-link by default, and FIDO2 or a password can be added later.

## [Getting started](#getting-started)

### [System Requirements](#system-requirements)

- Java 8
- Maven 3
- MongoDB 3.4.x
- Yarn 1.x
- NodeJS
- Ansible

## [Building and running](#building-and-running)

### [The myconext-server](#myconext-server)

This project uses Spring Boot and Maven. To run locally, type:

`cd myconext-server`

`mvn spring-boot:run -Dspring-boot.run.profiles=dev`

When developing, it's convenient to just execute the applications main-method, which is in [Application](myconext-server/src/main/java/myconext/MyConextServerApplication.java).
Don't forget to set the active profile to dev.

### [The account-gui](#myconext-gui)

The myconext client is build with Svetle and to get initially started:

```
cd account-gui
yarn install
yarn dev
```

Browse to the [application homepage](http://localhost:3001/).

### [The myconext-gui](#myconext-gui)

The IdP is also build with Svelte and to get initially started:

```
cd myconext-gui
yarn install
yarn start
```

There is no home page, you'll need to visit a SP and choose eduID to login.

### [Build](#build)

To deploy production bundles
```bash
mvn deploy
```
### [Crypto](#crypto)

The myconext application uses a private RSA key and corresponding certificate to sign the SAML requests. We don't want
to provide defaults, so in the integration tests the key / certificate pair is generated on the fly. if you want to
deploy the application in an environment where the certificate needs to be registered with the Service Provider (Proxy)
then you can generate a key pair with the following commands:
```
cd myconext/myconext-server/src/main/resources
openssl genrsa  -out myconext.pem 2048
openssl req -subj '/O=Organization, CN=OIDC/' -key myconext.pem -new -x509 -days 365 -out myconext.crt
```
Add the key pair to the [application.yml](myconext-server/src/main/resources/application.yml) file:
```
private_key_path: classpath:/myconext.pem
certificate_path: classpath:/myconext.crt
```
If you need to register the public key in EB then issue this command and copy & paste it in Manage for the correct IdP:
```
cat myconext.crt |ghead -n -1 |tail -n +2 | tr -d '\n'; echo
```
### [Miscellaneous](#miscellaneous)

To get an overview of the git source file's:
```
cloc --read-lang-def=cloc_definitions.txt --vcs=git
```

### [Migration](#migration)

It's possible to migrate from an existing IdP to this IdP. A new identity will be created, and the eppn wil be copied.

### [Attribute Manipulation](#attribue-manipulation)
```
curl -u oidcng:secret "http://login.test2.eduid.nl/myconext/api/attribute-manipulation?sp_entity_id=https://test.okke&uid=0eaa7fb2-4f94-476f-b3f6-c8dfc4115a87&sp_institution_guid=null"
```

### [Attribute Aggregation](#attribue-aggregation)
```
curl -u aa:secret "https://login.test2.eduid.nl/myconext/api/attribute-aggregation?sp_entity_id=https://mijn.test2.eduid.nl/shibboleth&eduperson_principal_name=j.doe@example.com"
```
Endpoint to detect duplicate eduID's for SP's that have the same institutionGuid
```
curl -u aa:secret 'https://login.test2.eduid.nl/myconext/api/system/eduid-duplicates' | jq .
```

### OpenAPI Documentation

http://localhost:8081/swagger-ui/index.html
http://localhost:8081/v3/api-docs.yaml

