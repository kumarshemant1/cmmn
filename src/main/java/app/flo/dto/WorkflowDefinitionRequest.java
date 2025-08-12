package app.flo.dto;

import app.flo.enums.Frequency;
import java.util.List;
import java.util.Map;

public class WorkflowDefinitionRequest {
    private String name;
    private String scheduledTime;
    private Frequency frequency;
    private String executionTime;
    private Integer nthWorkingDay;
    private Map<String, Object> drawflowData;
    private List<TaskDefinition> tasks;
    
    public static class TaskDefinition {
        private String id;
        private String name;
        private String taskType;
        private Map<String, Object> position;
        private List<String> connections;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public Map<String, Object> getPosition() { return position; }
        public void setPosition(Map<String, Object> position) { this.position = position; }
        
        public List<String> getConnections() { return connections; }
        public void setConnections(List<String> connections) { this.connections = connections; }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    
    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }
    
    public Integer getNthWorkingDay() { return nthWorkingDay; }
    public void setNthWorkingDay(Integer nthWorkingDay) { this.nthWorkingDay = nthWorkingDay; }
    
    public Map<String, Object> getDrawflowData() { return drawflowData; }
    public void setDrawflowData(Map<String, Object> drawflowData) { this.drawflowData = drawflowData; }
    
    public List<TaskDefinition> getTasks() { return tasks; }
    public void setTasks(List<TaskDefinition> tasks) { this.tasks = tasks; }
}