package app.flo.service;

import app.flo.entity.Task;
import app.flo.enums.TaskStatus;
import app.flo.enums.TaskType;
import app.flo.repository.TaskRepository;
import org.flowable.cmmn.api.CmmnTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private CmmnTaskService cmmnTaskService;
    
    public Task createTask(String name, Long workflowId) {
        Task task = new Task();
        task.setName(name);
        return taskRepository.save(task);
    }
    
    public Task createTaskFromDefinition(app.flo.dto.WorkflowDefinitionRequest.TaskDefinition taskDef, Long workflowId) {
        try {
            Task task = new Task();
            task.setName(taskDef.getName());
            
            // Safely parse task type
            if (taskDef.getTaskType() != null) {
                task.setTaskType(TaskType.valueOf(taskDef.getTaskType().toUpperCase()));
            }
            
            // Set assignee and taskGroup if provided
            if (taskDef.getAssignee() != null) {
                task.setAssignee(taskDef.getAssignee());
            }
            if (taskDef.getTaskGroup() != null) {
                task.setTaskGroup(taskDef.getTaskGroup());
            }
            
            // Get actual workflow from repository
            app.flo.entity.Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
            if (workflow != null) {
                task.setWorkflow(workflow);
                
                // Create Flowable task if case instance exists
                if (workflow.getCaseInstanceId() != null) {
                    try {
                        // Query existing Flowable tasks for this case
                        List<org.flowable.task.api.Task> flowableTasks = cmmnTaskService.createTaskQuery()
                            .caseInstanceId(workflow.getCaseInstanceId())
                            .taskName(taskDef.getName())
                            .list();
                        
                        if (!flowableTasks.isEmpty()) {
                            // Use existing Flowable task ID
                            task.setTaskId(flowableTasks.get(0).getId());
                        } else {
                            // Fallback to manual ID if Flowable task not found
                            task.setTaskId("task_" + taskDef.getId() + "_" + workflowId);
                        }
                    } catch (Exception e) {
                        // Fallback to manual ID if Flowable query fails
                        task.setTaskId("task_" + taskDef.getId() + "_" + workflowId);
                        System.err.println("Failed to query Flowable tasks, using fallback ID: " + e.getMessage());
                    }
                } else {
                    // No case instance yet, use manual ID
                    task.setTaskId("task_" + taskDef.getId() + "_" + workflowId);
                }
            }
            
            return taskRepository.save(task);
        } catch (Exception e) {
            System.err.println("Failed to create task: " + e.getMessage());
            throw e;
        }
    }
    
    public List<Task> getTasksByWorkflow(Long workflowId) {
        return taskRepository.findByWorkflowIdWithFiles(workflowId);
    }
    
    public Task completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            if (task.getTaskId() != null) {
                cmmnTaskService.complete(task.getTaskId());
            }
            return taskRepository.save(task);
        }
        return null;
    }
    
    public Task getTaskById(Long id) {
        return taskRepository.findByIdWithFiles(id).orElse(null);
    }
    
    @Autowired
    private FileService fileService;
    
    public app.flo.entity.BusinessFile uploadFileToTask(Long taskId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        return fileService.uploadFile(taskId, file);
    }
    
    public app.flo.entity.BusinessFile uploadFileToTask(Long taskId, org.springframework.web.multipart.MultipartFile file, Boolean retainFile, Boolean keepVersion, Boolean keepHistory) throws java.io.IOException {
        return fileService.uploadFile(taskId, file, retainFile, keepVersion, keepHistory);
    }
    
    public java.util.List<app.flo.entity.BusinessFile> getTaskFiles(Long taskId) {
        return fileService.getFilesByTask(taskId);
    }
    
    @Autowired
    private app.flo.service.ConsolidationService consolidationService;
    
    @Autowired
    private app.flo.repository.WorkflowRepository workflowRepository;
    
    public app.flo.entity.BusinessFile consolidateWorkflowFiles(Long taskId) throws java.io.IOException {
        Task task = getTaskById(taskId);
        if (task != null && task.getWorkflow() != null) {
            return consolidationService.consolidateFiles(task.getWorkflow().getId());
        }
        return null;
    }
    
    public java.util.List<Task> getTasksByCaseId(String caseInstanceId) {
        app.flo.entity.Workflow workflow = workflowRepository.findByCaseInstanceId(caseInstanceId);
        if (workflow != null) {
            return taskRepository.findByWorkflowIdWithFiles(workflow.getId());
        }
        return java.util.Collections.emptyList();
    }
    
    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(status);
            return taskRepository.save(task);
        }
        return null;
    }
    
    public List<Task> getTasksByGroup(String taskGroup) {
        return taskRepository.findByTaskGroup(taskGroup);
    }
    
    public Task assignTask(Long taskId, String assignee) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setAssignee(assignee);
            return taskRepository.save(task);
        }
        return null;
    }
    
    public app.flo.entity.Workflow getWorkflowById(Long workflowId) {
        return workflowRepository.findById(workflowId).orElse(null);
    }
    
    public void syncTasksWithFlowable(Long workflowId) {
        app.flo.entity.Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
        if (workflow != null && workflow.getCaseInstanceId() != null) {
            try {
                // Get Flowable tasks for this case instance
                List<org.flowable.task.api.Task> flowableTasks = cmmnTaskService.createTaskQuery()
                    .caseInstanceId(workflow.getCaseInstanceId())
                    .list();
                
                // Update our tasks with Flowable task IDs
                List<Task> ourTasks = taskRepository.findByWorkflowId(workflowId);
                for (Task ourTask : ourTasks) {
                    for (org.flowable.task.api.Task flowableTask : flowableTasks) {
                        if (ourTask.getName().equals(flowableTask.getName())) {
                            ourTask.setTaskId(flowableTask.getId());
                            if (flowableTask.getAssignee() != null) {
                                ourTask.setAssignee(flowableTask.getAssignee());
                            }
                            taskRepository.save(ourTask);
                            break;
                        }
                    }
                }
                System.out.println("Synced " + ourTasks.size() + " tasks with Flowable engine");
            } catch (Exception e) {
                System.err.println("Failed to sync tasks with Flowable: " + e.getMessage());
            }
        }
    }
}