---
type: C4 Component
title: Desk shell
status: stable
groma:
  id: desk-shell
  parent: servicedesk-gui
  code:
    - scanner: javascript
      file: servicedesk-gui/src/App.jsx
      symbol: App
    - scanner: javascript
      file: servicedesk-gui/src/components/Flash.jsx
      symbol: Flash
    - scanner: javascript
      file: servicedesk-gui/src/main.jsx
    - scanner: javascript
      file: servicedesk-gui/src/components/LanguageSelector.jsx
      symbol: LanguageSelector
    - scanner: javascript
      file: servicedesk-gui/src/pages/Forbidden.jsx
      symbol: Forbidden
    - scanner: javascript
      file: servicedesk-gui/src/locale/I18n.js
    - scanner: javascript
      file: servicedesk-gui/src/components/Header.jsx
      symbol: Header
    - scanner: javascript
      file: servicedesk-gui/src/components/UserMenu.jsx
      symbol: UserMenu
    - scanner: javascript
      file: servicedesk-gui/src/components/Tab.jsx
      symbol: Tab
    - scanner: javascript
      file: servicedesk-gui/src/pages/NotFound.jsx
      symbol: NotFound
    - scanner: javascript
      file: servicedesk-gui/src/components/Tabs.jsx
      symbol: Tabs
    - scanner: javascript
      file: servicedesk-gui/src/components/UnitHeader.jsx
      symbol: UnitHeader
    - scanner: javascript
      file: servicedesk-gui/src/components/BreadCrumb.jsx
      symbol: BreadCrumb
    - scanner: javascript
      file: servicedesk-gui/src/pages/RefreshRoute.jsx
      symbol: RefreshRoute
    - scanner: javascript
      file: servicedesk-gui/src/components/Page.jsx
      symbol: Page
    - scanner: javascript
      file: servicedesk-gui/src/utils/QueryParameters.js
    - scanner: javascript
      file: servicedesk-gui/src/api/index.js
    - scanner: javascript
      file: servicedesk-gui/src/utils/Utils.js
    - scanner: javascript
      file: servicedesk-gui/src/locale/en.js
    - scanner: javascript
      file: servicedesk-gui/src/components/Footer.jsx
      symbol: Footer
    - scanner: javascript
      file: servicedesk-gui/src/stores/AppStore.js
    - scanner: javascript
      file: servicedesk-gui/src/locale/nl.js
  group: Desk screens
description: Frame, language, and API client for the service desk.
---

Provides the header, tabs, and signed-in menu, and calls the service-desk API. Forbidden, missing, and refresh routes are part of this shell.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [servicedesk-gui/src/api/index.js](../../../../../../servicedesk-gui/src/api/index.js) | [myconext-server/src/main/java/myconext/api/ServiceDeskController.java](../../../../../../myconext-server/src/main/java/myconext/api/ServiceDeskController.java) | Reviews the case | HTTPS |
