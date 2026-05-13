# EduSphere Backend — Technical Documentation

## Architecture Overview

EduSphere is a Spring Boot 3.3.5 microservices platform built on Spring Cloud 2023.0.3 (Java 21). Services register with Netflix Eureka and communicate through an API Gateway. All external requests enter via the gateway; inter-service calls use Feign with a shared service secret.

### Service Registry

| Service | Port | Database |
|---|---|---|
| eureka-server | 8761 | — |
| api-gateway | 8080 | — |
| iam-service | 8081 | edusphere_iam |
| course-service | 8082 | edusphere_courses |
| enrollment-service | 8083 | edusphere_enrollment |
| assignment-service | 8084 | edusphere_assignments |
| notification-service | 8085 | edusphere_notifications |
| audit-service | 8086 | edusphere_audit |
| analytics-service | 8088 | edusphere_analytics |

---

## Security Architecture

### Request Flow

```
Client → API Gateway (JWT validation + header injection)
       → Downstream Service (GatewayHeaderAuthFilter reads X-User-* headers)
       → @PreAuthorize evaluates ROLE_*
```

### API Gateway (`api-gateway/JwtAuthFilter.java`)

The gateway validates the JWT token on every request (except auth endpoints). It:

1. Validates the JWT signature and expiry
2. Checks the `consentAccepted` JWT claim — returns HTTP 403 with a consent-required message if `false` (except `/api/v1/auth/consent`, `/api/v1/auth/logout`, `/api/v1/auth/validate`)
3. Strips the `Authorization` header and injects upstream-safe headers:
   - `X-User-Id` — UUID of the authenticated user
   - `X-User-Role` — user's role (STUDENT / INSTRUCTOR / COORDINATOR / ADMIN)
   - `X-User-Email` — user's email
   - `X-Consent-Accepted` — boolean

### `GatewayHeaderAuthFilter` (all downstream services)

Each service has `GatewayHeaderAuthFilter extends OncePerRequestFilter`. It reads `X-User-Id` and `X-User-Role`, constructs a `UsernamePasswordAuthenticationToken` with `ROLE_<ROLE>`, and stores it in the `SecurityContext`. This is why `@PreAuthorize("hasRole('STUDENT')")` works without a local JWT in downstream services.

### `ServiceAuthFilter` (all downstream services)

Inter-service Feign calls include an `X-Service-Key` header (value: `edusphere-internal-svc-2026`). The `ServiceAuthFilter` reads this header and grants `ROLE_SERVICE` to the request. This allows internal endpoints annotated `@PreAuthorize("hasRole('SERVICE')")` to be called between services.

### Filter Chain Order (each downstream service)

```
GatewayHeaderAuthFilter → ServiceAuthFilter → JwtAuthFilter → UsernamePasswordAuthenticationFilter
```

---

## IAM Service

### JWT Token

Access tokens are HS256-signed JWTs containing:
- `sub` — userId
- `email`
- `role`
- `consentAccepted` — boolean; gateway enforces this before routing

Token lifetime: 15 minutes. Refresh tokens are stored hashed (SHA-256) in the database, valid 7 days.

### Consent Enforcement

1. User logs in → receives JWT with `consentAccepted=false` if they haven't accepted T&C
2. All API calls return HTTP 403 (`"Please accept the Terms & Conditions"`) until consent is accepted
3. `PATCH /api/v1/auth/consent` accepts the terms → server issues a fresh JWT with `consentAccepted=true`
4. `UserConsent` record is persisted with IP address and terms version

### Key Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/auth/login` | — | Login, returns JWT |
| POST | `/api/v1/auth/refresh` | — | Refresh access token via cookie |
| POST | `/api/v1/auth/logout` | — | Revoke refresh token |
| PATCH | `/api/v1/auth/consent` | Authenticated | Accept T&C (returns new JWT) |
| POST | `/api/v1/admin/users/upload` | ADMIN | Bulk onboard users via Excel |
| GET | `/api/v1/admin/users` | ADMIN | List all users |
| DELETE | `/api/v1/admin/users/{userId}` | ADMIN | Soft-delete user |

### Audit Events Logged
- `USER_LOGIN` — on every successful login
- `CONSENT_ACCEPTED` — when T&C are accepted
- `USER_DELETED` — when admin deletes a user

---

## Course Service

### Course Content Upload

Instructors can upload content (PDF, video, etc.) to a course. The service validates that the instructor is enrolled in the course via Feign call to `enrollment-service`. If the enrollment service is unavailable, the check is bypassed (fail-open for resilience).

### Multi-Department Course Linking

Coordinators can link a course to one or more departments. Enrollment then enforces that a student's `departmentId` (from IAM) must match one of the course's linked departments, unless the enrollment is marked `isException=true`.

### Content Completion & Course Progress

When a student marks a content item as complete:
1. A `ContentCompletion` record is saved (unique constraint on `student_id + content_id`)
2. Analytics service is called with event `CONTENT_COMPLETED`
3. If all content in the course is now complete:
   - Analytics service is called with `COURSE_COMPLETED`
   - Notification service sends a congratulation email via `POST /api/v1/notifications/course-completion`

### Key Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/courses/{courseId}/departments/{deptId}` | COORDINATOR | Link course to department |
| DELETE | `/api/v1/courses/{courseId}/departments/{deptId}` | COORDINATOR | Unlink course from department |
| GET | `/api/v1/courses/{courseId}/departments` | Any | Get departments linked to course |
| POST | `/api/v1/courses/{courseId}/content/{contentId}/complete` | STUDENT | Mark content as complete |
| GET | `/api/v1/courses/{courseId}/progress` | STUDENT | Get own progress in course |

---

## Enrollment Service

### Enrollment Validation

When enrolling a user:
1. IAM service is called to verify the user exists and is active
2. Course service is called to verify the course exists and is active
3. Course service returns linked `departmentIds` — user's `departmentId` must be in this list
4. `isException=true` in the request bypasses the department check (admin/coordinator use)

### Visibility

| Endpoint | Allowed Roles |
|---|---|
| `POST /api/v1/enrollments` | ADMIN, COORDINATOR |
| `GET /api/v1/enrollments?courseId=` | ADMIN, COORDINATOR, INSTRUCTOR |
| `GET /api/v1/enrollments/students/me` | STUDENT |
| `GET /api/v1/enrollments/instructors/{id}` | INSTRUCTOR |

### Audit Events Logged
- `USER_ENROLLED` — when any user is enrolled in a course

---

## Assignment Service

### Assignment Creation

Instructors can create assignments two ways:
1. JSON body with a `questions` array — `POST /api/v1/courses/{courseId}/assignments`
2. Excel file upload — `POST /api/v1/courses/{courseId}/assignments/upload`

Excel format (row 1 = header, rows 2+ = questions):

| Column | Field |
|---|---|
| A | questionText |
| B | optionA |
| C | optionB |
| D | optionC |
| E | optionD |
| F | correctOption (A/B/C/D) |

The instructor must be enrolled in the course to create an assignment.

### Auto-Grading

On submission, each answer's `selectedOption` is compared to `question.correctOption`. Score = `(correct / total) * 100`. The correct options are hidden in the student-facing `GET /api/v1/assignments/{id}` response and revealed in the submission result.

### Post-Submission Events

After a successful submission:
1. Analytics service receives `ASSIGNMENT_SUBMITTED` event with the score
2. Audit service logs `ASSIGNMENT_SUBMITTED`

### Key Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/courses/{courseId}/assignments` | INSTRUCTOR | Create assignment (JSON) |
| POST | `/api/v1/courses/{courseId}/assignments/upload` | INSTRUCTOR | Create assignment (Excel) |
| GET | `/api/v1/assignments/{id}` | Any | Get assignment (no correct answers) |
| POST | `/api/v1/assignments/{id}/submit` | STUDENT | Submit assignment |
| GET | `/api/v1/assignments/{id}/submissions` | INSTRUCTOR | All submissions for assignment |
| GET | `/api/v1/assignments/{id}/attempt-status` | INSTRUCTOR | Who attempted / who didn't |
| GET | `/api/v1/students/{studentId}/progress` | Any | Student progress summary |

---

## Analytics Service

### Progress Tracking

The `StudentProgress` table stores one row per event. Event types:
- `CONTENT_COMPLETED` — fired by course-service when a student completes a content item
- `COURSE_COMPLETED` — fired by course-service when a student completes all content
- `ASSIGNMENT_SUBMITTED` — fired by assignment-service on submission

The `POST /api/v1/analytics/progress` endpoint is accessible only to `ROLE_SERVICE`.

### KPI Endpoint

`GET /api/v1/analytics/kpis?courseId=&deptId=` returns:
- `totalCourses` — from course-service
- `totalAssignments` — count of `ASSIGNMENT_SUBMITTED` events
- `averageScore` — mean score for the given `courseId` (if provided)

### Key Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/analytics/progress` | SERVICE | Record a progress event |
| GET | `/api/v1/analytics/progress/student/{id}` | INSTRUCTOR, COORDINATOR, ADMIN, STUDENT | Student's all events |
| GET | `/api/v1/analytics/progress/course/{id}` | INSTRUCTOR, COORDINATOR, ADMIN | All events for a course |
| GET | `/api/v1/analytics/progress/student/{sid}/course/{cid}` | INSTRUCTOR, COORDINATOR, ADMIN, STUDENT | Student events in course |
| GET | `/api/v1/analytics/kpis` | INSTRUCTOR, COORDINATOR, ADMIN | Platform KPIs |

---

## Audit Service

### Audit Log Structure

Each `AuditLog` record contains:
- `actorId` — UUID of the user who performed the action
- `actorRole` — their role at time of action
- `action` — event name (e.g., `USER_LOGIN`, `ASSIGNMENT_SUBMITTED`)
- `resourceType` — type of the affected resource
- `resourceId` — ID of the affected resource
- `serviceName` — which service logged the event
- `additionalData` — free-form JSON string for extra context

### Services That Log Audit Events

| Service | Events |
|---|---|
| iam-service | USER_LOGIN, CONSENT_ACCEPTED, USER_DELETED |
| enrollment-service | USER_ENROLLED |
| assignment-service | ASSIGNMENT_CREATED, ASSIGNMENT_SUBMITTED |

### Key Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/audit/logs` | Any authenticated | Create audit log (called by services) |
| GET | `/api/v1/audit/logs` | ADMIN | Search/filter audit logs |
| GET | `/api/v1/audit/logs/{auditId}` | ADMIN | Get specific audit log |

---

## Notification Service

### Dispatch Flow

`POST /api/v1/notifications/dispatch` accepts any event. The service:
1. Saves the notification record to the DB
2. Checks the user's email preference for that event type
3. Resolves the user's email via IAM service
4. Sends an email if the channel includes `EMAIL` and email is enabled in preferences

### Course Completion Notification

`POST /api/v1/notifications/course-completion` (SERVICE role only) is called by course-service when a student completes all content. It dispatches a `COURSE_COMPLETED` in-app notification and congratulation email.

### Notification Preferences

Users can enable/disable email per event type via `POST /api/v1/notifications/preferences`.

---

## Inter-Service Communication

All Feign clients use `FeignClientConfig`, which injects the `X-Service-Key` header. The `ServiceAuthFilter` in each downstream service validates this key and grants `ROLE_SERVICE`.

```
app.service.secret: edusphere-internal-svc-2026
```

### Service Dependencies (Feign Clients)

| Caller | Calls |
|---|---|
| assignment-service | enrollment-service, course-service, analytics-service, audit-service, notification-service |
| course-service | enrollment-service, analytics-service, notification-service, audit-service |
| enrollment-service | iam-service, course-service, audit-service |
| iam-service | audit-service |
| notification-service | iam-service |
| analytics-service | course-service |

---

## Database Schema Notes

- All primary keys are `UUID` generated via `GenerationType.UUID`
- Soft-delete: `deleted` boolean column in `BaseEntity`; filter with `WHERE deleted = false`
- JPA `ddl-auto: update` — tables are auto-created/updated on startup
- Each service has its own MySQL database (database-per-service pattern)
