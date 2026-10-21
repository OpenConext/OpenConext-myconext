# my-gui instructions (read this first in every session)

`my-gui` is the React + TypeScript rewrite of `../myconext-gui` (Svelte). Plan: `~/.claude/plans/prancy-stirring-knuth.md` (route map and steps).

## Stack

- ReactJS + Vite, yarn, SCSS (no Tailwind), TypeScript, `async`/`await`.
- File-based routing with React Router, see `src/routes/README.md`. `_auth*` routes are protected; `_auth.tsx` guards, `_auth._index.tsx` is home, `_auth.$.tsx` is the 404.
- Data fetching: TanStack Query. State management: Zustand.
- No backend changes, except introducing a model.
- Environment ribbon: the server serves CSS at `/myconext/api/disclaimer` (`body::after`); load it in `root.tsx`.
- Locales: `src/locale/{en,nl,pt}.ts` + `I18n.ts` (analogue to `login-gui/src/locale`). **No hardcoded strings in React**: use `I18n.t('key')`; add every key to en, nl and pt (pt is a copy of en for now).
- Limit code in `useEffect`; at most one `useEffect` per component.

## UI: `@surfnet/curve-react`

- Use curve-react components for everything. Source: `../DesignSystem/packages/react/src/components/ui/*`, variants in `../DesignSystem/packages/contracts`, Storybook: https://surfnet.github.io/DesignSystem/react/.
- Use the library's tokens (CSS variables such as `var(--muted)`, `var(--primary)`, `--radius-*`, `--spacing`), the same ones Figma uses. Never hard-code colours, spacing or radii. Dark mode: `html.dark`; colour themes: `html.theme-*`.
- Icons: `@phosphor-icons/react`.
- If no curve component exists, build on curve tokens, but always double check (search DesignSystem and Storybook) first. If in doubt whether a new component is needed, **ask the user**.
- Known custom components: `PinVerifier` (on curve `InputOTP`). curve has no Header, Footer, page layout or ribbon: build those from curve components (`NavigationMenu`, `DropdownMenu`, `Badge`) and tokens.
- SCSS mixins are out of scope for now.

## Hard rules

- **Never edit anything in `../DesignSystem`** (it is git-ignored and read-only reference).
- **Never commit.** The user commits.
- Run `yarn check` (tsc + lint + prettier) before finishing; `yarn dev` serves on :3001 and proxies to the backend on :8081.

## References

- Old app: `../myconext-gui` (routes in `src/App.svelte`, tabs in `src/routes/Home.svelte`, API in `src/api/index.js`, stores in `src/stores/user.js`, i18n in `src/locale`).
- Figma file key `ZAZOaq0W6q1nyQJIDwXplH` (eduID). Node ids: Home `10809:30836`; Identity `10483:39565`, `10497:7045`; Institutions `10497:8022`, `10510:9918`, `10510:10066`, `10486:46469`; Security `10510:9772`, `10525:13624`; Activity `10555:36345`, `10555:36555`, `10555:38094`.
- Design: 5 top-nav tabs (Home, Identity, Institutions, Security, Activity), language and user menu (Account, Logout) on the right. Details expand in place and destructive actions use Dialogs, so they are not routes.

## Route map (old → new)

| New path                                                                                                                                                                         | Old path(s)                                                                                                                                     |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `/`                                                                                                                                                                              | `/`, `/home`                                                                                                                                    |
| `/identity` (+ linking-error pages `/identity/eppn-already-linked`, `attribute-missing`, `subject-already-linked`, `external-account-linked-error`)                              | `/personal`, `/edit-name`, `/manage`, `/eppn-already-linked`, `/attribute-missing`, `/subject-already-linked`, `/external-account-linked-error` |
| `/institutions`                                                                                                                                                                  | linked accounts in `/personal` (`/personal?institution=` opens the new-institution dialog)                                                      |
| `/security` (dialogs: password, passkeys, eduID app, recovery method, email change; opened by query params `passwordHash`, `password`, `editEmail`, `credential`)                | `/security`, `/credential`, `/webauthn`, `/reset-password?h=`, `/add-password?h=`, `/reset-password-link`, `/edit-email`, tiqr wizard paths     |
| `/activity`                                                                                                                                                                      | `/data-activity`, `/services`, `/service`                                                                                                       |
| `/account`, `/account/delete`                                                                                                                                                    | redirect to `/`, the user menu holds download data and delete account                                                                           |
| `/update-email?h=`                                                                                                                                                               | `/update-email?h=`                                                                                                                              |
| public: `/landing`, `/install-app`, `/create-from-institution`, `/create-from-institution/link/:hash`, `/verify/:hash`, `/expired`, `/eppn-already-linked`, `/attribute-missing` | same                                                                                                                                            |

Routes that must not nest under a layout route use a trailing underscore on the parent segment (`create-from-institution_.link.$hash`, `_auth.identity_.eppn-already-linked`).

The backend, emails and IdP still link to old paths, so keep redirect routes (preserving the query string) for them.
