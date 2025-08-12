package app.flo.controller;

import app.flo.entity.Task;
import app.flo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestParam String name, @RequestParam Long workflowId, @RequestParam String taskType) {
        Task task = taskService.createTask(name, workflowId);
        task.setTaskType(app.flo.entity.TaskType.valueOf(taskType));
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
    public ResponseEntity<app.flo.entity.BusinessFile> uploadFile(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            app.flo.entity.BusinessFile businessFile = taskService.uploadFileToTask(id, file);
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
}