package app.flo.controller;

import app.flo.entity.TaskMetadata;
import app.flo.service.FlowableTaskService;
import app.flo.service.TaskService;
import org.flowable.task.api.Task;
import app.flo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private FlowableTaskService flowableTaskService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @GetMapping("/case/{caseInstanceId}")
    public ResponseEntity<java.util.List<app.flo.dto.TaskDTO>> getTasksByCaseInstance(@PathVariable String caseInstanceId) {
        try {
            java.util.List<Task> tasks = flowableTaskService.getTasksByCaseInstance(caseInstanceId);
            java.util.List<app.flo.dto.TaskDTO> taskDTOs = tasks.stream()
                .map(app.flo.dto.TaskDTO::new)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(taskDTOs);
        } catch (Exception e) {
            System.err.println("Error getting tasks: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<java.util.List<app.flo.dto.TaskDTO>> getActiveTasks() {
        try {
            java.util.List<Task> tasks = flowableTaskService.getActiveTasks();
            java.util.List<app.flo.dto.TaskDTO> taskDTOs = tasks.stream()
                .map(app.flo.dto.TaskDTO::new)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(taskDTOs);
        } catch (Exception e) {
            System.err.println("Error getting active tasks: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<app.flo.dto.TaskDTO> getTask(@PathVariable String taskId) {
        try {
            System.out.println("Getting Flowable task with ID: " + taskId);
            Task task = flowableTaskService.getFlowableTask(taskId);
            if (task != null) {
                System.out.println("Found task: " + task.getName());
                return ResponseEntity.ok(new app.flo.dto.TaskDTO(task));
            } else {
                System.out.println("Task not found with ID: " + taskId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting task: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{taskId}/complete")
    public ResponseEntity<java.util.Map<String, String>> completeTask(@PathVariable String taskId) {
        try {
            flowableTaskService.completeTaskWithMetadata(taskId);
            return ResponseEntity.ok(java.util.Map.of("taskId", taskId, "status", "completed"));
        } catch (Exception e) {
            System.err.println("Error completing task: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{id}/upload")
    public ResponseEntity<app.flo.entity.BusinessFile> uploadFile(
            @PathVariable Long id, 
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "retainFile", required = false) Boolean retainFile,
            @RequestParam(value = "keepVersion", required = false) Boolean keepVersion,
            @RequestParam(value = "keepHistory", required = false) Boolean keepHistory) {
        try {
            System.out.println("Upload request - TaskId: " + id + ", File: " + file.getOriginalFilename());
            
            if (file.isEmpty()) {
                System.err.println("File is empty");
                return ResponseEntity.badRequest().build();
            }
            
            app.flo.entity.BusinessFile businessFile;
            if (retainFile != null || keepVersion != null || keepHistory != null) {
                businessFile = taskService.uploadFileToTask(id, file, retainFile, keepVersion, keepHistory);
            } else {
                businessFile = taskService.uploadFileToTask(id, file);
            }
            
            return businessFile != null ? ResponseEntity.ok(businessFile) : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/files")
    public ResponseEntity<java.util.List<app.flo.entity.BusinessFile>> getTaskFiles(@PathVariable Long id) {
        try {
            System.out.println("Getting files for task ID: " + id);
            java.util.List<app.flo.entity.BusinessFile> files = taskService.getTaskFiles(id);
            System.out.println("Found " + files.size() + " files for task " + id);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            System.err.println("Error getting task files: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{id}/consolidate")
    public ResponseEntity<app.flo.entity.BusinessFile> consolidateFiles(@PathVariable Long id) {
        try {
            app.flo.entity.BusinessFile consolidatedFile = taskService.consolidateWorkflowFiles(id);
            return consolidatedFile != null ? ResponseEntity.ok(consolidatedFile) : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{taskId}/assign")
    public ResponseEntity<java.util.Map<String, String>> assignTask(@PathVariable String taskId, @RequestParam String assignee) {
        try {
            flowableTaskService.assignTask(taskId, assignee);
            return ResponseEntity.ok(java.util.Map.of("taskId", taskId, "assignee", assignee));
        } catch (Exception e) {
            System.err.println("Error assigning task: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/assignee/{assignee}")
    public ResponseEntity<java.util.List<app.flo.dto.TaskDTO>> getTasksByAssignee(@PathVariable String assignee) {
        try {
            java.util.List<Task> tasks = flowableTaskService.getTasksByAssignee(assignee);
            java.util.List<app.flo.dto.TaskDTO> taskDTOs = tasks.stream()
                .map(app.flo.dto.TaskDTO::new)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(taskDTOs);
        } catch (Exception e) {
            System.err.println("Error getting tasks by assignee: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{taskId}/status")
    public ResponseEntity<java.util.Map<String, String>> getTaskStatus(@PathVariable String taskId) {
        try {
            Task task = flowableTaskService.getFlowableTask(taskId);
            String status = task != null ? "ACTIVE" : "COMPLETED";
            return ResponseEntity.ok(java.util.Map.of("status", status));
        } catch (Exception e) {
            System.err.println("Error getting task status: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{taskId}/completed")
    public ResponseEntity<java.util.Map<String, Boolean>> isTaskCompleted(@PathVariable String taskId) {
        try {
            Task task = flowableTaskService.getFlowableTask(taskId);
            boolean completed = task == null; // If not found in Flowable, it's completed
            return ResponseEntity.ok(java.util.Map.of("completed", completed));
        } catch (Exception e) {
            System.err.println("Error checking task completion: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}/completion-date")
    public ResponseEntity<TaskMetadata> setCompletionWorkingDay(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        try {
            TaskMetadata task = taskService.getTaskById(id);
            if (task != null) {
                String completionDate = request.get("completionWorkingDay");
                if (completionDate != null) {
                    task.setCompletionWorkingDay(java.time.LocalDateTime.parse(completionDate));
                    taskRepository.save(task);
                    return ResponseEntity.ok(task);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error setting completion date: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/flowable/{flowableTaskId}/metadata")
    public ResponseEntity<TaskMetadata> getTaskMetadataByFlowableId(@PathVariable String flowableTaskId) {
        try {
            TaskMetadata metadata = taskService.getTaskMetadataByFlowableTaskId(flowableTaskId);
            return metadata != null ? ResponseEntity.ok(metadata) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error getting task metadata: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/workflow/{workflowInstanceId}/add")
    public ResponseEntity<TaskMetadata> addTaskToWorkflowInstance(@PathVariable Long workflowInstanceId, @RequestBody java.util.Map<String, Object> taskData) {
        try {
            TaskMetadata task = new TaskMetadata();
            task.setName((String) taskData.get("name"));
            task.setTaskType(app.flo.enums.TaskType.valueOf(((String) taskData.get("taskType")).toUpperCase()));
            
            if (taskData.containsKey("assignee")) {
                task.setAssignee((String) taskData.get("assignee"));
            }
            if (taskData.containsKey("taskGroup")) {
                task.setTaskGroup((String) taskData.get("taskGroup"));
            }
            
            // This is deprecated - should use FlowableTaskService.createTaskMetadata
            task.setTaskId("dynamic_task_" + System.currentTimeMillis());
            return ResponseEntity.ok(taskRepository.save(task));
        } catch (Exception e) {
            System.err.println("Error adding task to workflow instance: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}