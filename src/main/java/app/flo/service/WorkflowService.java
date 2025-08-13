package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.repository.WorkflowRepository;
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
        workflow = workflowRepository.save(workflow);
        
        // Ensure every workflow has a case instance ID
        String caseInstanceId = cmmnService.createCaseInstance(workflow.getId());
        if (caseInstanceId == null) {
            throw new RuntimeException("Failed to create case instance for workflow");
        }
        
        // Update workflow with case instance ID
        workflow.setCaseInstanceId(caseInstanceId);
        workflow = workflowRepository.save(workflow);
        
        return workflow;
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
        System.out.println("Workflow saved with ID: " + workflow.getId());
        
        // Generate CMMN content (in-memory)
        try {
            String cmmnContent = cmmnGeneratorService.generateCmmnContent(request);
            System.out.println("CMMN content generated for workflow: " + workflow.getName());
        } catch (Exception e) {
            System.err.println("Failed to generate CMMN content: " + e.getMessage());
        }
        
        // Create case instance
        String caseInstanceId = cmmnService.createCaseInstance(workflow.getId());
        System.out.println("Case instance created: " + caseInstanceId);
        
        // Update workflow with case instance ID
        workflow.setCaseInstanceId(caseInstanceId);
        workflow = workflowRepository.save(workflow);
        
        // Create tasks
        if (request.getTasks() != null) {
            System.out.println("Creating " + request.getTasks().size() + " tasks");
            for (app.flo.dto.WorkflowDefinitionRequest.TaskDefinition taskDef : request.getTasks()) {
                taskService.createTaskFromDefinition(taskDef, workflow.getId());
            }
        }
        
        return workflow;
    }
    
    public String startWorkflow(Long workflowId) {
        return cmmnService.createCaseInstance(workflowId);
    }
    
    public List<Workflow> getAllWorkflows() {
        List<Workflow> workflows = workflowRepository.findAllWithTasksAndFiles();
        // Null-safe initialization of business files for all workflows
        workflows.forEach(workflow -> {
            if (workflow.getTasks() != null) {
                workflow.getTasks().forEach(task -> {
                    if (task.getBusinessFiles() != null) {
                        task.getBusinessFiles().size();
                    }
                });
            }
        });
        return workflows;
    }
    
    public Workflow getWorkflowById(Long id) {
        java.util.Optional<Workflow> workflow = workflowRepository.findByIdWithTasks(id);
        if (workflow.isPresent()) {
            // Null-safe initialization of business files
            if (workflow.get().getTasks() != null) {
                workflow.get().getTasks().forEach(task -> {
                    if (task.getBusinessFiles() != null) {
                        task.getBusinessFiles().size();
                    }
                });
            }
        }
        return workflow.orElse(null);
    }
    
    public Workflow getWorkflowWithTasks(Long id) {
        return workflowRepository.findByIdWithTasks(id).orElse(null);
    }
    
    public Workflow getWorkflowByCaseId(String caseInstanceId) {
        Workflow workflow = workflowRepository.findByCaseInstanceId(caseInstanceId);
        if (workflow != null && workflow.getTasks() != null) {
            // Null-safe initialization of tasks and files
            workflow.getTasks().forEach(task -> {
                if ( task != null && task.getBusinessFiles() != null) {
                    task.getBusinessFiles().size();
                }
            });
        }
        return workflow;
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