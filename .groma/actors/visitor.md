---
type: C4 Actor
title: Visitor
status: stable
groma:
  id: visitor
description: Reader of the public eduID site.
---

Someone who has not signed in and is reading the public eduID site to learn what it is, how to install the app, or where to get help.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [visitor](visitor.md) | [public-gui/src/App.jsx](../../public-gui/src/App.jsx) | Reads the site | Browser |
