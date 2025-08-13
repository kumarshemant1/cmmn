package app.flo.service;

import app.flo.entity.TaskMetadata;
import app.flo.entity.WorkflowInstance;
import app.flo.repository.TaskRepository;
import app.flo.repository.WorkflowInstanceRepository;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class FlowableTaskService {
    
    @Autowired
    private CmmnTaskService cmmnTaskService;
    
    @Autowired
    private TaskRepository taskMetadataRepository;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    // Primary task operations using Flowable API
    public List<Task> getTasksByCaseInstance(String caseInstanceId) {
        return cmmnTaskService.createTaskQuery()
            .caseInstanceId(caseInstanceId)
            .list();
    }
    
    public Task getFlowableTask(String taskId) {
        return cmmnTaskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();
    }
    
    public void completeTask(String taskId) {
        cmmnTaskService.complete(taskId);
    }
    
    public void completeTask(String taskId, Map<String, Object> variables) {
        cmmnTaskService.complete(taskId, variables);
    }
    
    public void assignTask(String taskId, String assignee) {
        cmmnTaskService.setAssignee(taskId, assignee);
    }
    
    public List<Task> getTasksByAssignee(String assignee) {
        return cmmnTaskService.createTaskQuery()
            .taskAssignee(assignee)
            .list();
    }
    
    public List<Task> getActiveTasks() {
        return cmmnTaskService.createTaskQuery()
            .active()
            .list();
    }
    
    // Business metadata operations
    public TaskMetadata getTaskMetadata(String flowableTaskId) {
        return taskMetadataRepository.findByTaskId(flowableTaskId);
    }
    
    public TaskMetadata saveTaskMetadata(TaskMetadata metadata) {
        return taskMetadataRepository.save(metadata);
    }
    
    // Combined operations following standard Flowable pattern
    public Task getTaskWithMetadata(String taskId) {
        Task flowableTask = getFlowableTask(taskId);
        if (flowableTask != null) {
            TaskMetadata metadata = getTaskMetadata(taskId);
            // Flowable task is primary, metadata is supplementary
            return flowableTask;
        }
        return null;
    }
    
    public TaskMetadata createTaskMetadata(String flowableTaskId, String caseInstanceId, app.flo.enums.TaskType taskType) {
        WorkflowInstance instance = workflowInstanceRepository.findByCaseInstanceId(caseInstanceId);
        if (instance != null) {
            TaskMetadata metadata = new TaskMetadata();
            metadata.setTaskId(flowableTaskId);
            metadata.setCaseInstanceId(caseInstanceId);
            metadata.setWorkflowInstance(instance);
            metadata.setTaskType(taskType);
            
            // Get task name from Flowable
            Task flowableTask = getFlowableTask(flowableTaskId);
            if (flowableTask != null) {
                metadata.setName(flowableTask.getName());
                metadata.setAssignee(flowableTask.getAssignee());
            }
            
            return saveTaskMetadata(metadata);
        }
        return null;
    }
    
    public void completeTaskWithMetadata(String taskId) {
        // Update metadata completion time
        TaskMetadata metadata = getTaskMetadata(taskId);
        if (metadata != null) {
            metadata.setCompletedAt(java.time.LocalDateTime.now());
            saveTaskMetadata(metadata);
        }
        
        // Complete in Flowable (primary operation)
        completeTask(taskId);
    }
}