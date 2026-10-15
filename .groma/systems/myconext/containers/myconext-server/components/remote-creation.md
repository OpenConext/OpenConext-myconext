---
type: C4 Component
title: Remote creation
status: stable
groma:
  id: remote-creation
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/RemoteCreationController.java
      symbol: RemoteCreationController
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/EduIDAssignedValue.java
      symbol: EduIDAssignedValue
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/EduIDInstitutionPseudonym.java
      symbol: EduIDInstitutionPseudonym
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/EduIDValue.java
      symbol: EduIDValue
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/NewExternalEduID.java
      symbol: NewExternalEduID
    - scanner: java
      file: myconext-server/src/main/java/myconext/remotecreation/UpdateExternalEduID.java
      symbol: UpdateExternalEduID
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/EmailExistsResponse.java
      symbol: EmailExistsResponse
  group: Service APIs
description: Lets a trusted institution client create and update eduIDs.
---

Studielink is the configured client. It can ask whether an email or eduID already exists, bind an institution pseudonym, and create, update, or delete the external eduID. Confirmation mail is sent when an account is created or validated.
