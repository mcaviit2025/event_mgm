# Event management admin UI

The React + Vite admin interface talks to the Spring Boot API under `/api`.

## Run locally

1. Start the backend using its usual Spring profile and database configuration. By default, it listens on `http://localhost:8080` and uses the `/api` context path.
2. In this directory, run `npm install`, then `npm run dev`.
3. Open the local URL printed by Vite and sign in with an administrator account.

Vite proxies `/api` requests to `http://localhost:8080`. To use a different API base URL, set `VITE_API_BASE_URL` in a local `.env` file (for example, `VITE_API_BASE_URL=https://example.test/api`).

The Events screen supports event listing, search, create, edit, and delete. Category and event-type options can be added from the event form. Event fees are decimal amounts with two fractional digits; `0.00` means free. The backend currently stores and returns the fee but does not process payments or define a currency.

## Backend event fee

`eventFee` is accepted by `POST /api/v1/admin/events`, optionally updated with `PUT /api/v1/admin/events/{id}`, and returned in event responses. It must be non-negative, with at most two fractional digits.
