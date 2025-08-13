package app.flo.entity;

import app.flo.enums.Frequency;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "workflows")
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    private Frequency frequency;
    
    @Column(name = "execution_time")
    private LocalTime executionTime;
    
    @Column(name = "nth_working_day")
    private Integer nthWorkingDay;
    
    @Column(name = "drawflow_data", columnDefinition = "TEXT")
    private String drawflowData;
    


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    

    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    

    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    
    public LocalTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalTime executionTime) { this.executionTime = executionTime; }
    
    public Integer getNthWorkingDay() { return nthWorkingDay; }
    public void setNthWorkingDay(Integer nthWorkingDay) { this.nthWorkingDay = nthWorkingDay; }
    
    public String getDrawflowData() { return drawflowData; }
    public void setDrawflowData(String drawflowData) { this.drawflowData = drawflowData; }
}