---
type: C4 Component
title: eduID record
status: stable
groma:
  id: user-record
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/User.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/EduID.java
      symbol: EduID
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/LinkedAccount.java
      symbol: LinkedAccount
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ExternalLinkedAccount.java
      symbol: ExternalLinkedAccount
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ProvisionedLinkedAccount.java
      symbol: ProvisionedLinkedAccount
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/Verification.java
      symbol: Verification
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/LoginOptions.java
      symbol: LoginOptions
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/PublicKeyCredentials.java
      symbol: PublicKeyCredentials
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UserLogin.java
      symbol: UserLogin
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UserInactivity.java
      symbol: UserInactivity
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/UserRepository.java
      symbol: UserRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/UserLoginRepository.java
      symbol: UserLoginRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/UserBeforeSaveCallBack.java
      symbol: UserBeforeSaveCallBack
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/MongoMapping.java
      symbol: MongoMapping
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/MongoConversions.java
      symbol: MongoConversions
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/DateConverter.java
      symbol: DateConverter
    - scanner: java
      file: myconext-server/src/main/java/myconext/mongo/Migrations.java
      symbol: Migrations
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/IdpScoping.java
      symbol: IdpScoping
  group: Account
description: Stored shape of a person and the repository that loads it.
---

The MongoDB documents for the account: eduID values, institution and external links, verification, passkeys, and login history, plus the Spring Data repositories and Mongo mapping used to save them. Sign-in, self-service, and the partner APIs all read this record rather than keeping their own copy.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/repository/UserRepository.java](../../../../../../myconext-server/src/main/java/myconext/repository/UserRepository.java) | [mongodb](../../../../../externals/mongodb.md) | Stores accounts | MongoDB |
