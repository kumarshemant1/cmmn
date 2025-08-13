package app.flo.repository;

import app.flo.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    Workflow findByCaseInstanceId(String caseInstanceId);
    
    @org.springframework.data.jpa.repository.Query("SELECT w FROM Workflow w LEFT JOIN FETCH w.tasks WHERE w.id = :id")
    java.util.Optional<Workflow> findByIdWithTasks(@org.springframework.data.repository.query.Param("id") Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.tasks")
    java.util.List<Workflow> findAllWithTasksAndFiles();
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.tasks WHERE w.id = :id")
    java.util.Optional<Workflow> findByIdWithTasksAndFiles(@org.springframework.data.repository.query.Param("id") Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.tasks t LEFT JOIN FETCH t.businessFiles WHERE w.id = :id")
    java.util.Optional<Workflow> findByIdWithTasksAndFilesComplete(@org.springframework.data.repository.query.Param("id") Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT w FROM Workflow w LEFT JOIN FETCH w.tasks WHERE w.scheduledTime <= :now AND w.frequency IS NOT NULL")
    java.util.List<Workflow> findScheduledWorkflows(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}