# CMMN Workflow Service

A Spring Boot application for managing CMMN workflows with tasks and file operations.

## Features

- Create workflows with scheduled execution
- Manage tasks (UPLOAD, DOWNLOAD, RE-UPLOAD, REVIEW, CONSOLIDATE)
- File upload/download for tasks
- Generate CMMN XML definitions
- Task status tracking

## API Endpoints

### Workflows
- `POST /api/workflows` - Create workflow
- `GET /api/workflows` - Get all workflows
- `GET /api/workflows/{id}` - Get workflow by ID
- `PUT /api/workflows/{id}/tasks/{taskId}/complete` - Complete task

### Tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks/{id}/upload` - Upload file to task
- `PUT /api/tasks/{id}/status` - Update task status
- `GET /api/tasks/{id}/files` - Get task files

### Files
- `GET /api/files/{fileId}/download` - Download file

### CMMN
- `GET /api/cmmn/{workflowId}` - Get CMMN definition
- `GET /api/cmmn/{workflowId}/download` - Download CMMN file

## Sample Workflow Request

```json
{
  "name": "Monthly Report Workflow",
  "scheduledTime": "2024-01-04T09:00:00",
  "frequencyPattern": "monthly-4th-working-day",
  "tasks": [
    {
      "name": "upload T1 file",
      "taskType": "UPLOAD"
    },
    {
      "name": "upload T2 file", 
      "taskType": "UPLOAD"
    },
    {
      "name": "review files",
      "taskType": "REVIEW"
    },
    {
      "name": "consolidate reports",
      "taskType": "CONSOLIDATE"
    }
  ]
}
```

## Running the Application

```bash
./mvnw spring-boot:run
```

Access H2 Console: http://localhost:8080/h2-console