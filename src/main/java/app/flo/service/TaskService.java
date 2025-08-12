package app.flo.service;

import app.flo.entity.Task;
import app.flo.entity.TaskStatus;
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
                task.setTaskType(app.flo.entity.TaskType.valueOf(taskDef.getTaskType().toUpperCase()));
            }
            
            // Get actual workflow from repository
            app.flo.entity.Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
            if (workflow != null) {
                task.setWorkflow(workflow);
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
            if (task.getCmmnTaskId() != null) {
                cmmnTaskService.complete(task.getCmmnTaskId());
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
        app.flo.entity.Workflow workflow = workflowRepository.findByCmmnCaseId(caseInstanceId);
        if (workflow != null) {
            return taskRepository.findByWorkflowIdWithFiles(workflow.getId());
        }
        return java.util.Collections.emptyList();
    }
}