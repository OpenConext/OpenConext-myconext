---
type: C4 Component
title: Site shell
status: stable
groma:
  id: site-shell
  parent: public-gui
  code:
    - scanner: javascript
      file: public-gui/src/App.jsx
      symbol: App
    - scanner: javascript
      file: public-gui/src/components/LanguageSelector.jsx
      symbol: LanguageSelector
    - scanner: javascript
      file: public-gui/src/locale/I18n.js
    - scanner: javascript
      file: public-gui/src/components/CollapseField.jsx
      symbol: CollapseField
    - scanner: javascript
      file: public-gui/src/main.jsx
    - scanner: javascript
      file: public-gui/src/utils/QueryParameters.js
    - scanner: javascript
      file: public-gui/src/hooks/useFragmentOpen.js
      symbol: useFragmentOpen
    - scanner: javascript
      file: public-gui/src/stores/AppStore.js
    - scanner: javascript
      file: public-gui/src/components/Navigation.jsx
      symbol: Navigation
    - scanner: javascript
      file: public-gui/src/routes/MobileNavigation.jsx
      symbol: MobileNavigation
    - scanner: javascript
      file: public-gui/src/locale/nl.js
    - scanner: javascript
      file: public-gui/src/components/Header.jsx
      symbol: Header
    - scanner: javascript
      file: public-gui/src/locale/en.js
    - scanner: javascript
      file: public-gui/src/utils/Utils.js
    - scanner: javascript
      file: public-gui/src/components/Background.jsx
      symbol: Background
    - scanner: javascript
      file: public-gui/src/components/Footer.jsx
      symbol: Footer
    - scanner: javascript
      file: public-gui/src/components/AnchorLink.jsx
      symbol: AnchorLink
    - scanner: javascript
      file: public-gui/src/components/InfoLinkField.jsx
      symbol: InfoLinkField
description: Header, footer, navigation, and language for the public site.
---

Frames every public page, switches Dutch and English, and remembers the login and My eduID hosts for the current domain.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [public-gui/src/App.jsx](../../../../../../public-gui/src/App.jsx) | [account-gui/src/main.js](../../../../../../account-gui/src/main.js) | Links to sign-in | Browser |
| [public-gui/src/App.jsx](../../../../../../public-gui/src/App.jsx) | [myconext-gui/src/main.js](../../../../../../myconext-gui/src/main.js) | Links to My eduID | Browser |
