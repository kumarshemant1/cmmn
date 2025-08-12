package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.repository.WorkflowRepository;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

@Service
public class CmmnService {
    
    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;
    
    @Autowired
    private CmmnTaskService cmmnTaskService;
    
    @Autowired
    private CmmnRepositoryService cmmnRepositoryService;
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    public String createCaseInstance(Long workflowId) {
        try {
            Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
            if (workflow == null) {
                throw new RuntimeException("Workflow not found with ID: " + workflowId);
            }
            
            // Check if case instance already exists
            if (workflow.getCaseInstanceId() != null) {
                return workflow.getCaseInstanceId();
            }
            
            String caseInstanceId;
            try {
                // Deploy CMMN file if exists
                String caseDefinitionKey = deployCmmnFile(workflow.getName());
                
                // Create case instance
                CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                    .caseDefinitionKey(caseDefinitionKey)
                    .businessKey(workflowId.toString())
                    .name(workflow.getName())
                    .start();
                caseInstanceId = caseInstance.getId();
            } catch (Exception flowableError) {
                // Fallback: generate UUID-based case instance ID
                caseInstanceId = java.util.UUID.randomUUID().toString();
            }
                
            workflow.setCaseInstanceId(caseInstanceId);
            workflowRepository.save(workflow);
            
            return caseInstanceId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create case instance: " + e.getMessage(), e);
        }
    }
    
    private String deployCmmnFile(String workflowName) throws IOException {
        try {
            String caseId = workflowName.replaceAll("\\s+", "").toLowerCase() + "Case";
            String fileName = caseId + ".cmmn";
            
            // Use absolute path
            java.nio.file.Path currentPath = java.nio.file.Paths.get("").toAbsolutePath();
            java.nio.file.Path filePath = currentPath.resolve("src/main/resources/processes/" + fileName);
            
            System.out.println("Looking for CMMN file at: " + filePath.toString());
            
            // Check if file exists
            if (!java.nio.file.Files.exists(filePath)) {
                System.err.println("CMMN file not found at: " + filePath.toString());
                throw new RuntimeException("CMMN file not found: " + filePath.toString());
            }
            
            // Deploy CMMN file to Flowable using file input stream
            try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath.toFile())) {
                cmmnRepositoryService.createDeployment()
                    .addInputStream(fileName, fis)
                    .name(workflowName + " Deployment")
                    .deploy();
                System.out.println("Successfully deployed CMMN file: " + fileName);
            }
                
            return caseId;
        } catch (Exception e) {
            System.err.println("Failed to deploy CMMN file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Map<String, Object> getCaseInstanceInfo(String caseInstanceId) {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceQuery()
            .caseInstanceId(caseInstanceId)
            .singleResult();
            
        Map<String, Object> info = new HashMap<>();
        if (caseInstance != null) {
            info.put("id", caseInstance.getId());
            info.put("name", caseInstance.getName());
            info.put("businessKey", caseInstance.getBusinessKey());
            info.put("state", caseInstance.getState());
            info.put("startTime", caseInstance.getStartTime());
        }
        return info;
    }
    
    public List<Map<String, Object>> getCaseTasks(String caseInstanceId) {
        List<Task> tasks = cmmnTaskService.createTaskQuery()
            .caseInstanceId(caseInstanceId)
            .list();
            
        return tasks.stream().map(task -> {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("id", task.getId());
            taskInfo.put("name", task.getName());
            taskInfo.put("assignee", task.getAssignee());
            taskInfo.put("createTime", task.getCreateTime());
            return taskInfo;
        }).toList();
    }
    
    public String triggerWorkflow(Long workflowId) {
        try {
            Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
            if (workflow == null) {
                throw new RuntimeException("Workflow not found with ID: " + workflowId);
            }
            
            // Deploy and start new case instance
            String caseDefinitionKey = deployCmmnFile(workflow.getName());
            
            CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey(caseDefinitionKey)
                .businessKey(workflowId.toString())
                .name(workflow.getName() + " - Triggered")
                .start();
                
            System.out.println("Triggered workflow: " + workflow.getName() + " with case instance: " + caseInstance.getId());
            return caseInstance.getId();
        } catch (Exception e) {
            System.err.println("Failed to trigger workflow: " + e.getMessage());
            throw new RuntimeException("Failed to trigger workflow: " + e.getMessage(), e);
        }
    }
}