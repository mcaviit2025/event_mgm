const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export class ApiError extends Error {
  constructor(message, validationErrors = {}) {
    super(message);
    this.name = "ApiError";
    this.validationErrors = validationErrors;
  }
}

export async function apiRequest(path, { token, method = "GET", body } = {}) {
  const headers = new Headers({ Accept: "application/json" });
  if (body !== undefined) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(
      "Could not reach the event API. Check that the Spring Boot server is running.",
    );
  }

  const text = await response.text();
  let payload;
  try {
    payload = text ? JSON.parse(text) : {};
  } catch {
    payload = { message: text };
  }

  if (!response.ok) {
    if (response.status === 401 && token) {
      sessionStorage.removeItem("event-admin-token");
      window.dispatchEvent(new Event("event-admin-auth-expired"));
    }
    throw new ApiError(
      payload.message || `Request failed (${response.status})`,
      payload.validationErrors || {},
    );
  }

  return payload.data;
}
