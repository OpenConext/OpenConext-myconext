---
type: C4 Component
title: My eduID client
status: stable
groma:
  id: my-eduid-client
  parent: myconext-gui
  code:
    - scanner: javascript
      file: myconext-gui/src/main.js
    - scanner: javascript
      file: myconext-gui/src/locale/nl.js
    - scanner: javascript
      file: myconext-gui/src/locale/en.js
    - scanner: javascript
      file: myconext-gui/src/stores/domains.js
    - scanner: javascript
      file: myconext-gui/src/format/date.js
    - scanner: javascript
      file: myconext-gui/src/api/index.js
    - scanner: javascript
      file: myconext-gui/src/locale/I18n.js
    - scanner: javascript
      file: myconext-gui/src/constants/loginStatus.js
    - scanner: javascript
      file: myconext-gui/src/stores/user.js
      symbol: createFlash
    - scanner: javascript
      file: myconext-gui/src/constants/authenticationStatus.js
    - scanner: javascript
      file: myconext-gui/src/validation/regexp.js
    - scanner: javascript
      file: myconext-gui/src/constants/enrollmentStatus.js
    - scanner: javascript
      file: myconext-gui/src/utils/utils.js
    - scanner: javascript
      file: myconext-gui/src/verify/banks.js
      symbol: logo
    - scanner: javascript
      file: myconext-gui/src/utils/date.js
    - scanner: javascript
      file: myconext-gui/src/utils/poll.js
    - scanner: javascript
      file: myconext-gui/src/utils/services.js
description: Browser calls, stores, and copy for My eduID.
---

Calls the account, linking, and Tiqr APIs after sign-in, and keeps the signed-in user. The Svelte pages for profile, security, services, and app enrollment are not part of this scanned component.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-gui/src/api/index.js](../../../../../../myconext-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/api/UserController.java](../../../../../../myconext-server/src/main/java/myconext/api/UserController.java) | Manages the account | HTTPS |
| [myconext-gui/src/api/index.js](../../../../../../myconext-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/tiqr/TiqrController.java](../../../../../../myconext-server/src/main/java/myconext/tiqr/TiqrController.java) | Manages the app | HTTPS |
| [myconext-gui/src/api/index.js](../../../../../../myconext-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/api/AccountLinkerController.java](../../../../../../myconext-server/src/main/java/myconext/api/AccountLinkerController.java) | Links an institution | HTTPS |
