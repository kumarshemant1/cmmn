package app.flo.controller;

import app.flo.service.WorkflowService;
import app.flo.service.TaskService;
import app.flo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class CaseController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private FileService fileService;
    
    @GetMapping("/{caseInstanceId}/workflow")
    public ResponseEntity<app.flo.entity.Workflow> getWorkflowByCase(@PathVariable String caseInstanceId) {
        app.flo.entity.Workflow workflow = workflowService.getWorkflowByCaseId(caseInstanceId);
        return workflow != null ? ResponseEntity.ok(workflow) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{caseInstanceId}/tasks")
    public ResponseEntity<java.util.List<app.flo.entity.Task>> getTasksByCase(@PathVariable String caseInstanceId) {
        java.util.List<app.flo.entity.Task> tasks = taskService.getTasksByCaseId(caseInstanceId);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{caseInstanceId}/files")
    public ResponseEntity<java.util.List<app.flo.entity.BusinessFile>> getFilesByCase(@PathVariable String caseInstanceId) {
        java.util.List<app.flo.entity.BusinessFile> files = fileService.getFilesByCaseId(caseInstanceId);
        return ResponseEntity.ok(files);
    }
}