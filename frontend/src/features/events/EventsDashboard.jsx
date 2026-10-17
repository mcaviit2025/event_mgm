import { useCallback, useEffect, useMemo, useState } from "react";
import { apiRequest } from "../../services/api.js";
import { formatError, toApiDateTime } from "../../utils/eventFormatters.js";
import { Alert } from "../../shared/components/Alert.jsx";
import { StatCard } from "../../shared/components/StatCard.jsx";
import { AdminLayout } from "../../shared/layout/AdminLayout.jsx";
import { LookupModal } from "../lookups/LookupModal.jsx";
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
      setPageError(formatError(error));
    } finally {
      setLoading(false);
    }
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
    await apiRequest(endpoint, { token, method: "POST", body: form });
    setLookupModal(null);
    setNotice(lookupModal.kind === "category" ? "Category added." : "Event type added.");
    await loadDashboard();
  }

  async function deleteEvent(event) {
    if (!window.confirm(`Delete “${event.eventName}”? This cannot be undone.`)) return;
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
        onOpenLookup={(kind) => setLookupModal({ kind })}
        onRefresh={loadDashboard}
        loading={loading}
        headerAction={
          <button className="button button-primary button-small" type="button" onClick={() => setEventModal({})}>
            <span aria-hidden="true">＋</span> Create event
          </button>
        }
      >
        <section className="page-heading">
          <div>
            <div className="eyebrow">EVENT MANAGEMENT</div>
            <h1>Your events</h1>
            <p>Plan, publish, and keep track of every gathering.</p>
          </div>
          <button className="button button-primary create-mobile" type="button" onClick={() => setEventModal({})}>
            <span aria-hidden="true">＋</span> Create event
          </button>
        </section>

        {pageError && <Alert onDismiss={() => setPageError("")}>{pageError}</Alert>}
        {notice && <Alert variant="success" onDismiss={() => setNotice("")}>{notice}</Alert>}

        <section className="stats-grid" aria-label="Event statistics">
          <StatCard label="Active events" value={events.length} icon="◷" tone="violet" detail="Available in your workspace" />
          <StatCard label="Total registrations" value={registrationCount} icon="♙" tone="blue" detail="Across all events" />
          <StatCard label="Categories" value={categories.length} icon="⌘" tone="orange" detail="Available to assign" />
          <StatCard label="Event types" value={eventTypes.length} icon="◇" tone="green" detail="Available to assign" />
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
      </AdminLayout>

      {eventModal && (
        <EventFormModal
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
    </>
  );
}
