import { useState } from "react";
import { apiRequest } from "../../services/api.js";
import { Alert } from "../../shared/components/Alert.jsx";
import { formatError } from "../../utils/eventFormatters.js";

export function LookupManagementPage({ token, kind, items, loading, onRefresh, onNotice, onEdit, onAdd }) {
  const isCategory = kind === "category";
  const label = isCategory ? "category" : "event type";
  const endpoint = isCategory ? "/v1/admin/categories" : "/v1/admin/event-types";
  const [error, setError] = useState("");
  const [workingId, setWorkingId] = useState("");

  async function toggleActive(item) {
    setWorkingId(String(item.id));
    setError("");
    try {
      const action = item.isActive === false ? "activate" : "deactivate";
      await apiRequest(`${endpoint}/${item.id}/${action}`, { token, method: "PATCH" });
      onNotice(`${isCategory ? "Category" : "Event type"} ${action}d.`);
      await onRefresh();
    } catch (requestError) {
      setError(formatError(requestError));
    } finally {
      setWorkingId("");
    }
  }

  async function deleteItem(item) {
    if (!window.confirm(`Permanently delete "${item.name}"?`)) return;
    setWorkingId(String(item.id));
    setError("");
    try {
      await apiRequest(`${endpoint}/${item.id}`, { token, method: "DELETE" });
      onNotice(`${isCategory ? "Category" : "Event type"} deleted.`);
      await onRefresh();
    } catch (requestError) {
      setError(formatError(requestError));
    } finally {
      setWorkingId("");
    }
  }

  return (
    <>
      <section className="page-heading lookup-management-heading">
        <div>
          <div className="eyebrow">EVENT CLASSIFICATION</div>
          <h1>{isCategory ? "Categories" : "Event types"}</h1>
          <p>View, add, edit, activate, deactivate, or delete {label} options.</p>
        </div>
        <button className="button button-primary" type="button" onClick={() => onAdd(kind)}>
          <span aria-hidden="true">＋</span> Add {label}
        </button>
      </section>

      {error && <Alert onDismiss={() => setError("")}>{error}</Alert>}

      <section className="events-panel lookup-management-panel">
        <div className="panel-heading">
          <div>
            <h2>All {isCategory ? "categories" : "event types"} <span className="count-pill">{items.length}</span></h2>
            <p>Delete is available when the option is not referenced by an event. Otherwise deactivate it.</p>
          </div>
        </div>
        {loading && items.length === 0 ? (
          <div className="lookup-empty-state">Loading {isCategory ? "categories" : "event types"}…</div>
        ) : items.length === 0 ? (
          <div className="lookup-empty-state">
            <strong>No {isCategory ? "categories" : "event types"} yet</strong>
            <span>Add the first option to use it when creating an event.</span>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="events-table lookup-management-table">
              <thead>
                <tr><th>NAME</th><th>DESCRIPTION</th><th>STATUS</th><th aria-label="Actions" /></tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td><strong>{item.name}</strong></td>
                    <td className="lookup-description">{item.description || "—"}</td>
                    <td>
                      <span className={`lookup-status ${item.isActive === false ? "inactive" : "active"}`}>
                        {item.isActive === false ? "Inactive" : "Active"}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions lookup-row-actions">
                        <button className="text-action" type="button" disabled={workingId !== ""} onClick={() => onEdit(item, kind)}>Edit</button>
                        <button className="text-action lookup-toggle-action" type="button" disabled={workingId !== ""} onClick={() => toggleActive(item)}>
                          {workingId === String(item.id) ? "Saving…" : item.isActive === false ? "Activate" : "Deactivate"}
                        </button>
                        <button className="text-action lookup-delete-action" type="button" disabled={workingId !== ""} onClick={() => deleteItem(item)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
