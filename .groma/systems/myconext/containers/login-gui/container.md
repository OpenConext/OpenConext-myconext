---
type: C4 Container
title: Login GUI
status: draft
groma:
  id: login-gui
  parent: myconext
  technology: React, TypeScript, Vite
description: Early React replacement for the login screens.
---

React Router application that loads `/config` from the eduID server and renders a placeholder login shell. It is not a Maven module and it is not the login application that ships today. The Svelte login screens are the deployed identity-provider UI. The TypeScript scanner does not declare a container for this app, so this container stays a draft.
