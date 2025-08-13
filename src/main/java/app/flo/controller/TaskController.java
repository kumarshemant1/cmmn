package app.flo.controller;

import app.flo.entity.Task;
import app.flo.service.TaskService;
import app.flo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestParam String name, @RequestParam Long workflowId, @RequestParam String taskType) {
        Task task = taskService.createTask(name, workflowId);
        task.setTaskType(app.flo.enums.TaskType.valueOf(taskType));
        return ResponseEntity.ok(task);
    }
    
    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<Task>> getTasksByWorkflow(@PathVariable Long workflowId) {
        return ResponseEntity.ok(taskService.getTasksByWorkflow(workflowId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        Task task = taskService.completeTask(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/upload")
    public ResponseEntity<app.flo.entity.BusinessFile> uploadFile(
            @PathVariable Long id, 
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "retainFile", required = false) Boolean retainFile,
            @RequestParam(value = "keepVersion", required = false) Boolean keepVersion,
            @RequestParam(value = "keepHistory", required = false) Boolean keepHistory) {
        try {
            app.flo.entity.BusinessFile businessFile = taskService.uploadFileToTask(id, file, retainFile, keepVersion, keepHistory);
            return businessFile != null ? ResponseEntity.ok(businessFile) : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/files")
    public ResponseEntity<java.util.List<app.flo.entity.BusinessFile>> getTaskFiles(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskFiles(id));
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
    
    @PutMapping("/{id}/assign")
    public ResponseEntity<Task> assignTask(@PathVariable Long id, @RequestParam String assignee) {
        Task task = taskService.assignTask(id, assignee);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/assignee/{assignee}")
    public ResponseEntity<List<Task>> getTasksByAssignee(@PathVariable String assignee) {
        List<Task> tasks = taskRepository.findByAssignee(assignee);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{id}/completion-date")
    public ResponseEntity<Task> setCompletionWorkingDay(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        try {
            Task task = taskService.getTaskById(id);
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
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/workflow/{workflowId}/add")
    public ResponseEntity<Task> addTaskToWorkflow(@PathVariable Long workflowId, @RequestBody java.util.Map<String, Object> taskData) {
        Task task = new Task();
        task.setName((String) taskData.get("name"));
        task.setTaskType(app.flo.enums.TaskType.valueOf(((String) taskData.get("taskType")).toUpperCase()));
        
        if (taskData.containsKey("assignee")) {
            task.setAssignee((String) taskData.get("assignee"));
        }
        if (taskData.containsKey("taskGroup")) {
            task.setTaskGroup((String) taskData.get("taskGroup"));
        }
        
        app.flo.entity.Workflow workflow = taskService.getWorkflowById(workflowId);
        if (workflow != null) {
            task.setWorkflow(workflow);
            task.setTaskId("dynamic_task_" + System.currentTimeMillis());
            return ResponseEntity.ok(taskRepository.save(task));
        }
        return ResponseEntity.notFound().build();
    }
}