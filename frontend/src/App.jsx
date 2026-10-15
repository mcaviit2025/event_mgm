import { useCallback, useEffect, useMemo, useState } from "react";
import { ApiError, apiRequest } from "./api.js";

const emptyForm = () => {
  const start = new Date();
  start.setDate(start.getDate() + 1);
  start.setHours(18, 0, 0, 0);
  const deadline = new Date(start);
  deadline.setHours(deadline.getHours() - 3);

  return {
    eventName: "",
    venue: "",
    eventDateTime: toLocalInput(start),
    registrationDeadline: toLocalInput(deadline),
    description: "",
    eventFee: "0.00",
    maxParticipants: "50",
    maxParticipantsPerGroup: "1",
    registrationMode: "SOLO_ONLY",
    eventTypeId: "",
    categoryId: "",
  };
};

function toLocalInput(date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 16);
}

function toApiDateTime(value) {
  return value ? `${value}:00` : "";
}

function displayDate(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function displayAmount(value) {
  return Number(value || 0).toFixed(2);
}

function getFriendlyError(error) {
  if (error instanceof ApiError) return error.message;
  return "Something went wrong. Please try again.";
}

function App() {
  const [token, setToken] = useState(
    () => sessionStorage.getItem("event-admin-token") || "",
  );
  const [user, setUser] = useState(null);
  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [eventTypes, setEventTypes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState("");
  const [notice, setNotice] = useState("");
  const [query, setQuery] = useState("");
  const [eventModal, setEventModal] = useState(null);
  const [lookupModal, setLookupModal] = useState(null);

  const signOut = useCallback(async () => {
    const currentToken = sessionStorage.getItem("event-admin-token");
    let logoutError = "";
    if (currentToken) {
      try {
        await apiRequest("/v1/auth/logout", {
          method: "POST",
          token: currentToken,
        });
      } catch (error) {
        logoutError = getFriendlyError(error);
      }
    }
    sessionStorage.removeItem("event-admin-token");
    setToken("");
    setUser(null);
    setEvents([]);
    setCategories([]);
    setEventTypes([]);
    setPageError(logoutError ? `Signed out locally, but server logout failed: ${logoutError}` : "");
  }, []);

  useEffect(() => {
    const handleExpired = () => {
      setToken("");
      setUser(null);
      setPageError("Your session expired. Please sign in again.");
    };
    window.addEventListener("event-admin-auth-expired", handleExpired);
    return () =>
      window.removeEventListener("event-admin-auth-expired", handleExpired);
  }, []);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setPageError("");
    try {
      const [nextEvents, nextCategories, nextTypes] = await Promise.all([
        apiRequest("/v1/admin/events", { token }),
        apiRequest("/v1/admin/categories", { token }),
        apiRequest("/v1/admin/event-types", { token }),
      ]);
      setEvents(nextEvents || []);
      setCategories((nextCategories || []).filter((item) => item.isActive));
      setEventTypes((nextTypes || []).filter((item) => item.isActive));
    } catch (error) {
      setPageError(getFriendlyError(error));
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token) loadDashboard();
  }, [token, loadDashboard]);

  const filteredEvents = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return events;
    return events.filter((event) =>
      [event.eventName, event.venue, event.category?.name, event.eventType?.name]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized)),
    );
  }, [events, query]);

  async function handleLogin(credentials) {
    const data = await apiRequest("/v1/auth/login", {
      method: "POST",
      body: credentials,
    });
    if (!data?.token) throw new ApiError("The login response did not include an access token.");
    sessionStorage.setItem("event-admin-token", data.token);
    setUser(data.user || null);
    setToken(data.token);
    setPageError("");
  }

  async function saveEvent(form) {
    const { id, ...values } = form;
    const requestBody = {
      ...values,
      eventDateTime: toApiDateTime(values.eventDateTime),
      registrationDeadline: toApiDateTime(values.registrationDeadline),
      eventFee: values.eventFee,
      maxParticipants: Number(values.maxParticipants),
      maxParticipantsPerGroup: Number(values.maxParticipantsPerGroup),
      eventTypeId: Number(values.eventTypeId),
      categoryId: Number(values.categoryId),
    };

    await apiRequest(id ? `/v1/admin/events/${id}` : "/v1/admin/events", {
      token,
      method: id ? "PUT" : "POST",
      body: requestBody,
    });
    setEventModal(null);
    setNotice(id ? "Event changes saved." : "Event created successfully.");
    await loadDashboard();
  }

  async function saveLookup(form) {
    const endpoint =
      lookupModal.kind === "category"
        ? "/v1/admin/categories"
        : "/v1/admin/event-types";
    await apiRequest(endpoint, {
      token,
      method: "POST",
      body: form,
    });
    setLookupModal(null);
    setNotice(
      lookupModal.kind === "category"
        ? "Category added."
        : "Event type added.",
    );
    await loadDashboard();
  }

  async function deleteEvent(event) {
    if (!window.confirm(`Delete “${event.eventName}”? This cannot be undone.`)) return;
    try {
      await apiRequest(`/v1/admin/events/${event.id}`, {
        token,
        method: "DELETE",
      });
      setNotice("Event deleted.");
      await loadDashboard();
    } catch (error) {
      setPageError(getFriendlyError(error));
    }
  }

  if (!token) {
    return (
      <LoginPage
        onLogin={handleLogin}
        initialError={pageError}
        clearError={() => setPageError("")}
      />
    );
  }

  const registrationCount = events.reduce(
    (sum, event) => sum + (event.currentParticipants || 0),
    0,
  );

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" aria-label="Gather home">
          <span className="brand-mark">g</span>
          <span>gather<span className="brand-dot">.</span></span>
        </a>
        <div className="workspace-label">WORKSPACE</div>
        <nav className="side-nav" aria-label="Main navigation">
          <a className="nav-item active" href="#events">
            <span className="nav-symbol">▦</span> Events
          </a>
          <a className="nav-item" href="#event-types" onClick={(event) => {
            event.preventDefault();
            setLookupModal({ kind: "event-type" });
          }}>
            <span className="nav-symbol">◇</span> Event types
          </a>
          <a className="nav-item" href="#categories" onClick={(event) => {
            event.preventDefault();
            setLookupModal({ kind: "category" });
          }}>
            <span className="nav-symbol">⌘</span> Categories
          </a>
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card">
            <span className="help-icon">?</span>
            <div><strong>Need a hand?</strong><span>Explore the API docs</span></div>
            <a href="/api/swagger-ui.html" target="_blank" rel="noreferrer" aria-label="Open API docs">↗</a>
          </div>
          <button className="profile-button" onClick={signOut}>
            <span className="avatar">{(user?.fullName || user?.username || "A").slice(0, 1).toUpperCase()}</span>
            <span className="profile-copy"><strong>{user?.fullName || user?.username || "Admin"}</strong><small>Administrator</small></span>
            <span className="profile-menu">↗</span>
          </button>
        </div>
      </aside>

      <main className="main-content" id="events">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><span className="crumb-separator">/</span><strong>Events</strong></div>
          <div className="topbar-actions">
            <span className="today-label">{new Intl.DateTimeFormat(undefined, { dateStyle: "full" }).format(new Date())}</span>
            <button className="icon-button" title="Refresh events" onClick={loadDashboard} disabled={loading}>⟳</button>
            <button className="button button-primary button-small" onClick={() => setEventModal({})}>
              <span>＋</span> Create event
            </button>
          </div>
        </header>

        <section className="page-heading">
          <div>
            <div className="eyebrow">EVENT MANAGEMENT</div>
            <h1>Your events</h1>
            <p>Plan, publish, and keep track of every gathering.</p>
          </div>
          <button className="button button-primary create-mobile" onClick={() => setEventModal({})}>
            <span>＋</span> Create event
          </button>
        </section>

        {pageError && <div className="alert alert-error" role="alert"><span>!</span>{pageError}<button onClick={() => setPageError("")} aria-label="Dismiss error">×</button></div>}
        {notice && <div className="alert alert-success" role="status"><span>✓</span>{notice}<button onClick={() => setNotice("")} aria-label="Dismiss message">×</button></div>}

        <section className="stats-grid" aria-label="Event statistics">
          <StatCard label="Active events" value={events.length} icon="◷" tone="violet" detail="Currently published" />
          <StatCard label="Total registrations" value={registrationCount} icon="♙" tone="blue" detail="Across your events" />
          <StatCard label="Categories" value={categories.length} icon="⌘" tone="orange" detail="Available to assign" />
          <StatCard label="Event types" value={eventTypes.length} icon="◇" tone="green" detail="Available to assign" />
        </section>

        <section className="events-panel">
          <div className="panel-heading">
            <div>
              <h2>All events <span className="count-pill">{events.length}</span></h2>
              <p>A complete overview of your upcoming and past events.</p>
            </div>
            <label className="search-field">
              <span aria-hidden="true">⌕</span>
              <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search events" aria-label="Search events" />
              <kbd>⌘ K</kbd>
            </label>
          </div>
          <div className="table-scroll">
            <table className="events-table">
              <thead><tr><th>EVENT</th><th>DATE &amp; TIME</th><th>CATEGORY</th><th>FEE</th><th>REGISTRATIONS</th><th>STATUS</th><th aria-label="Actions"></th></tr></thead>
              <tbody>
                {loading && !events.length ? (
                  <tr><td colSpan="7" className="table-state"><span className="spinner" />Loading events…</td></tr>
                ) : filteredEvents.length ? filteredEvents.map((event) => (
                  <tr key={event.id}>
                    <td><div className="event-cell"><span className="event-avatar">{(event.eventName || "E").slice(0, 1).toUpperCase()}</span><span><strong>{event.eventName}</strong><small>{event.venue}</small></span></div></td>
                    <td className="date-cell">{displayDate(event.eventDateTime)}</td>
                    <td><span className="category-chip">{event.category?.name || "Uncategorized"}</span></td>
                    <td><span className="fee-cell">{Number(event.eventFee || 0) === 0 ? "Free" : displayAmount(event.eventFee)}</span></td>
                    <td><span className="registration-cell">{event.currentParticipants || 0}<span> / {event.maxParticipants}</span></span></td>
                    <td><StatusBadge event={event} /></td>
                    <td><div className="row-actions"><button className="text-action" onClick={() => setEventModal(event)}>Edit</button><button className="more-button" onClick={() => deleteEvent(event)} title="Delete event" aria-label={`Delete ${event.eventName}`}>···</button></div></td>
                  </tr>
                )) : (
                  <tr><td colSpan="7" className="table-state">
                    <div className="empty-state">
                      <span className="empty-icon">✳</span>
                      <strong>{query ? "No matching events" : "Your first event starts here"}</strong>
                      <span>{query ? "Try another name, venue, or category." : "Create an event and it will appear in this list."}</span>
                      {!query && <button className="button button-primary button-small" onClick={() => setEventModal({})}>＋ Create event</button>}
                    </div>
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
          <div className="panel-footer"><span>Showing <strong>{filteredEvents.length}</strong> of <strong>{events.length}</strong> events</span><span className="footer-live"><span /> Synced with your workspace</span></div>
        </section>
        <footer className="page-footer"><span>Gather Event Management</span><span>Built for better gatherings <span className="footer-heart">♥</span></span></footer>
      </main>

      {eventModal && (
        <EventModal
          event={eventModal.id ? eventModal : null}
          categories={categories}
          eventTypes={eventTypes}
          onClose={() => setEventModal(null)}
          onSave={saveEvent}
          onCreateLookup={(kind) => setLookupModal({ kind })}
        />
      )}
      {lookupModal && (
        <LookupModal
          kind={lookupModal.kind}
          onClose={() => setLookupModal(null)}
          onSave={saveLookup}
        />
      )}
    </div>
  );
}

function StatCard({ label, value, icon, tone, detail }) {
  return (
    <article className="stat-card">
      <div className={`stat-icon ${tone}`}>{icon}</div>
      <div className="stat-content"><span>{label}</span><strong>{value}</strong><small>{detail}</small></div>
      <span className="stat-decoration">↗</span>
    </article>
  );
}

function StatusBadge({ event }) {
  const status = event.status || "OPEN";
  const tone = status === "OPEN" && event.isRegistrationOpen ? "open" : status.toLowerCase();
  const label = status === "OPEN" && event.isRegistrationOpen ? "Open" : status.toLowerCase().replaceAll("_", " ");
  return <span className={`status-badge ${tone}`}><span />{label}</span>;
}

function LoginPage({ onLogin, initialError, clearError }) {
  const [credentials, setCredentials] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => setError(initialError), [initialError]);

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    clearError();
    try {
      await onLogin(credentials);
    } catch (loginError) {
      setError(getFriendlyError(loginError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login-shell">
      <div className="login-art">
        <a className="brand brand-light" href="#"><span className="brand-mark">g</span><span>gather<span className="brand-dot">.</span></span></a>
        <div className="art-copy"><div className="eyebrow">MAKE IT HAPPEN</div><h1>Good things<br />happen when<br /><em>we gather.</em></h1><p>Your events, thoughtfully organized in one place.</p></div>
        <div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" /><div className="art-orbit orbit-three" />
        <div className="art-note"><span>✳</span><div><strong>Make room for moments</strong><small>Everything begins with an idea.</small></div></div>
        <span className="art-footer">EVENTS WORTH SHOWING UP FOR</span>
      </div>
      <section className="login-content">
        <div className="login-mobile-brand"><span className="brand-mark">g</span><span>gather<span className="brand-dot">.</span></span></div>
        <div className="login-form-wrap">
          <div className="login-kicker"><span className="status-dot" /> ADMIN WORKSPACE</div>
          <h2>Welcome back</h2>
          <p className="login-subtitle">Sign in to manage your events and community.</p>
          {error && <div className="alert alert-error login-error" role="alert"><span>!</span>{error}</div>}
          <form className="login-form" onSubmit={submit}>
            <label>Username<input autoComplete="username" required value={credentials.username} onChange={(event) => setCredentials({ ...credentials, username: event.target.value })} placeholder="Enter your username" /></label>
            <label>Password<input type="password" autoComplete="current-password" required value={credentials.password} onChange={(event) => setCredentials({ ...credentials, password: event.target.value })} placeholder="Enter your password" /></label>
            <button className="button button-primary login-submit" type="submit" disabled={submitting}>{submitting ? "Signing in…" : "Sign in"}<span>→</span></button>
          </form>
          <div className="login-security"><span>▣</span> Secure sign in powered by your event management API</div>
        </div>
        <div className="login-bottom"><span>© {new Date().getFullYear()} Gather</span><span>Made for the moments that matter.</span></div>
      </section>
    </main>
  );
}

function EventModal({ event, categories, eventTypes, onClose, onSave, onCreateLookup }) {
  const [form, setForm] = useState(() => event ? {
    id: event.id,
    eventName: event.eventName || "",
    venue: event.venue || "",
    eventDateTime: (event.eventDateTime || "").slice(0, 16),
    registrationDeadline: (event.registrationDeadline || "").slice(0, 16),
    description: event.description || "",
    eventFee: displayAmount(event.eventFee),
    maxParticipants: String(event.maxParticipants ?? 50),
    maxParticipantsPerGroup: String(event.maxParticipantsPerGroup ?? 1),
    registrationMode: event.registrationMode || "SOLO_ONLY",
    eventTypeId: String(event.eventType?.id || ""),
    categoryId: String(event.category?.id || ""),
  } : emptyForm());
  const [validationErrors, setValidationErrors] = useState({});
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function update(name, value) {
    setForm((current) => {
      const next = { ...current, [name]: value };
      if (name === "registrationMode") {
        if (value === "SOLO_ONLY") next.maxParticipantsPerGroup = "1";
        if (value === "GROUP_ONLY" && Number(current.maxParticipantsPerGroup) < 2) next.maxParticipantsPerGroup = "2";
      }
      return next;
    });
    setValidationErrors((current) => ({ ...current, [name]: undefined }));
  }

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setValidationErrors({});
    try {
      await onSave(form);
    } catch (saveError) {
      setError(
        Object.keys(saveError.validationErrors || {}).length
          ? "Please correct the highlighted validation errors."
          : getFriendlyError(saveError),
      );
      setValidationErrors(saveError.validationErrors || {});
    } finally {
      setSubmitting(false);
    }
  }

  const fieldError = (name) => validationErrors[name] && <small className="field-error">{validationErrors[name]}</small>;
  const dateMinimum = toLocalInput(new Date());

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className="modal-card event-modal" role="dialog" aria-modal="true" aria-labelledby="event-modal-title">
        <header className="modal-header"><div><div className="eyebrow">{event ? "EVENT DETAILS" : "NEW EVENT"}</div><h2 id="event-modal-title">{event ? "Edit event" : "Create an event"}</h2><p>Give your gathering the details it needs to come to life.</p></div><button className="close-button" onClick={onClose} aria-label="Close">×</button></header>
        {error && <div className="alert alert-error modal-error" role="alert"><span>!</span>{error}</div>}
        {Object.keys(validationErrors).length > 0 && (
          <ul className="validation-summary">
            {Object.entries(validationErrors).map(([field, message]) => (
              <li key={field}>{message}</li>
            ))}
          </ul>
        )}
        <form onSubmit={submit} className="event-form">
          <div className="form-section-title"><span>01</span> The essentials</div>
          <div className="form-grid">
            <label className="field-wide">Event name<input required minLength="3" maxLength="100" value={form.eventName} onChange={(event) => update("eventName", event.target.value)} placeholder="e.g. Design community meetup" />{fieldError("eventName")}</label>
            <label className="field-wide">Description<textarea required minLength="10" maxLength="1000" rows="3" value={form.description} onChange={(event) => update("description", event.target.value)} placeholder="Tell people what makes this event special…" /> <small className="field-hint">{form.description.length}/1000 characters</small>{fieldError("description")}</label>
            <label>Venue<input required minLength="3" maxLength="100" value={form.venue} onChange={(event) => update("venue", event.target.value)} placeholder="Venue or location" />{fieldError("venue")}</label>
            <label>Event fee <span className="label-hint">Amount · 0 is free</span><div className="input-prefix"><span>¤</span><input required type="number" min="0" max="99999999.99" step="0.01" value={form.eventFee} onChange={(event) => update("eventFee", event.target.value)} /></div>{fieldError("eventFee")}</label>
          </div>
          <div className="form-section-title"><span>02</span> Schedule</div>
          <div className="form-grid">
            <label>Event date &amp; time<input required type="datetime-local" min={dateMinimum} value={form.eventDateTime} onChange={(event) => update("eventDateTime", event.target.value)} />{fieldError("eventDateTime")}</label>
            <label>Registration deadline<input required type="datetime-local" min={dateMinimum} value={form.registrationDeadline} onChange={(event) => update("registrationDeadline", event.target.value)} />{fieldError("registrationDeadline")}</label>
            <p className="form-note field-wide">Registration must close at least 2 hours before the event begins.</p>
          </div>
          <div className="form-section-title"><span>03</span> Registration &amp; classification</div>
          <div className="form-grid">
            <label>Registration mode<select value={form.registrationMode} onChange={(event) => update("registrationMode", event.target.value)}><option value="SOLO_ONLY">Solo only</option><option value="GROUP_ONLY">Groups only</option><option value="BOTH">Solo &amp; groups</option></select></label>
            <label>Maximum participants<input required type="number" min="1" max="10000" value={form.maxParticipants} onChange={(event) => update("maxParticipants", event.target.value)} />{fieldError("maxParticipants")}</label>
            <label>Participants per group<input required type="number" min={form.registrationMode === "GROUP_ONLY" ? "2" : "1"} max="50" value={form.maxParticipantsPerGroup} onChange={(event) => update("maxParticipantsPerGroup", event.target.value)} disabled={form.registrationMode === "SOLO_ONLY"} />{fieldError("maxParticipantsPerGroup")}</label>
            <label>Event type<div className="select-action"><select required value={form.eventTypeId} onChange={(event) => update("eventTypeId", event.target.value)}><option value="">Select event type</option>{eventTypes.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}</select><button type="button" title="Add event type" onClick={() => onCreateLookup("event-type")}>＋</button></div>{eventTypes.length === 0 && <small className="field-hint">No active types yet. Add one using the + button.</small>}</label>
            <label>Category<div className="select-action"><select required value={form.categoryId} onChange={(event) => update("categoryId", event.target.value)}><option value="">Select category</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select><button type="button" title="Add category" onClick={() => onCreateLookup("category")}>＋</button></div>{categories.length === 0 && <small className="field-hint">No active categories yet. Add one using the + button.</small>}</label>
          </div>
          <footer className="modal-footer"><span className="required-note"><span>*</span> Required fields</span><div><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={submitting}>{submitting ? "Saving…" : event ? "Save changes" : "Create event"}<span>→</span></button></div></footer>
        </form>
      </section>
    </div>
  );
}

function LookupModal({ kind, onClose, onSave }) {
  const label = kind === "category" ? "category" : "event type";
  const [form, setForm] = useState({ name: "", description: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await onSave(form);
    } catch (saveError) {
      setError(getFriendlyError(saveError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop lookup-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className="modal-card lookup-modal" role="dialog" aria-modal="true" aria-labelledby="lookup-title">
        <header className="modal-header"><div><div className="eyebrow">CLASSIFICATION</div><h2 id="lookup-title">Add {label}</h2><p>Create a new option for your event.</p></div><button className="close-button" onClick={onClose} aria-label="Close">×</button></header>
        {error && <div className="alert alert-error modal-error" role="alert"><span>!</span>{error}</div>}
        <form className="lookup-form" onSubmit={submit}>
          <label>Name<input required minLength="2" maxLength="50" pattern="[a-zA-Z0-9 ]+" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} placeholder={`e.g. ${kind === "category" ? "Community" : "Workshop"}`} /></label>
          <label>Description <span className="label-hint">Optional</span><textarea maxLength="255" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} placeholder="A short description" /></label>
          <footer className="modal-footer"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={submitting}>{submitting ? "Adding…" : `Add ${label}`}</button></footer>
        </form>
      </section>
    </div>
  );
}

export default App;
