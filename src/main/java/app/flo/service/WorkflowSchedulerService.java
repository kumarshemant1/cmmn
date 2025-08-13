package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.entity.TaskMetadata;
import app.flo.entity.WorkflowInstance;
import app.flo.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowSchedulerService {
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private CmmnService cmmnService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private WorkflowService workflowService;
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkAndTriggerWorkflows() {
        LocalDateTime now = LocalDateTime.now();
        List<Workflow> scheduledWorkflows = workflowRepository.findScheduledWorkflows(now);
        
        for (Workflow workflow : scheduledWorkflows) {
            try {
                triggerWorkflowExecution(workflow);
            } catch (Exception e) {
                System.err.println("Failed to trigger scheduled workflow " + workflow.getId() + ": " + e.getMessage());
            }
        }
    }
    
    public WorkflowInstance triggerWorkflowExecution(Workflow workflow) {
        try {
            // Create workflow instance (this will create case instance)
            WorkflowInstance instance = workflowService.startWorkflow(workflow.getId());
            
            System.out.println("Triggered workflow: " + workflow.getName() + 
                             " with case instance: " + instance.getCaseInstanceId());
            return instance;
        } catch (Exception e) {
            System.err.println("Failed to trigger workflow " + workflow.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to trigger workflow: " + e.getMessage(), e);
        }
    }
    

    
    public void completeTaskAndMoveNext(Long taskId) {
        TaskMetadata currentTask = taskService.getTaskById(taskId);
        if (currentTask == null) return;
        
        // Complete current task in Flowable
        taskService.completeTask(taskId);
        
        System.out.println("Completed task: " + currentTask.getName());
    }
    

}