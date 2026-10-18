package com.erp.desktop.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;

/**
 * Thin HTTP client for the Quarkus core.
 *
 * <p>Every call blocks on a virtual worker thread; the caller is responsible for
 * running it off the JavaFX Application Thread (see {@code Ui.async}).
 */
public class ApiClient {

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(3);

    /** Raised for any non-2xx response, carrying the status so callers can react. */
    public static class ApiCallException extends RuntimeException {
        public final int status;
        public final String friendlyMessage;

        ApiCallException(int status, String friendlyMessage) {
            super(friendlyMessage);
            this.status = status;
            this.friendlyMessage = friendlyMessage;
        }
    }

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private volatile String bearerToken;
    private volatile String username = "";
    private volatile String fullName = "";
    private volatile Long companyId;
    private volatile String companyName = "";
    private volatile List<String> roles = List.of();
    private volatile boolean mustChangePassword;
    private volatile boolean demo;
    private volatile Consumer<String> sessionExpiredHandler = message -> { };

    public ApiClient(int port) {
        this("http://127.0.0.1:" + port);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String username() {
        return username;
    }

    public String fullName() {
        return fullName;
    }

    public Long companyId() {
        return companyId;
    }

    public String companyName() {
        return companyName;
    }

    public List<String> roles() {
        return roles;
    }

    /** True while the portfolio demo session is active. */
    public boolean isDemo() {
        return demo;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public boolean hasSession() {
        return bearerToken != null;
    }

    public void setSessionExpiredHandler(Consumer<String> handler) {
        this.sessionExpiredHandler = handler == null ? message -> { } : handler;
    }

    /** Drops every trace of the session so a demo exit cannot leak into a normal sign-in. */
    public void clearSession() {
        bearerToken = null;
        username = "";
        fullName = "";
        companyId = null;
        companyName = "";
        roles = List.of();
        mustChangePassword = false;
        demo = false;
    }

    public boolean isUp() {
        try {
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(uri("/api/status"))
                    .timeout(PROBE_TIMEOUT).GET().build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ sessions

    public JsonNode login(String username, String password) {
        JsonNode body = post("/api/auth/login", JSON.createObjectNode()
                .put("username", username)
                .put("password", password), false);
        adopt(body);
        return body;
    }

    /** Whether this deployment offers the portfolio demo. */
    public boolean demoAvailable() {
        try {
            JsonNode body = get("/api/demo/status", false);
            return body.path("enabled").asBoolean(false);
        } catch (ApiCallException e) {
            // A 404 simply means demo mode is not compiled in for this deployment.
            return false;
        }
    }

    /** Opens the demo session: no credentials, isolated tenant. */
    public JsonNode startDemo() {
        JsonNode body = post("/api/demo/session", JSON.createObjectNode(), false);
        adopt(body);
        return body;
    }

    /**
 * Restores the demo dataset to its documented starting point.
 *
 * <p>Reset deletes the demo tenant and rebuilds it, which produces a new company id.
 * The previous token is scoped to the old one and would see nothing, so a fresh demo
 * session is opened here and adopted transparently - the reviewer stays in the demo
 * without noticing the tenant was recreated.
     */
    public JsonNode resetDemo() {
        JsonNode summary = post("/api/demo/reset", JSON.createObjectNode(), true);
        startDemo();
        return summary;
    }

    /** Asks the backend to acknowledge the exit, then clears local session state. */
    public void exitDemo() {
        try {
            delete("/api/demo/session");
        } catch (RuntimeException e) {
            // Tokens are stateless; a failed acknowledgement must not trap the user
            // in a demo session they are trying to leave.
        } finally {
            clearSession();
        }
    }

    // ------------------------------------------------------------------ generic calls

    public JsonNode get(String path) {
        return get(path, true);
    }

    public JsonNode get(String path, boolean auth) {
        return parse(execute(builder(path, auth).GET().build()));
    }

    public JsonNode post(String path, JsonNode body) {
        return post(path, body, true);
    }

    public JsonNode post(String path, JsonNode body, boolean auth) {
        return parse(execute(builder(path, auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build()));
    }

    public JsonNode create(String path, JsonNode body) {
        return post(path, body);
    }

    public JsonNode update(String path, String id, JsonNode body) {
        return put(path + "/" + id, body);
    }

    public JsonNode put(String path, JsonNode body) {
        return parse(execute(builder(path, true)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build()));
    }

    public void delete(String path) {
        execute(builder(path, true).DELETE().build());
    }

    public JsonNode meta(String path) {
        return get(path + "/meta", true);
    }

    public JsonNode list(String path, String search, int page, int size, String sort, boolean desc) {
        return list(path, search, page, size, sort, desc, Map.of());
    }

    public JsonNode list(String path, String search, int page, int size, String sort, boolean desc,
                         Map<String, String> filters) {
        StringBuilder url = new StringBuilder(path).append("?page=").append(page).append("&size=").append(size);
        if (search != null && !search.isBlank()) url.append("&q=").append(enc(search.trim()));
        if (sort != null && !sort.isBlank()) url.append("&sort=").append(enc(sort)).append("&desc=").append(desc);
        appendFilters(url, filters);
        return get(url.toString(), true);
    }

    public String exportCsv(String path, String search) {
        return exportCsv(path, search, "id", true, Map.of());
    }

    public String exportCsv(String path, String search, String sort, boolean desc, Map<String, String> filters) {
        StringBuilder url = new StringBuilder(path).append("/export.csv?desc=").append(desc);
        if (search != null && !search.isBlank()) url.append("&q=").append(enc(search.trim()));
        if (sort != null && !sort.isBlank()) url.append("&sort=").append(enc(sort));
        appendFilters(url, filters);
        return execute(builder(url.toString(), true).setHeader("Accept", "text/csv").GET().build());
    }

    private static void appendFilters(StringBuilder url, Map<String, String> filters) {
        filters.forEach((name, value) -> {
            if (value != null && !value.isBlank()) url.append("&f.").append(enc(name)).append('=').append(enc(value));
        });
    }

    public JsonNode importCsv(String path, String csv) {
        return parse(execute(builder(path + "/import", true)
                .header("Content-Type", "text/csv")
                .POST(HttpRequest.BodyPublishers.ofString(csv, StandardCharsets.UTF_8))
                .build()));
    }

    public JsonNode businessAction(String actionPath) {
        return post(actionPath, JSON.createObjectNode(), true);
    }

    /** Replaces the current account password. */
    public void changePassword(String currentPassword, String newPassword) {
        var body = JSON.createObjectNode();
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);
        post("/api/auth/password", body);
    }

    public JsonNode overview() {
        return get("/api/overview", true);
    }

    /** Parses a response body, never leaking a Jackson exception at the caller. */
    private static JsonNode parse(String body) {
        try {
            return JSON.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (JsonProcessingException e) {
            throw new ApiCallException(500, "The server returned a response the app could not read.");
        }
    }

    // ------------------------------------------------------------------ internals

    private void adopt(JsonNode body) {
        String token = body.path("token").asText("");
        if (token.isBlank()) {
            throw new ApiCallException(500, "The server did not return a session token.");
        }
        bearerToken = token;
        username = body.path("username").asText("");
        fullName = body.path("fullName").asText("");
        companyId = body.path("companyId").isNumber() ? body.path("companyId").asLong() : null;
        companyName = body.path("companyName").asText("");
        List<String> parsed = new ArrayList<>();
        body.path("roles").forEach(node -> parsed.add(node.asText()));
        roles = List.copyOf(parsed);
        mustChangePassword = body.path("mustChangePassword").asBoolean(false);
        demo = body.path("demo").asBoolean(false);
    }

    private HttpRequest.Builder builder(String path, boolean auth) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .timeout(TIMEOUT)
                .header("Accept", "application/json");
        if (auth) {
            String token = bearerToken;
            if (token == null || token.isBlank()) {
                // Fail here rather than sending an anonymous request that would come
                // back as a confusing 401 from the server.
                throw new ApiCallException(401, "Your session has ended. Sign in again to continue.");
            }
            request.header("Authorization", "Bearer " + token);
        }
        return request;
    }

    private URI uri(String path) {
        return URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path));
    }

    private String execute(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401 && request.headers().firstValue("Authorization").isPresent()) {
                // The token was rejected (expired or revoked). Announce it once so the
                // shell can return to the sign-in screen instead of leaving every
                // screen silently failing.
                String message = "Your session has expired. Sign in again to continue.";
                if (request.headers().firstValue("Authorization").orElse("").equals("Bearer " + bearerToken)) {
                    clearSession();
                    onFxThread(() -> sessionExpiredHandler.accept(message));
                }
                throw new ApiCallException(401, message);
            }
            if (response.statusCode() >= 400) {
                throw new ApiCallException(response.statusCode(), friendly(response));
            }
            return response.body() == null ? "" : response.body();
        } catch (IOException e) {
            throw new ApiCallException(0, "Cannot reach the ERP service at " + baseUrl + ". Is it still running?");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiCallException(0, "The request was interrupted.");
        }
    }

    private static void onFxThread(Runnable action) {
        try {
            if (Platform.isFxApplicationThread()) {
                action.run();
            } else {
                Platform.runLater(action);
            }
        } catch (IllegalStateException e) {
            // Toolkit already shut down; nothing left to update.
        }
    }

    private String friendly(HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        try {
            JsonNode error = JSON.readTree(body);
            if (error.hasNonNull("message")) return error.get("message").asText();
            if (error.hasNonNull("title")) return error.get("title").asText();
            if (error.hasNonNull("error")) return error.get("error").asText();
        } catch (Exception ignored) {
            // Not JSON - fall through to the status-based message.
        }
        return switch (response.statusCode()) {
            case 400 -> "The server rejected the request as invalid.";
            case 401 -> "Your session is missing or has expired.";
            case 403 -> "You do not have permission for that action.";
            case 404 -> "That record or page no longer exists.";
            case 409 -> "Someone else changed this record. Reload and try again.";
            case 422 -> "The server could not process that change.";
            case 500 -> "The server hit an unexpected error. Check the backend log.";
            default -> "Request failed (HTTP " + response.statusCode() + ").";
        };
    }

    private static String enc(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
