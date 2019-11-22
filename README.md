# MyConext
[![Build Status](https://travis-ci.org/oharsta/myconext.svg)](https://travis-ci.org/oharsta/myconext)
[![codecov.io](https://codecov.io/github/oharsta/myconext/coverage.svg)](https://codecov.io/github/oharsta/myconext)

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

There is no home page, you'll need to visit a SP and choose the Guest IdP to login.

### [Build](#build)

To deploy production bundles
```bash
mvn deploy
```
