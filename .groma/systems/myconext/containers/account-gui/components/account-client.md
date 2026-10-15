---
type: C4 Component
title: Login client
status: stable
groma:
  id: account-client
  parent: account-gui
  code:
    - scanner: javascript
      file: account-gui/src/main.js
    - scanner: javascript
      file: account-gui/src/constants/enrollmentStatus.js
    - scanner: javascript
      file: account-gui/src/constants/AlertType.js
    - scanner: javascript
      file: account-gui/src/locale/parser.js
      symbol: parse
    - scanner: javascript
      file: account-gui/src/constants/authenticationStatus.js
    - scanner: javascript
      file: account-gui/src/constants/loginStatus.js
    - scanner: javascript
      file: account-gui/src/locale/en.js
    - scanner: javascript
      file: account-gui/src/locale/nl.js
    - scanner: javascript
      file: account-gui/src/utils/services.js
    - scanner: javascript
      file: account-gui/src/utils/utils.js
    - scanner: javascript
      file: account-gui/src/api/index.js
    - scanner: javascript
      file: account-gui/src/constants/regexp.js
    - scanner: javascript
      file: account-gui/src/utils/poll.js
    - scanner: javascript
      file: account-gui/src/constants/cookieNames.js
    - scanner: javascript
      file: account-gui/src/stores/domains.js
    - scanner: javascript
      file: account-gui/src/utils/sso.js
      symbol: proceed
    - scanner: javascript
      file: account-gui/src/utils/date.js
      symbol: dateFromEpoch
    - scanner: javascript
      file: account-gui/src/stores/user.js
    - scanner: javascript
      file: account-gui/src/utils/format.js
      symbol: capitalize
    - scanner: javascript
      file: account-gui/src/verify/banks.js
      symbol: logo
    - scanner: javascript
      file: account-gui/src/constants/loginPreferences.js
    - scanner: javascript
      file: account-gui/src/locale/I18n.js
    - scanner: javascript
      file: account-gui/src/utils/constants.js
    - scanner: javascript
      file: account-gui/src/stores/conf.js
description: Browser calls, stores, and copy for the login screens.
---

Calls the account and Tiqr APIs, keeps the current user and configuration, and supplies Dutch and English text. The Svelte routes that render each login step are not part of this scanned component.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [account-gui/src/api/index.js](../../../../../../account-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/api/UserController.java](../../../../../../myconext-server/src/main/java/myconext/api/UserController.java) | Requests sign-in | HTTPS |
| [account-gui/src/api/index.js](../../../../../../account-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/tiqr/TiqrController.java](../../../../../../myconext-server/src/main/java/myconext/tiqr/TiqrController.java) | Starts app sign-in | HTTPS |
