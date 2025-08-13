package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.entity.Task;
import app.flo.enums.TaskStatus;
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
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkAndTriggerWorkflows() {
        LocalDateTime now = LocalDateTime.now();
        List<Workflow> scheduledWorkflows = workflowRepository.findScheduledWorkflows(now);
        
        for (Workflow workflow : scheduledWorkflows) {
            triggerWorkflowExecution(workflow);
        }
    }
    
    public void triggerWorkflowExecution(Workflow workflow) {
        try {
            // Start new case instance
            String caseInstanceId = cmmnService.triggerWorkflow(workflow.getId());
            
            // Initialize first task
            initializeFirstTask(workflow);
            
            System.out.println("Triggered workflow: " + workflow.getName() + 
                             " with case instance: " + caseInstanceId);
        } catch (Exception e) {
            System.err.println("Failed to trigger workflow " + workflow.getId() + ": " + e.getMessage());
        }
    }
    
    private void initializeFirstTask(Workflow workflow) {
        List<Task> tasks = workflow.getTasks();
        if (tasks != null && !tasks.isEmpty()) {
            Task firstTask = tasks.get(0);
            firstTask.setStatus(TaskStatus.ACTIVE);
            taskService.updateTaskStatus(firstTask.getId(), TaskStatus.ACTIVE);
        }
    }
    
    public void completeTaskAndMoveNext(Long taskId) {
        Task currentTask = taskService.getTaskById(taskId);
        if (currentTask == null) return;
        
        // Complete current task
        taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
        
        // Check if task is part of a group
        if (currentTask.getTaskGroup() != null) {
            // Check if all tasks in group are completed
            if (isTaskGroupCompleted(currentTask.getTaskGroup())) {
                moveToNextTaskGroup(currentTask.getWorkflow());
            }
        } else {
            // Single task - move to next immediately
            moveToNextSingleTask(currentTask);
        }
    }
    
    private boolean isTaskGroupCompleted(String taskGroup) {
        List<Task> groupTasks = taskService.getTasksByGroup(taskGroup);
        return groupTasks.stream().allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);
    }
    
    private void moveToNextTaskGroup(Workflow workflow) {
        List<Task> tasks = workflow.getTasks();
        // Find next ungrouped task or different group and activate
        tasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.ACTIVE)
            .findFirst()
            .ifPresent(nextTask -> taskService.updateTaskStatus(nextTask.getId(), TaskStatus.ACTIVE));
    }
    
    private void moveToNextSingleTask(Task currentTask) {
        Workflow workflow = currentTask.getWorkflow();
        List<Task> tasks = workflow.getTasks();
        
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(currentTask.getId()) && i + 1 < tasks.size()) {
                Task nextTask = tasks.get(i + 1);
                taskService.updateTaskStatus(nextTask.getId(), TaskStatus.ACTIVE);
                break;
            }
        }
    }
}