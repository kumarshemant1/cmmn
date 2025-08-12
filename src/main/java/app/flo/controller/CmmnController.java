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
}