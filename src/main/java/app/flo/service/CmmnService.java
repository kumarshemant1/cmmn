package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.repository.WorkflowRepository;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmmnService {
    
    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;
    
    @Autowired
    private CmmnTaskService cmmnTaskService;
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    public String createCaseInstance(Long workflowId) {
        try {
            Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
            if (workflow == null) {
                throw new RuntimeException("Workflow not found with ID: " + workflowId);
            }
            
            // Check if case instance already exists
            if (workflow.getCmmnCaseId() != null) {
                return workflow.getCmmnCaseId();
            }
            
            // Generate unique case instance ID if Flowable not available
            String caseInstanceId;
            try {
                CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                    .businessKey(workflowId.toString())
                    .name(workflow.getName())
                    .start();
                caseInstanceId = caseInstance.getId();
            } catch (Exception flowableError) {
                // Fallback: generate UUID-based case instance ID
                caseInstanceId = "case_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            }
                
            workflow.setCmmnCaseId(caseInstanceId);
            workflowRepository.save(workflow);
            
            return caseInstanceId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create case instance: " + e.getMessage(), e);
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
}