# MyConext
[![JAVA CI](https://github.com/OpenConext/OpenConext-myconext/actions/workflows/actions.yml/badge.svg)](https://github.com/OpenConext/OpenConext-myconext/actions/workflows/actions.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

An IdP for OpenConext. A user can create and manage his own identity. Authentication uses a magic-link by default, and FIDO2 or a password can be added later.

## [Getting started](#getting-started)

### [System Requirements](#system-requirements)

- Java 11
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

The myconext client is build with Svelte and to get initially started:

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

There is no home page, you'll need to visit an SP and choose eduID to login.

### [Build](#build)

To deploy production bundles
```bash
mvn deploy
```
### [Mail](#mail)

The default mail configuration sends mails to port 1025. Install https://mailpit.axllent.org/ and capture all emails send. 
You can see all mails delivered at http://0.0.0.0:8025/ when mailpit is installed.
```bash
brew install mailpit
```

### [Crypto](#crypto)

The myconext application uses a private RSA key and corresponding certificate to sign the SAML requests. We don't want
to provide defaults, so in the integration tests the key / certificate pair is generated on the fly. if you want to
deploy the application in an environment where the certificate needs to be registered with the Service Provider (Proxy)
then you can generate a key pair with the following commands:
```
cd myconext/myconext-server/src/main/resources
openssl genrsa -traditional -out myconext.pem 2048
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
### [Translations](translations)

The github actions will generate new translations of the source is changed.

```
cd account-gui
yarn localicious render ../myconext-server/src/main/resources/localizations.yaml ./src/locale/ --languages en,nl --outputTypes js -c SHARED
rm -fr ./src/locale/js/Localizable.ts
cd myconext-gui
yarn localicious render ../myconext-server/src/main/resources/localizations.yaml ./src/locale/ --languages en,nl --outputTypes js -c SHARED
rm -fr ./src/locale/js/Localizable.ts
```

### [Miscellaneous](#miscellaneous)

To get an overview of the git source file's:
```
cloc --read-lang-def=cloc_definitions.txt --vcs=git
```

### [Migration](#migration)

It's possible to migrate from an existing IdP to this IdP. A new identity will be created, and the eppn wil be copied.

### [Attribute Manipulation](#attribute-manipulation)
```
curl -u oidcng:secret "http://login.test2.eduid.nl/myconext/api/attribute-manipulation?sp_entity_id=https://test.okke&uid=0eaa7fb2-4f94-476f-b3f6-c8dfc4115a87&sp_institution_guid=null"
```

### [Attribute Aggregation](#attribute-aggregation)
```
curl -u aa:secret "https://login.test2.eduid.nl/myconext/api/attribute-aggregation?sp_entity_id=https://mijn.test2.eduid.nl/shibboleth&eduperson_principal_name=j.doe@example.com"
```
Endpoint to detect duplicate eduID's for SP's that have the same institutionGuid
```
curl -u aa:secret 'https://login.test2.eduid.nl/myconext/api/system/eduid-duplicates' | jq .
```

### OpenAPI Documentation

http://localhost:8081/myconext/api/swagger-ui/index.html

http://localhost:8081/myconext/api/api-docs

https://login.test2.eduid.nl/myconext/api/swagger-ui/index.html


https://login.test2.eduid.nl/myconext/api/api-docs

### IDIN & e-Herkenning

The redirect URI's for local development have to start with https. You can use the reverse proxy of ngrok for this. For example:
```
ngrok http --domain okke.harsta.eu.ngrok.io 8081
```

### Running the IdP and testing localhost

The [idp_metadata.xml](idp_metadata.xml) file contains the IdP metadata for localhost development. Import an IdP in Manage and
whitelist this for the SP's you want to test with. The OIDC-Playground is capable of testing the different ACR options.