package app.flo.repository;

import app.flo.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    

    
    @org.springframework.data.jpa.repository.Query("SELECT w FROM Workflow w WHERE w.scheduledTime <= :now AND w.frequency IS NOT NULL")
    java.util.List<Workflow> findScheduledWorkflows(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}