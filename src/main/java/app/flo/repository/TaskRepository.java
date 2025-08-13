package app.flo.repository;

import app.flo.entity.TaskMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskMetadata, Long> {

    List<TaskMetadata> findByWorkflowInstanceId(Long workflowInstanceId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskMetadata t LEFT JOIN FETCH t.businessFiles WHERE t.workflowInstance.id = :workflowInstanceId")
    List<TaskMetadata> findByWorkflowInstanceIdWithFiles(@org.springframework.data.repository.query.Param("workflowInstanceId") Long workflowInstanceId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskMetadata t LEFT JOIN FETCH t.businessFiles WHERE t.id = :id")
    java.util.Optional<TaskMetadata> findByIdWithFiles(@org.springframework.data.repository.query.Param("id") Long id);
    
    List<TaskMetadata> findByAssignee(String assignee);
    
    List<TaskMetadata> findByWorkflowInstanceIdAndTaskType(Long workflowInstanceId, app.flo.enums.TaskType taskType);
    
    List<TaskMetadata> findByTaskGroup(String taskGroup);
    
    List<TaskMetadata> findByName(String name);
    
    TaskMetadata findByTaskId(String taskId);
    
    List<TaskMetadata> findByCaseInstanceId(String caseInstanceId);
}