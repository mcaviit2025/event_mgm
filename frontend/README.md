# Event management admin UI

React + Vite admin interface for the Spring Boot event management API. The app source is organized by feature, shared UI, API services, utilities, and styles.

## Run locally

1. Start the backend using its usual Spring profile and database configuration. By default, it listens on `http://localhost:8080` with the `/api` context path.
2. From this `frontend` directory, run `npm install`, then `npm run dev`.
3. Open the local URL printed by Vite and sign in with an administrator account.

Vite proxies `/api` requests to `http://localhost:8080`. To use a different API base URL, set `VITE_API_BASE_URL` in a local `.env` file (for example, `VITE_API_BASE_URL=https://example.test/api`).

The Events screen supports event listing, search, create, edit, and delete. Event creation uses separate date and time inputs; changing either automatically sets the registration deadline to the same time on the previous day, and the deadline remains editable. The Categories and Event types screens list all options and support create, edit, activate/deactivate, and delete through the admin APIs. Options in use by events may be blocked from deletion by the backend and can instead be deactivated. Event fees accept non-negative amounts with at most two fractional digits; `0.00` means free. The backend stores and returns the fee but does not process payments or define a currency.

## Backend event fee

`eventFee` is accepted by `POST /api/v1/admin/events`, optionally updated with `PUT /api/v1/admin/events/{id}`, and returned in event responses. It must be non-negative, with at most two fractional digits.

## Project layout

```text
src/
  app/                 App entry and authentication state
  features/
    auth/              Administrator login
    events/            Dashboard, event table, and create/edit form
    lookups/           Category and event-type management
  services/            HTTP API client
  shared/
    components/        Reusable UI components
    layout/            Authenticated admin shell
  styles/              Global and responsive styles
  utils/               Date, amount, and error formatters
```

## Windows Rollup install recovery

If Vite reports that `@rollup/rollup-win32-x64-msvc` is missing or is not a valid Win32 application, its installed optional native package is corrupted or was installed for the wrong platform. Close any Vite process, then from the `frontend` directory run:

```powershell
Remove-Item -Recurse -Force node_modules
Remove-Item -Force package-lock.json
npm install --include=optional
npm run dev
```

Run the removal commands only from the `frontend` directory. This clears the frontend's generated dependencies and lockfile so npm can regenerate the Windows Rollup binary from the manifest.
