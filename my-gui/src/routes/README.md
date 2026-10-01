# File-system-based routing

Folders represent the structure of the urls in the frontend. Every folder is a path and needs to contain a `route.tsx`.
The ones with a prefix `_auth` are the protected routes. This includes `_auth._index.tsx` for the home page (or `/`) and the `_auth.$.tsx` for a catch-all (404 page).

When a protected path is visited in the browser, the logic inside `_auth.tsx` gets executed to determine if the user is allowed to visit te protected pages.

More info: https://reactrouter.com/how-to/file-route-conventions
