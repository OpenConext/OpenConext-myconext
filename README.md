# MyConext
[![Build Status](https://travis-ci.org/OpenConext/OpenConext-myconext.svg)](https://travis-ci.org/OpenConext/OpenConext-myconext)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-myconext/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-myconext)

Me environment for OpenConext

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

`cd manage-server`

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

When developing, it's convenient to just execute the applications main-method, which is in [Application](myconext-server/src/main/java/myconext/MyConextServerApplication.java).
Don't forget to set the active profile to dev.

### [The account-gui](#myconext-gui)

The myconext client is build with Svetle and to get initially started:

```
cd manage-gui
yarn install
yarn dev
```

Browse to the [application homepage](http://localhost:3001/).

### [The myconext-gui](#myconext-gui)

The Guest-IdP is also build with Svelte and to get initially started:

```
cd manage-gui
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

### [Miscellaneous](#miscellaneous)

To get an overview of the git source file's:
```
cloc --read-lang-def=cloc_definitions.txt --vcs=git
```

### [Migration](#migration)

[Migrate your account](https://login.test2.eduid.nl/migration)
