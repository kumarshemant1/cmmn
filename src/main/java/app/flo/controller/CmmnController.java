package app.flo.controller;

import app.flo.service.CmmnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cmmn")
public class CmmnController {
    
    @Autowired
    private CmmnService cmmnService;
    
    @PostMapping("/case/{workflowId}")
    public ResponseEntity<Map<String, String>> createCaseInstance(@PathVariable Long workflowId) {
        String caseInstanceId = cmmnService.createCaseInstance(workflowId);
        return ResponseEntity.ok(Map.of("caseInstanceId", caseInstanceId));
    }
    
    @GetMapping("/case/{caseInstanceId}")
    public ResponseEntity<Map<String, Object>> getCaseInstance(@PathVariable String caseInstanceId) {
        Map<String, Object> caseInfo = cmmnService.getCaseInstanceInfo(caseInstanceId);
        return ResponseEntity.ok(caseInfo);
    }
    
    @GetMapping("/case/{caseInstanceId}/tasks")
    public ResponseEntity<java.util.List<Map<String, Object>>> getCaseTasks(@PathVariable String caseInstanceId) {
        java.util.List<Map<String, Object>> tasks = cmmnService.getCaseTasks(caseInstanceId);
        return ResponseEntity.ok(tasks);
    }
    
    @Autowired
    private app.flo.service.CmmnGeneratorService cmmnGeneratorService;
    
    @Autowired
    private app.flo.service.WorkflowService workflowService;
    
    @GetMapping("/{workflowId}")
    public ResponseEntity<String> getCmmnDefinition(@PathVariable Long workflowId) {
        try {
            app.flo.entity.Workflow workflow = workflowService.getWorkflowById(workflowId);
            if (workflow != null) {
                String cmmnContent = cmmnGeneratorService.getCmmnContent(workflow.getName());
                return cmmnContent != null ? ResponseEntity.ok(cmmnContent) : ResponseEntity.notFound().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{workflowId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadCmmnFile(@PathVariable Long workflowId) {
        try {
            app.flo.entity.Workflow workflow = workflowService.getWorkflowById(workflowId);
            if (workflow != null) {
                String fileName = workflow.getName().replaceAll("\\s+", "_").toLowerCase() + ".cmmn";
                java.nio.file.Path filePath = java.nio.file.Paths.get("src/main/resources/processes/").resolve(fileName);
                
                if (java.nio.file.Files.exists(filePath)) {
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath);
                    return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}