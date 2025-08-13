package app.flo.service;

import app.flo.dto.WorkflowDefinitionRequest;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CmmnGeneratorService {
    
    private final String cmmnDir = "src/main/resources/processes/";
    
    public String generateCmmnContent(WorkflowDefinitionRequest request) {
        String cmmnContent = buildCmmnXml(request);
        saveCmmnFile(request.getName(), cmmnContent);
        return cmmnContent;
    }
    
    private String buildCmmnXml(WorkflowDefinitionRequest request) {
        StringBuilder cmmn = new StringBuilder();
        
        cmmn.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        cmmn.append("<definitions xmlns=\"http://www.omg.org/spec/CMMN/20151109/MODEL\" \n");
        cmmn.append("             xmlns:flowable=\"http://flowable.org/cmmn\"\n");
        cmmn.append("             targetNamespace=\"http://flowable.org/cmmn\">\n\n");
        
        String caseId = request.getName().replaceAll("\\s+", "").toLowerCase() + "Case";
        cmmn.append("    <case id=\"").append(caseId).append("\" name=\"").append(request.getName()).append("\">\n");
        cmmn.append("        <casePlanModel id=\"casePlanModel\">\n");
        
        // Generate plan items
        if (request.getTasks() != null) {
            for (WorkflowDefinitionRequest.TaskDefinition task : request.getTasks()) {
                String planItemId = "planItem_" + task.getId();
                String taskId = "task_" + task.getId();
                cmmn.append("            <planItem id=\"").append(planItemId).append("\" definitionRef=\"").append(taskId).append("\"/>\n");
            }
            
            cmmn.append("\n");
            
            // Generate human tasks with proper IDs
            for (WorkflowDefinitionRequest.TaskDefinition task : request.getTasks()) {
                String taskId = "task_" + task.getId();
                String taskKey = caseId + "_" + task.getId(); // Unique task key
                cmmn.append("            <humanTask id=\"").append(taskId)
                    .append("\" name=\"").append(task.getName())
                    .append("\" flowable:assignee=\"user\"")
                    .append(" flowable:formKey=\"").append(taskKey).append("\"/>\n");
            }
        }
        
        cmmn.append("        </casePlanModel>\n");
        cmmn.append("    </case>\n");
        cmmn.append("</definitions>");
        
        return cmmn.toString();
    }
    
    private void saveCmmnFile(String workflowName, String cmmnContent) {
        try {
            Path cmmnPath = Paths.get(cmmnDir);
            if (!Files.exists(cmmnPath)) {
                Files.createDirectories(cmmnPath);
            }
            
            String fileName = workflowName.replaceAll("\\s+", "_").toLowerCase() + ".cmmn";
            Path filePath = cmmnPath.resolve(fileName);
            
            Files.write(filePath, cmmnContent.getBytes());
            System.out.println("CMMN file saved: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save CMMN file: " + e.getMessage());
        }
    }
    
    public String getCmmnContent(String workflowName) {
        try {
            String fileName = workflowName.replaceAll("\\s+", "_").toLowerCase() + ".cmmn";
            Path filePath = Paths.get(cmmnDir).resolve(fileName);
            
            if (Files.exists(filePath)) {
                return Files.readString(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to read CMMN file: " + e.getMessage());
        }
        return null;
    }
}