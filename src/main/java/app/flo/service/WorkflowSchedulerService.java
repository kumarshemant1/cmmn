package app.flo.service;

import app.flo.entity.Workflow;
import app.flo.enums.Frequency;
import app.flo.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WorkflowSchedulerService {
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private CmmnService cmmnService;
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void executeScheduledWorkflows() {
        LocalDateTime now = LocalDateTime.now();
        List<Workflow> workflows = workflowRepository.findAll();
        
        for (Workflow workflow : workflows) {
            if (shouldExecuteWorkflow(workflow, now)) {
                executeWorkflow(workflow);
            }
        }
    }
    
    private boolean shouldExecuteWorkflow(Workflow workflow, LocalDateTime now) {
        if (workflow.getFrequency() == null || workflow.getExecutionTime() == null) {
            return false;
        }
        
        LocalTime currentTime = now.toLocalTime();
        if (!currentTime.equals(workflow.getExecutionTime())) {
            return false;
        }
        
        switch (workflow.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                return isNthWorkingDayOfWeek(now, workflow.getNthWorkingDay());
            case MONTHLY:
                return isNthWorkingDayOfMonth(now, workflow.getNthWorkingDay());
            default:
                return false;
        }
    }
    
    private boolean isNthWorkingDayOfWeek(LocalDateTime date, Integer nthDay) {
        if (nthDay == null) return false;
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        return dayOfWeek <= 5 && dayOfWeek == nthDay; // 1-5 for Mon-Fri
    }
    
    private boolean isNthWorkingDayOfMonth(LocalDateTime date, Integer nthDay) {
        if (nthDay == null) return false;
        // Calculate working day of month (simplified - excludes weekends only)
        int workingDay = 0;
        for (int day = 1; day <= date.getDayOfMonth(); day++) {
            LocalDateTime checkDate = date.withDayOfMonth(day);
            if (checkDate.getDayOfWeek().getValue() <= 5) { // Mon-Fri
                workingDay++;
            }
        }
        return workingDay == nthDay;
    }
    
    private void executeWorkflow(Workflow workflow) {
        try {
            cmmnService.triggerWorkflow(workflow.getId());
            System.out.println("Executed scheduled workflow: " + workflow.getName());
        } catch (Exception e) {
            System.err.println("Failed to execute workflow " + workflow.getName() + ": " + e.getMessage());
        }
    }
}