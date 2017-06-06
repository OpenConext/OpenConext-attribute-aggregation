import spinner from "../lib/spin";

const apiPath = "/aa/api/internal/";
let csrfToken = null;

function apiUrl(path) {
    return apiPath + path;
}

function validateResponse(showErrorDialog) {
    return res => {
        spinner.stop();

        if (!res.ok) {
            if (res.type === "opaqueredirect") {
                setTimeout(() => window.location.reload(true), 100);
                return res;
            }
            const error = new Error(res.statusText);
            error.response = res;
            if (showErrorDialog) {
                setTimeout(() => {
                    const error = new Error(res.statusText);
                    error.response = res;
                    throw error;
                }, 100);
            }
            throw error;
        }
        csrfToken = res.headers.get("x-csrf-token");

        const sessionAlive = res.headers.get("x-session-alive");

        if (sessionAlive !== "true") {
            window.location.reload(true);
        }
        return res;

    };
}

function validFetch(path, options, headers = {}, showErrorDialog = true) {
    const contentHeaders = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": csrfToken,
        ...headers
    };

    const fetchOptions = Object.assign({}, {headers: contentHeaders}, options, {
        credentials: "same-origin",
        redirect: "manual"
    });
    spinner.start();
    return fetch(apiUrl(path), fetchOptions)
        .catch(err => {
            spinner.stop();
            throw err;
        })
        .then(validateResponse(showErrorDialog));
}

function fetchJson(path, options = {}, headers = {}, showErrorDialog = true) {
    return validFetch(path, options, headers, showErrorDialog)
        .then(res => res.json());
}

function postPutJson(path, body = {}, method) {
    const httpMethod = method || (body.id === undefined ? "post" : "put");
    return fetchJson(path, {method: httpMethod, body: JSON.stringify(body)});
}

function fetchDelete(path) {
    return validFetch(path, {method: "delete"});
}

//API
export function accounts() {
    return fetchJson("accounts");
}

export function authorityConfiguration() {
    return fetchJson("authorityConfiguration");
}

export function getUser() {
    return fetchJson("users/me", {}, {}, false);
}

export function reportError(error) {
    return postPutJson("error", error);
}

export function logOut() {
    return fetchDelete("users/logout");
}
