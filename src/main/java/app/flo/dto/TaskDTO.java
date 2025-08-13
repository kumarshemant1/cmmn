package app.flo.dto;

import org.flowable.task.api.Task;

public class TaskDTO {
    private String flowableTaskId;
    private String name;
    private String assignee;
    private String caseInstanceId;
    private String taskDefinitionKey;
    private java.time.LocalDateTime createTime;
    
    public TaskDTO() {}
    
    public TaskDTO(Task task) {
        this.flowableTaskId = task.getId();
        this.name = task.getName();
        this.assignee = task.getAssignee();
        this.caseInstanceId = task.getScopeId();
        this.taskDefinitionKey = task.getTaskDefinitionKey();
        this.createTime = task.getCreateTime() != null ? 
            task.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
    }
    
    public String getFlowableTaskId() { return flowableTaskId; }
    public void setFlowableTaskId(String flowableTaskId) { this.flowableTaskId = flowableTaskId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    
    public String getCaseInstanceId() { return caseInstanceId; }
    public void setCaseInstanceId(String caseInstanceId) { this.caseInstanceId = caseInstanceId; }
    
    public String getTaskDefinitionKey() { return taskDefinitionKey; }
    public void setTaskDefinitionKey(String taskDefinitionKey) { this.taskDefinitionKey = taskDefinitionKey; }
    
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}