# EduSphere — Swagger Testing Guide

## Prerequisites

1. Start services in this order:
   - `eureka-server` (port 8761)
   - `api-gateway` (port 8080)
   - `iam-service` (port 8081)
   - `course-service`, `enrollment-service`, `assignment-service`, `notification-service`, `audit-service`, `analytics-service` (any order)

2. Swagger UIs are available at:

| Service | Swagger URL |
|---|---|
| IAM | http://localhost:8081/swagger-ui.html |
| Course | http://localhost:8082/swagger-ui.html |
| Enrollment | http://localhost:8083/swagger-ui.html |
| Assignment | http://localhost:8084/swagger-ui.html |
| Notification | http://localhost:8085/swagger-ui.html |
| Audit | http://localhost:8086/swagger-ui.html |
| Analytics | http://localhost:8088/swagger-ui.html |

> **Note:** For end-to-end testing, all API calls should go through the gateway at `http://localhost:8080`. For testing individual services directly (bypassing the gateway), add the `X-User-Id` and `X-User-Role` headers manually in Swagger or Postman.

---

## Step 1 — IAM Service: User Onboarding

### 1.1 Bulk Onboard Users (Admin Login Required First)

The system ships with a default admin. Log in first:

**POST** `/api/v1/auth/login`
```json
{
  "email": "admin@edusphere.edu",
  "password": "Admin@123"
}
```
Copy the `accessToken` from the response. In Swagger UI, click **Authorize** → paste `Bearer <token>`.

### 1.2 Bulk Upload Users via Excel

**POST** `/api/v1/admin/users/upload` (Admin only)

Prepare an Excel file with columns: `firstName | lastName | email | role | departmentCode | studentOrEmployeeId`

Roles: `STUDENT`, `INSTRUCTOR`, `COORDINATOR`, `ADMIN`

Upload via the multipart form. Each user receives a welcome email with a temporary password.

### 1.3 List All Users

**GET** `/api/v1/admin/users` (Admin only)

Returns all active users with their `userId` values. Note down UUIDs for testing.

### 1.4 Delete a User

**DELETE** `/api/v1/admin/users/{userId}` (Admin only)

Soft-deletes the user. Audit log `USER_DELETED` is created.

---

## Step 2 — IAM Service: Authentication

### 2.1 Login as a Student

**POST** `/api/v1/auth/login`
```json
{
  "email": "student@edusphere.edu",
  "password": "<temp-password-from-email>"
}
```
The response includes:
- `accessToken` — use in Swagger `Authorize`
- `consentRequired: true` — if the user hasn't accepted T&C yet
- `passwordChangeRequired: true` — if temp password hasn't been changed

### 2.2 Accept Terms & Conditions

If `consentRequired: true`, all APIs return 403 until this is done:

**PATCH** `/api/v1/auth/consent`
```json
{
  "accepted": true,
  "termsVersion": "1.0"
}
```
The response includes a fresh `accessToken` with `consentAccepted=true`. Update the Swagger Authorize token.

### 2.3 Refresh Token

**POST** `/api/v1/auth/refresh` (uses `refresh_token` cookie set during login)

### 2.4 Logout

**POST** `/api/v1/auth/logout`

---

## Step 3 — Course Service: Course & Department Setup

### 3.1 Get Courses

**GET** `/api/v1/courses` (via gateway or course-service directly)

Note a `courseId` for subsequent steps.

### 3.2 Link a Course to a Department (Coordinator)

Login as a COORDINATOR, then:

**POST** `/api/v1/courses/{courseId}/departments/{departmentId}`

After linking, enrollment for this course will be restricted to students in `departmentId`.

### 3.3 Get Linked Departments

**GET** `/api/v1/courses/{courseId}/departments`

Returns a list of department UUIDs linked to the course.

### 3.4 Unlink Department

**DELETE** `/api/v1/courses/{courseId}/departments/{departmentId}` (Coordinator only)

---

## Step 4 — Enrollment Service

### 4.1 Enroll a Student

**POST** `/api/v1/enrollments` (Admin or Coordinator)
```json
{
  "userId": "<student-uuid>",
  "courseId": "<course-uuid>",
  "userRole": "STUDENT",
  "exception": false
}
```
**Expected behavior:**
- Returns 200 if the student's `departmentId` matches one of the course's linked departments
- Returns 403 (`"You can only enroll in courses linked to your department"`) if not
- Set `"exception": true` to bypass the department check

Audit log `USER_ENROLLED` is created on success.

### 4.2 Enroll an Instructor

Same endpoint, set `"userRole": "INSTRUCTOR"`.

### 4.3 View Enrollments for a Course

**GET** `/api/v1/enrollments?courseId={courseId}` (Admin, Coordinator, Instructor)

### 4.4 Student's Own Enrollments

**GET** `/api/v1/enrollments/students/me` (Student, uses `X-User-Id` header)

---

## Step 5 — Assignment Service

### 5.1 Create Assignment via JSON (Instructor)

**POST** `/api/v1/courses/{courseId}/assignments`

Headers: `X-User-Id: <instructor-uuid>`

```json
{
  "title": "Module 1 Quiz",
  "instructions": "Answer all questions",
  "timeLimitMinutes": 30,
  "submissionDeadline": "2026-12-31T23:59:59",
  "questions": [
    {
      "questionText": "What is Java?",
      "optionA": "A programming language",
      "optionB": "A coffee brand",
      "optionC": "An island",
      "optionD": "None",
      "correctOption": "A",
      "sequenceNumber": 1
    }
  ]
}
```
**Expected:** Returns the created assignment. Audit log `ASSIGNMENT_CREATED` is created.

### 5.2 Create Assignment via Excel Upload (Instructor)

**POST** `/api/v1/courses/{courseId}/assignments/upload` (multipart/form-data)

Form fields:
- `file` — `.xlsx` file
- `title` — assignment title
- `instructions` — (optional)
- `timeLimitMinutes` — integer
- `submissionDeadline` — ISO datetime, e.g., `2026-12-31T23:59:59`

Excel columns: `questionText | optionA | optionB | optionC | optionD | correctOption`

### 5.3 Get Assignment for Student (No Correct Answers)

**GET** `/api/v1/assignments/{assignmentId}`

Correct answers are hidden in this response.

### 5.4 Submit Assignment (Student)

**POST** `/api/v1/assignments/{assignmentId}/submit`

Headers: `X-User-Id: <student-uuid>`

```json
{
  "answers": [
    {
      "questionId": "<question-uuid>",
      "selectedOption": "A"
    }
  ],
  "timeTakenSeconds": 180
}
```
**Expected:** Returns submission detail with `isCorrect` per answer and the `correctOption` revealed. Score calculated automatically. Analytics event `ASSIGNMENT_SUBMITTED` fired. Audit log created.

### 5.5 View Who Attempted an Assignment (Instructor)

**GET** `/api/v1/assignments/{assignmentId}/attempt-status`

Returns list of students who submitted (with scores) and students who haven't.

### 5.6 View All Submissions (Instructor)

**GET** `/api/v1/assignments/{assignmentId}/submissions`

---

## Step 6 — Course Content Completion

### 6.1 Mark Content as Complete (Student)

**POST** `/api/v1/courses/{courseId}/content/{contentId}/complete`

Headers: `X-User-Id: <student-uuid>`

**Expected:** If this is the last content item, the notification service sends a course completion email.

### 6.2 Check Course Progress

**GET** `/api/v1/courses/{courseId}/progress`

Headers: `X-User-Id: <student-uuid>`

Returns: `totalContents`, `completedContents`, `progressPercentage`, `completedContentIds`

---

## Step 7 — Analytics Service

### 7.1 View Student's Progress Events

**GET** `/api/v1/analytics/progress/student/{studentId}`

Returns all events (`CONTENT_COMPLETED`, `COURSE_COMPLETED`, `ASSIGNMENT_SUBMITTED`) for the student.

### 7.2 View Course Progress Events

**GET** `/api/v1/analytics/progress/course/{courseId}` (Instructor, Coordinator, Admin)

### 7.3 View Student Progress in a Specific Course

**GET** `/api/v1/analytics/progress/student/{studentId}/course/{courseId}`

### 7.4 Get KPIs

**GET** `/api/v1/analytics/kpis?courseId={courseId}` (Instructor, Coordinator, Admin)

Returns: `totalCourses`, `totalAssignments`, `averageScore`, `passRate`

---

## Step 8 — Notification Service

### 8.1 Get My Notifications

**GET** `/api/v1/notifications`

Headers: `X-User-Id: <user-uuid>`

Returns all unread and read notifications for the user.

### 8.2 Get Unread Count

**GET** `/api/v1/notifications/count`

### 8.3 Mark Notification as Read

**PATCH** `/api/v1/notifications/{notificationId}/read`

### 8.4 Update Notification Preferences

**POST** `/api/v1/notifications/preferences`

Headers: `X-User-Id: <user-uuid>`

```json
{
  "preferences": [
    { "eventType": "COURSE_COMPLETED", "emailEnabled": true },
    { "eventType": "ASSIGNMENT_SUBMITTED", "emailEnabled": false }
  ]
}
```

---

## Step 9 — Audit Service (Admin)

### 9.1 Search Audit Logs

**GET** `/api/v1/audit/logs?action=USER_LOGIN&page=0&size=20` (Admin only)

Filter parameters: `actorId`, `action`, `resourceType`, `fromDate`, `toDate`

### 9.2 Get Specific Audit Log

**GET** `/api/v1/audit/logs/{auditId}` (Admin only)

---

## Common Error Responses

| HTTP Status | Cause | Fix |
|---|---|---|
| 401 | Missing or expired JWT | Re-login, update Swagger token |
| 403 | Wrong role for endpoint | Login with correct role |
| 403 + "Please accept the Terms & Conditions" | `consentAccepted=false` in JWT | Call `PATCH /api/v1/auth/consent` |
| 404 | Resource not found | Check UUID is correct |
| 409 | Duplicate (already enrolled, already submitted) | Check existing records |
| 403 + "Student is not enrolled" | Student not enrolled in course | Enroll the student first |

---

## Testing the Gateway vs. Direct Service

When testing via gateway (`localhost:8080`), the gateway validates the JWT and injects `X-User-*` headers automatically. You only need to set the `Authorization: Bearer <token>` header.

When testing a service directly (e.g., `localhost:8084` for assignment-service), the JWT filter won't find a valid token (gateway stripped it). Instead, manually set:
```
X-User-Id: <uuid>
X-User-Role: STUDENT
```
This simulates what the gateway would inject.
