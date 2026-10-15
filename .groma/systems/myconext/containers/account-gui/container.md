---
type: C4 Container
title: Login screens
status: stable
groma:
  id: account-gui
  parent: myconext
  technology: Svelte, Vite
description: Screens for registering an eduID and signing in.
---

The Svelte application a service provider redirects to when someone chooses eduID. It runs magic-link, password, passkey, and eduID-app sign-in, and the linking and step-up screens that interrupt that sign-in. It has no standalone home page. The route screens are Svelte files, which the enabled scanners do not read; the mapped client is the JavaScript those screens use to call the server.
