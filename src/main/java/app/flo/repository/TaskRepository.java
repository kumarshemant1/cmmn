package app.flo.repository;

import app.flo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByWorkflowId(Long workflowId);

    Task findByCmmnTaskId(String cmmnTaskId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t LEFT JOIN FETCH t.businessFiles WHERE t.workflow.id = :workflowId")
    List<Task> findByWorkflowIdWithFiles(@org.springframework.data.repository.query.Param("workflowId") Long workflowId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t LEFT JOIN FETCH t.businessFiles WHERE t.id = :id")
    java.util.Optional<Task> findByIdWithFiles(@org.springframework.data.repository.query.Param("id") Long id);
}