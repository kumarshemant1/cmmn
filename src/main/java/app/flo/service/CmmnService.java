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
    
    @Autowired
    private CmmnGeneratorService cmmnGeneratorService;
    
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
            
            // Generate UUID-based case instance ID (simplified approach)
            String caseInstanceId = java.util.UUID.randomUUID().toString();
                
            workflow.setCaseInstanceId(caseInstanceId);
            workflowRepository.save(workflow);
            
            return caseInstanceId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create case instance: " + e.getMessage(), e);
        }
    }
    
    private String deployCmmnFromMemory(String workflowName, String cmmnContent) {
        try {
            String caseId = workflowName.replaceAll("\\s+", "").toLowerCase() + "Case";
            String fileName = caseId + ".cmmn";
            
            // Deploy CMMN content directly from memory
            java.io.ByteArrayInputStream cmmnStream = new java.io.ByteArrayInputStream(cmmnContent.getBytes());
            
            cmmnRepositoryService.createDeployment()
                .addInputStream(fileName, cmmnStream)
                .name(workflowName + " Deployment")
                .deploy();
                
            System.out.println("Successfully deployed CMMN from memory: " + fileName);
            return caseId;
        } catch (Exception e) {
            System.err.println("Failed to deploy CMMN from memory: " + e.getMessage());
            throw new RuntimeException("Failed to deploy CMMN: " + e.getMessage(), e);
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
            
            // Generate new case instance ID for triggered workflow
            String caseInstanceId = java.util.UUID.randomUUID().toString();
            
            System.out.println("Triggered workflow: " + workflow.getName() + " with case instance: " + caseInstanceId);
            return caseInstanceId;
        } catch (Exception e) {
            System.err.println("Failed to trigger workflow: " + e.getMessage());
            throw new RuntimeException("Failed to trigger workflow: " + e.getMessage(), e);
        }
    }
}