package app.flo.controller;

import app.flo.entity.Workflow;
import app.flo.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private app.flo.service.CmmnService cmmnService;
    
    @Autowired
    private app.flo.service.WorkflowSchedulerService schedulerService;
    
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestParam String name) {
        Workflow workflow = workflowService.createWorkflow(name);
        return ResponseEntity.ok(workflow);
    }
    
    @PostMapping("/definition")
    public ResponseEntity<?> createWorkflowFromDefinition(@RequestBody app.flo.dto.WorkflowDefinitionRequest request) {
        try {
            Workflow workflow = workflowService.createWorkflowFromDefinition(request);
            return ResponseEntity.ok(workflow);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "Failed to create workflow from definition",
                "message", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<java.util.Map<String, String>> startWorkflow(@PathVariable Long id) {
        String caseInstanceId = workflowService.startWorkflow(id);
        return ResponseEntity.ok(java.util.Map.of("caseInstanceId", caseInstanceId));
    }
    
    @PostMapping("/{id}/trigger")
    public ResponseEntity<java.util.Map<String, String>> triggerWorkflow(@PathVariable Long id) {
        try {
            Workflow workflow = workflowService.getWorkflowById(id);
            if (workflow == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Workflow not found"));
            }
            
            schedulerService.triggerWorkflowExecution(workflow);
            return ResponseEntity.ok(java.util.Map.of("workflowId", id.toString(), "status", "triggered"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable Long id) {
        Workflow workflow = workflowService.getWorkflowById(id);
        return workflow != null ? ResponseEntity.ok(workflow) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/tasks/{taskId}/complete")
    public ResponseEntity<java.util.Map<String, String>> completeTask(@PathVariable Long id, @PathVariable Long taskId) {
        try {
            schedulerService.completeTaskAndMoveNext(taskId);
            return ResponseEntity.ok(java.util.Map.of("taskId", taskId.toString(), "status", "completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/definition")
    public ResponseEntity<Workflow> updateWorkflowDefinition(@PathVariable Long id, @RequestBody app.flo.dto.WorkflowDefinitionRequest request) {
        try {
            Workflow workflow = workflowService.updateWorkflowDefinition(id, request);
            return workflow != null ? ResponseEntity.ok(workflow) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/case/{caseInstanceId}")
    public ResponseEntity<Workflow> getWorkflowByCaseId(@PathVariable String caseInstanceId) {
        Workflow workflow = workflowService.getWorkflowByCaseId(caseInstanceId);
        return workflow != null ? ResponseEntity.ok(workflow) : ResponseEntity.notFound().build();
    }
}