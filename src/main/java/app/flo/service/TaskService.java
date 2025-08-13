package app.flo.service;

import app.flo.entity.TaskMetadata;
import app.flo.enums.TaskType;
import app.flo.repository.TaskRepository;
import app.flo.repository.WorkflowInstanceRepository;
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
    
    public TaskMetadata createTask(String name, Long workflowInstanceId) {
        TaskMetadata task = new TaskMetadata();
        task.setName(name);
        return taskRepository.save(task);
    }
    
    // This method is deprecated - use FlowableTaskService.createTaskMetadata instead
    @Deprecated
    public TaskMetadata createTaskFromDefinition(app.flo.dto.WorkflowDefinitionRequest.TaskDefinition taskDef, Long workflowId) {
        // Template creation only - no Flowable integration at template level
        TaskMetadata task = new TaskMetadata();
        task.setName(taskDef.getName());
        
        if (taskDef.getTaskType() != null) {
            task.setTaskType(TaskType.valueOf(taskDef.getTaskType().toUpperCase()));
        }
        if (taskDef.getAssignee() != null) {
            task.setAssignee(taskDef.getAssignee());
        }
        if (taskDef.getTaskGroup() != null) {
            task.setTaskGroup(taskDef.getTaskGroup());
        }
        
        return taskRepository.save(task);
    }
    
    public List<TaskMetadata> getTasksByWorkflowInstance(Long workflowInstanceId) {
        return taskRepository.findByWorkflowInstanceIdWithFiles(workflowInstanceId);
    }
    
    public TaskMetadata completeTask(Long taskId) {
        TaskMetadata task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getTaskId() != null) {
            // Complete task in Flowable engine
            cmmnTaskService.complete(task.getTaskId());
            
            // Update completion timestamp
            task.setCompletedAt(java.time.LocalDateTime.now());
            return taskRepository.save(task);
        }
        return null;
    }
    
    public TaskMetadata getTaskById(Long id) {
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
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    public app.flo.entity.BusinessFile consolidateWorkflowFiles(Long taskId) throws java.io.IOException {
        TaskMetadata task = getTaskById(taskId);
        if (task != null && task.getWorkflowInstance() != null) {
            return consolidationService.consolidateFiles(task.getWorkflowInstance().getId());
        }
        return null;
    }
    
    public java.util.List<TaskMetadata> getTasksByCaseId(String caseInstanceId) {
        return taskRepository.findByCaseInstanceId(caseInstanceId);
    }
    
    public String getTaskStatus(Long taskId) {
        TaskMetadata task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getTaskId() != null) {
            try {
                org.flowable.task.api.Task flowableTask = cmmnTaskService.createTaskQuery()
                    .taskId(task.getTaskId())
                    .singleResult();
                
                if (flowableTask != null) {
                    return "ACTIVE";
                } else {
                    return task.getCompletedAt() != null ? "COMPLETED" : "TERMINATED";
                }
            } catch (Exception e) {
                return "UNKNOWN";
            }
        }
        return "NOT_STARTED";
    }
    
    public boolean isTaskCompleted(Long taskId) {
        TaskMetadata task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getTaskId() != null) {
            org.flowable.task.api.Task flowableTask = cmmnTaskService.createTaskQuery()
                .taskId(task.getTaskId())
                .singleResult();
            return flowableTask == null;
        }
        return false;
    }
    
    public List<TaskMetadata> getTasksByGroup(String taskGroup) {
        return taskRepository.findByTaskGroup(taskGroup);
    }
    
    public TaskMetadata assignTask(Long taskId, String assignee) {
        TaskMetadata task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setAssignee(assignee);
            return taskRepository.save(task);
        }
        return null;
    }
    
    public app.flo.entity.Workflow getWorkflowById(Long workflowId) {
        return workflowRepository.findById(workflowId).orElse(null);
    }
    
    public void syncTasksWithFlowable(String caseInstanceId) {
        if (caseInstanceId != null) {
            try {
                // Get Flowable tasks for this case instance
                List<org.flowable.task.api.Task> flowableTasks = cmmnTaskService.createTaskQuery()
                    .caseInstanceId(caseInstanceId)
                    .list();
                
                System.out.println("Found " + flowableTasks.size() + " active Flowable tasks for case: " + caseInstanceId);
                
                // Get workflow instance
                app.flo.entity.WorkflowInstance workflowInstance = workflowInstanceRepository.findByCaseInstanceId(caseInstanceId);
                if (workflowInstance == null) {
                    System.err.println("WorkflowInstance not found for case: " + caseInstanceId);
                    return;
                }
                
                // Create TaskMetadata for each Flowable task
                for (org.flowable.task.api.Task flowableTask : flowableTasks) {
                    // Check if TaskMetadata already exists
                    TaskMetadata existingTask = taskRepository.findByTaskId(flowableTask.getId());
                    if (existingTask == null) {
                        // Create new TaskMetadata
                        TaskMetadata newTask = new TaskMetadata();
                        newTask.setTaskId(flowableTask.getId());
                        newTask.setName(flowableTask.getName());
                        newTask.setCaseInstanceId(caseInstanceId);
                        newTask.setWorkflowInstance(workflowInstance);
                        newTask.setTaskType(TaskType.UPLOAD); // Default type
                        if (flowableTask.getAssignee() != null) {
                            newTask.setAssignee(flowableTask.getAssignee());
                        }
                        taskRepository.save(newTask);
                        System.out.println("Created TaskMetadata: " + newTask.getName() + " with Flowable ID: " + flowableTask.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to sync tasks with Flowable: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public List<org.flowable.task.api.Task> getActiveFlowableTasks(String caseInstanceId) {
        return cmmnTaskService.createTaskQuery()
            .caseInstanceId(caseInstanceId)
            .list();
    }
    
    public TaskMetadata getTaskMetadataByFlowableTaskId(String flowableTaskId) {
        return taskRepository.findByTaskId(flowableTaskId);
    }
}