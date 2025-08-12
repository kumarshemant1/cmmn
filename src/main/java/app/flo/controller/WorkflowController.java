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
            String caseInstanceId = cmmnService.triggerWorkflow(id);
            return ResponseEntity.ok(java.util.Map.of("caseInstanceId", caseInstanceId, "status", "triggered"));
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
}