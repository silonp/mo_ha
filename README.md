# Mid-Office Home Assignment (`mo_ha`)

A Spring Boot service that records **configuration changes**, makes them queryable
via a REST API, and asynchronously notifies a downstream system whenever a
**CRITICAL** change is recorded.

## What is a configuration change?

A configuration change describes a single mutation to a configuration entry —
who changed it, what changed, when, and why. It's defined by
[`ConfigChangeRequest`](src/main/java/com/ohpen/mo_ha/rest/dto/ConfigChangeRequest.java)
The service then assigns an `id` and a `timestamp` and stores it as an immutable record.

### Fields (per `ConfigChangeRequest`)

| Field           | Type                                                                             | Required                      |
|-----------------|----------------------------------------------------------------------------------|-------------------------------|
| `configKey`     | `String`                                                                         | yes                           |
| `type`          | [`ConfigChangeType`](src/main/java/com/ohpen/mo_ha/domain/ConfigChangeType.java) | yes                           |
| `severity`      | [`Severity`](src/main/java/com/ohpen/mo_ha/domain/Severity.java)                 | yes                           |
| `previousValue` | `String`                                                                         | depends on `type` (see below) |
| `newValue`      | `String`                                                                         | depends on `type` (see below) |
| `changedBy`     | `String`                                                                         | yes                           |
| `reason`        | `String`                                                                         | no                            |

### Type-specific restrictions

`type` further constrains which value fields are allowed. These are enforced **domain invariants**.

| `type`   | `previousValue`     | `newValue`          |
|----------|---------------------|---------------------|
| `CREATE` | **must be absent**  | **must be present** |
| `UPDATE` | **must be present** | **must be present** |
| `DELETE` | **must be present** | **must be absent**  |

## Persistence — in-memory store

The service uses an **in-memory database**
([`ConfigChangeInMemoryRepository`](src/main/java/com/ohpen/mo_ha/domain/db/ConfigChangeInMemoryRepository.java)),
backed by a `ConcurrentHashMap`. No external database is required to run the service or its tests.

## Dependencies

- Micrometer (Metrics and Tracing API)
- Brave (Tracing engine)

## Prerequisites

- JDK 21 on `PATH` (or set `JAVA_HOME`)

## How to run

The service listens on **`http://localhost:8080`** by default.

### Run from source

```bash
./mvnw spring-boot:run
```

### Build & run the jar

```bash
./mvnw clean package
java -jar target/mo_ha-0.0.1.jar
```

### Useful configuration (in [`application.properties`](src/main/resources/application.properties))

| Property                            | Default | Purpose                                          |
|-------------------------------------|---------|--------------------------------------------------|
| `notification.rest.connect-timeout` | `PT2S`  | HTTP connect timeout for external notifications. |
| `notification.rest.read-timeout`    | `PT5S`  | HTTP read timeout for external notifications.    |

## Interfaces

### Public API — `/config-changes`

| Method | Path                   | Description                                                                                                                |
|--------|------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/config-changes`      | Record a config change. Returns `201 Created` with `Location` header. If `severity=CRITICAL`, fires an async notification. |
| `GET`  | `/config-changes`      | List changes. Optional query params: `from` (ISO instant), `to` (ISO instant), `type` (`CREATE`/`UPDATE`/`DELETE`).        |
| `GET`  | `/config-changes/{id}` | Fetch a single change by UUID. `404` when missing.                                                                         |

#### Examples

```bash
# Record a CREATE change
curl -X POST 'http://localhost:8080/config-changes' \
  -H 'Content-Type: application/json' \
  -d '{
    "configKey":  "feature.x.enabled",
    "type":       "CREATE",
    "severity":   "NON_CRITICAL",
    "newValue":   "true",
    "changedBy":  "Alice",
    "reason":     "Initial rollout."
  }'
  
# Response (`201 Created`):
{
  "id":            "96df72d4-5001-4f8d-b4b6-e2cc4c9fd6cc",
  "configKey":     "feature.x.enabled",
  "type":          "CREATE",
  "severity":      "NON_CRITICAL",
  "previousValue": null,
  "newValue":      "true",
  "changedBy":     "Alice",
  "reason":        "Initial rollout.",
  "timestamp":     "2026-05-07T09:14:20.661885200Z"
}

# List changes within a time span:
curl 'http://localhost:8080/config-changes?from=2026-05-07T00:00:00Z&to=2026-05-07T23:59:59Z'

# List changes of a given type:
curl 'http://localhost:8080/config-changes?type=CREATE'
```

### Metrics API — `/metrics`

| Method | Path              | Description                                                         |
|--------|-------------------|---------------------------------------------------------------------|
| `GET`  | `/metrics`        | Micrometer metrics. Only the explicitly enabled meters are visible. |
| `GET`  | `/metrics/{name}` | Drill into a specific meter.                                        |

**Custom metric**: `config_changes_recorded_total` is a counter incremented every
time a change is recorded. It is tagged with:

- `severity` — `CRITICAL` or `NON_CRITICAL`
- `type` — `CREATE`, `UPDATE`, or `DELETE`

This lets you slice the count by record type, severity, or both.
Use the `tag` query parameter (repeatable) on `/metrics/{name}` to filter.

#### Examples

```bash
# Only CREATE records
curl 'http://localhost:8080/metrics/config_changes_recorded_total?tag=type:CREATE'

# CRITICAL UPDATE records
curl 'http://localhost:8080/metrics/config_changes_recorded_total?tag=type:UPDATE&tag=severity:CRITICAL'
```

### Health API — `/health`

| Method | Path                          | Description                                                                        |
|--------|-------------------------------|------------------------------------------------------------------------------------|
| `GET`  | `/health`                     | Aggregated health status.                                                          |
| `GET`  | `/health/{component}`         | Drill into a specific component (e.g. `notificationService`, `diskSpace`, `ping`). |
| `GET`  | `/health/notificationService` | Custom indicator showing status of external notification service.                  |

#### Examples

```bash
# Custom downstream-notifier indicator
curl 'http://localhost:8080/health/notificationService'
```

### Internal stub API — `/internal/notifications`

Self-hosted **test API** used as the default downstream notification target. It lets the
service be exercised end-to-end with no external dependencies and supports timeout testing.

| Method | Path                                       | Description                                                        |
|--------|--------------------------------------------|--------------------------------------------------------------------|
| `GET`  | `/internal/notifications/stub/health`      | Stub health probe (`200 OK`).                                      |
| `POST` | `/internal/notifications/stub`             | Receives a CRITICAL notification, logs and returns `202 Accepted`. |
| `GET`  | `/internal/notifications/stub/slow/health` | Same as above to simulate slow/timeout scenarios.                  |
| `POST` | `/internal/notifications/stub/slow`        | Same as above to simulate slow/timeout scenarios.                  |

## Tracing

Every request is traced via `micrometer-tracing` (Brave bridge). Sampling is set to `1.0` so a `traceId` is generated
for every incoming HTTP request and
propagated through async notification calls and is also included in error responses, so an operator can pivot from a
failing response straight to the relevant logs.

## Integration tests

The project ships with integration tests that boot the full Spring context and
exercise the service end-to-end (including the in-memory store and the internal
notification stub):

### Run the tests

```bash
./mvnw test
```

## Key Notes

A few assumptions were taken to keep the assignment focused. In a production environment those need to be considered.

- No authentication / authorization. All endpoints are unsecured.
  The `changedBy` field is a string — there is no link to an authenticated principal.
- Notifications are best-effort. Fire-and-forget. Failures
  are logged and swallowed — there is no retry (that would only practice correct library use and added another
  dependency).
- `configKey` is a string. Beyond non-blank + length, no namespace, schema, or
  anything like that is enforced.
- Values are also plain strings. `previousValue` / `newValue` carry whatever
  string the caller supplies.
- Server-assigned identity and time. `id` (UUID) and `timestamp` are always
  generated by the service.
- No pagination on `GET /config-changes`.
- String length limits are sensible defaults.