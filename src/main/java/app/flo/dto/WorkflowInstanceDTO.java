package app.flo.dto;

import app.flo.entity.WorkflowInstance;

public class WorkflowInstanceDTO {
    private Long id;
    private String caseInstanceId;
    private Long workflowId;
    private String workflowName;
    private java.time.LocalDateTime startedAt;
    
    public WorkflowInstanceDTO() {}
    
    public WorkflowInstanceDTO(WorkflowInstance instance) {
        this.id = instance.getId();
        this.caseInstanceId = instance.getCaseInstanceId();
        this.workflowId = instance.getWorkflow() != null ? instance.getWorkflow().getId() : null;
        this.workflowName = instance.getWorkflow() != null ? instance.getWorkflow().getName() : null;
        this.startedAt = instance.getStartedAt();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCaseInstanceId() { return caseInstanceId; }
    public void setCaseInstanceId(String caseInstanceId) { this.caseInstanceId = caseInstanceId; }
    
    public Long getWorkflowId() { return workflowId; }
    public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }
    
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    
    public java.time.LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(java.time.LocalDateTime startedAt) { this.startedAt = startedAt; }
}