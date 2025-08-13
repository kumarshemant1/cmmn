package app.flo.repository;

import app.flo.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    
    WorkflowInstance findByCaseInstanceId(String caseInstanceId);
    
    List<WorkflowInstance> findByWorkflowId(Long workflowId);
    
    List<WorkflowInstance> findByStatus(WorkflowInstance.InstanceStatus status);
}