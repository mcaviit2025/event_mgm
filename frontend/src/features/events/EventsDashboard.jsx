import { useCallback, useEffect, useMemo, useState } from "react";
import { apiRequest } from "../../services/api.js";
import { formatError, toApiDateTime } from "../../utils/eventFormatters.js";
import { Alert } from "../../shared/components/Alert.jsx";
import { StatCard } from "../../shared/components/StatCard.jsx";
import { AdminLayout } from "../../shared/layout/AdminLayout.jsx";
import { LookupModal } from "../lookups/LookupModal.jsx";
import { LookupManagementPage } from "../lookups/LookupManagementPage.jsx";
import { EventFormModal } from "./components/EventFormModal.jsx";
import { EventsTable } from "./components/EventsTable.jsx";

export function EventsDashboard({ token, user, onSignOut }) {
  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [eventTypes, setEventTypes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState("");
  const [notice, setNotice] = useState("");
  const [query, setQuery] = useState("");
  const [eventModal, setEventModal] = useState(null);
  const [lookupModal, setLookupModal] = useState(null);
  const [activeSection, setActiveSection] = useState("events");

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setPageError("");
    const [eventsResult, categoriesResult, typesResult] = await Promise.allSettled([
      apiRequest("/v1/admin/events", { token }),
      apiRequest("/v1/admin/categories", { token }),
      apiRequest("/v1/admin/event-types", { token }),
    ]);

    const errors = [];
    if (eventsResult.status === "fulfilled") {
      setEvents(Array.isArray(eventsResult.value) ? eventsResult.value : []);
    } else {
      errors.push(`Events: ${formatError(eventsResult.reason)}`);
    }

    if (categoriesResult.status === "fulfilled") {
      setCategories(Array.isArray(categoriesResult.value) ? categoriesResult.value : []);
    } else {
      errors.push(`Categories: ${formatError(categoriesResult.reason)}`);
    }

    if (typesResult.status === "fulfilled") {
      setEventTypes(Array.isArray(typesResult.value) ? typesResult.value : []);
    } else {
      errors.push(`Event types: ${formatError(typesResult.reason)}`);
    }

    setPageError(errors.join(" "));
    setLoading(false);
  }, [token]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const filteredEvents = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) return events;
    return events.filter((event) =>
      [event.eventName, event.venue, event.category?.name, event.eventType?.name]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalizedQuery)),
    );
  }, [events, query]);
  const activeCategories = useMemo(() => normalizeOptions(categories), [categories]);
  const activeEventTypes = useMemo(() => normalizeOptions(eventTypes), [eventTypes]);

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
    const endpoint = lookupModal.kind === "category"
      ? "/v1/admin/categories"
      : "/v1/admin/event-types";
    const editing = Boolean(lookupModal.item?.id);
    const result = await apiRequest(
      editing ? `${endpoint}/${lookupModal.item.id}` : endpoint,
      { token, method: editing ? "PUT" : "POST", body: form },
    );
    if (!editing && !result?.id) {
      throw new Error("The API did not return the ID of the new option.");
    }
    setNotice(`${lookupModal.kind === "category" ? "Category" : "Event type"} ${editing ? "updated" : "added"}.`);
    await loadDashboard();
    return result;
  }

  async function deleteEvent(event) {
    const confirmed = window.confirm(
      `Delete "${event.eventName}" and all associated participant registrations, teammates, and payment proofs?\n\nThis permanently removes the event data and cannot be undone.`,
    );
    if (!confirmed) return;
    try {
      await apiRequest(`/v1/admin/events/${event.id}`, { token, method: "DELETE" });
      setNotice("Event deleted.");
      await loadDashboard();
    } catch (error) {
      setPageError(formatError(error));
    }
  }

  const registrationCount = events.reduce(
    (total, event) => total + (event.currentParticipants || 0),
    0,
  );

  return (
    <>
      <AdminLayout
        user={user}
        onSignOut={onSignOut}
        onRefresh={loadDashboard}
        loading={loading}
        activeSection={activeSection}
        onNavigate={setActiveSection}
        headerAction={activeSection === "events" ? (
          <button className="button button-primary button-small" type="button" onClick={() => setEventModal({})}>
            <span aria-hidden="true">＋</span> Create event
          </button>
        ) : null}
      >
        {pageError && <Alert onDismiss={() => setPageError("")}>{pageError}</Alert>}
        {notice && <Alert variant="success" onDismiss={() => setNotice("")}>{notice}</Alert>}
        {activeSection === "events" ? <><section className="page-heading">
          <div>
            <div className="eyebrow">EVENT MANAGEMENT</div>
            <h1>Your events</h1>
            <p>Plan, publish, and keep track of every gathering.</p>
          </div>
          <button className="button button-primary create-mobile" type="button" onClick={() => setEventModal({})}>
            <span aria-hidden="true">＋</span> Create event
          </button>
        </section>

        <section className="stats-grid" aria-label="Event statistics">
          <StatCard label="Active events" value={events.length} icon="◷" tone="violet" detail="Available in your workspace" />
          <StatCard label="Total registrations" value={registrationCount} icon="♙" tone="blue" detail="Across all events" />
          <StatCard label="Categories" value={activeCategories.length} icon="⌘" tone="orange" detail="Available to assign" />
          <StatCard label="Event types" value={activeEventTypes.length} icon="◇" tone="green" detail="Available to assign" />
        </section>

        <EventsTable
          events={events}
          filteredEvents={filteredEvents}
          query={query}
          onQueryChange={setQuery}
          loading={loading}
          onEdit={setEventModal}
          onDelete={deleteEvent}
          onCreate={() => setEventModal({})}
        />
        <footer className="page-footer">
          <span>Gather Event Management</span>
          <span>Built for better gatherings <span className="footer-heart">♥</span></span>
        </footer>
        </> : activeSection === "categories" || activeSection === "event-types" ? (
          <LookupManagementPage
            token={token}
            kind={activeSection === "categories" ? "category" : "event-type"}
            items={activeSection === "categories" ? categories : eventTypes}
            loading={loading}
            onRefresh={loadDashboard}
            onNotice={setNotice}
            onEdit={(item, kind) => setLookupModal({ kind, item })}
            onAdd={(kind) => setLookupModal({ kind })}
          />
        ) : null}
      </AdminLayout>

      {eventModal && (
        <EventFormModal
          event={eventModal.id ? eventModal : null}
          categories={activeCategories}
          eventTypes={activeEventTypes}
          onClose={() => setEventModal(null)}
          onSave={saveEvent}
          onCreateLookup={(kind, onCreated) => setLookupModal({ kind, onCreated })}
        />
      )}
      {lookupModal && (
        <LookupModal
          kind={lookupModal.kind}
          item={lookupModal.item}
          onClose={() => setLookupModal(null)}
          onSave={saveLookup}
          onCreated={lookupModal.onCreated}
        />
      )}
    </>
  );
}

function normalizeOptions(options) {
  if (!Array.isArray(options)) return [];
  return options.filter((item) => (item.isActive ?? item.active ?? true) !== false);
}
