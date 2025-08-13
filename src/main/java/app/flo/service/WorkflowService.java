package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.entity.WorkflowInstance;
import app.flo.repository.WorkflowRepository;
import app.flo.repository.WorkflowInstanceRepository;
import jakarta.transaction.Transactional;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkflowService {
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;
    
    @Autowired
    private CmmnGeneratorService cmmnGeneratorService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private CmmnService cmmnService;
    
    public Workflow createWorkflow(String name) {
        Workflow workflow = new Workflow();
        workflow.setName(name);
        return workflowRepository.save(workflow);
    }

    public Workflow createWorkflowFromDefinition(app.flo.dto.WorkflowDefinitionRequest request) {
        System.out.println("Creating workflow from definition: " + request.getName());
        
        // Create workflow first
        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        
        if (request.getScheduledTime() != null && !request.getScheduledTime().isEmpty()) {
            workflow.setScheduledTime(java.time.LocalDateTime.parse(request.getScheduledTime()));
        }
        
        workflow.setFrequency(request.getFrequency());
        if (request.getExecutionTime() != null && !request.getExecutionTime().isEmpty()) {
            workflow.setExecutionTime(java.time.LocalTime.parse(request.getExecutionTime()));
        }
        workflow.setNthWorkingDay(request.getNthWorkingDay());
        
        if (request.getDrawflowData() != null) {
            try {
                workflow.setDrawflowData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.getDrawflowData()));
            } catch (Exception e) {
                System.err.println("Failed to serialize drawflow data: " + e.getMessage());
            }
        }
        
        workflow = workflowRepository.save(workflow);
        System.out.println("Workflow template saved with ID: " + workflow.getId());
        
        // Generate and persist CMMN content to disk
        try {
            String cmmnContent = cmmnGeneratorService.generateCmmnContent(request);
            System.out.println("CMMN content generated and saved for workflow: " + workflow.getName());
        } catch (Exception e) {
            System.err.println("Failed to generate CMMN content: " + e.getMessage());
        }
        
        // Task creation happens at instance level, not template level
        System.out.println("Workflow template created. Tasks will be created when instances are started.");
        
        return workflow;
    }
    
    public WorkflowInstance startWorkflow(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
        if (workflow == null) {
            throw new RuntimeException("Workflow template not found");
        }
        
        // Create case instance
        String caseInstanceId = cmmnService.createCaseInstance(workflowId);
        if (caseInstanceId == null) {
            throw new RuntimeException("Failed to create case instance");
        }
        
        // Create workflow instance
        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflow(workflow);
        instance.setCaseInstanceId(caseInstanceId);
        instance = workflowInstanceRepository.save(instance);
        
        // Sync tasks with Flowable engine for this instance
        taskService.syncTasksWithFlowable(caseInstanceId);
        
        return instance;
    }
    
    public List<Workflow> getAllWorkflows() {
        // Return all workflow templates (no tasks at template level)
        return workflowRepository.findAll();
    }
    
    public Workflow getWorkflowById(Long id) {
        // Return workflow template (no tasks at template level)
        return workflowRepository.findById(id).orElse(null);
    }
    
    public Workflow getWorkflowWithTasks(Long id) {
        // Deprecated - workflow templates don't have tasks
        return workflowRepository.findById(id).orElse(null);
    }
    
    public WorkflowInstance getWorkflowInstanceByCaseId(String caseInstanceId) {
        return workflowInstanceRepository.findByCaseInstanceId(caseInstanceId);
    }
    
    public List<WorkflowInstance> getWorkflowInstances(Long workflowId) {
        return workflowInstanceRepository.findByWorkflowId(workflowId);
    }
    
    public Workflow updateWorkflowDefinition(Long workflowId, app.flo.dto.WorkflowDefinitionRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
        if (workflow != null) {
            workflow.setName(request.getName());
            workflow.setFrequency(request.getFrequency());
            if (request.getExecutionTime() != null) {
                workflow.setExecutionTime(java.time.LocalTime.parse(request.getExecutionTime()));
            }
            workflow.setNthWorkingDay(request.getNthWorkingDay());
            return workflowRepository.save(workflow);
        }
        return null;
    }
}