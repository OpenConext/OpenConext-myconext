//Internal API
function validateResponse(res) {

  if (!res.ok) {
    if (res.type === "opaqueredirect") {
      setTimeout(() => window.location.reload(), 100);
      return res;
    }
    throw res;
  }

  return res.json();
}

function validFetch(path, options) {
  const fetchOptions = {
    ...options,
    credentials: "same-origin",
    redirect: "manual",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json"
    }
  };
  return fetch(path, fetchOptions).then(res => validateResponse(res));
}

function fetchJson(path, options = {}) {
  return validFetch(path, options);
}

function postPutJson(path, body, method) {
  return fetchJson(path, { method, body: JSON.stringify(body) });
}

//Base
export function user() {
  return fetchJson("/surfid/api/user");
}

export function magicLink(body) {
  return postPutJson(`/surfid/api/`, body, "POST");
}

export function reportError(error) {
  return postPutJson("/oidc/api/error", error, "POST");
}
