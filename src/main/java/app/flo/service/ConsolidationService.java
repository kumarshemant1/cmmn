package app.flo.service;

import app.flo.entity.BusinessFile;
import app.flo.entity.Task;
import app.flo.enums.TaskType;
import app.flo.repository.BusinessFileRepository;
import app.flo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ConsolidationService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private BusinessFileRepository businessFileRepository;
    
    private final String consolidatedDir = "uploads/consolidated/";
    
    public BusinessFile consolidateFiles(Long workflowId) throws IOException {
        List<Task> uploadTasks = taskRepository.findByWorkflowIdWithFiles(workflowId)
            .stream()
            .filter(task -> task.getTaskType() == TaskType.UPLOAD)
            .toList();
        
        Path consolidatedPath = Paths.get(consolidatedDir);
        if (!Files.exists(consolidatedPath)) {
            Files.createDirectories(consolidatedPath);
        }
        
        String consolidatedFileName = "consolidated_workflow_" + workflowId + ".txt";
        Path consolidatedFile = consolidatedPath.resolve(consolidatedFileName);
        
        StringBuilder content = new StringBuilder();
        content.append("=== CONSOLIDATED REPORT FOR WORKFLOW ").append(workflowId).append(" ===\n\n");
        
        for (Task task : uploadTasks) {
            content.append("--- Task: ").append(task.getName());
            if (task.getAssignee() != null) {
                content.append(" (Assignee: ").append(task.getAssignee()).append(")");
            }
            content.append(" ---\n");
            
            List<BusinessFile> files = businessFileRepository.findByTaskId(task.getId());
            for (BusinessFile file : files) {
                content.append("File: ").append(file.getBusinessFileName()).append("\n");
                content.append(Files.readString(Paths.get(file.getFilePath())));
                content.append("\n\n");
            }
        }
        
        Files.write(consolidatedFile, content.toString().getBytes());
        
        BusinessFile consolidatedBusinessFile = new BusinessFile();
        consolidatedBusinessFile.setBusinessFileName(consolidatedFileName);
        consolidatedBusinessFile.setFilePath(consolidatedFile.toString());
        consolidatedBusinessFile.setFileSize((long) content.length());
        consolidatedBusinessFile.setContentType("text/plain");
        consolidatedBusinessFile.setRetainFile(true);
        consolidatedBusinessFile.setKeepVersion(true);
        consolidatedBusinessFile.setKeepHistory(true);
        
        return businessFileRepository.save(consolidatedBusinessFile);
    }
}