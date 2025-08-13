package app.flo.service;

import app.flo.entity.BusinessFile;
import app.flo.entity.Task;
import app.flo.repository.BusinessFileRepository;
import app.flo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileService {
    
    @Autowired
    private BusinessFileRepository businessFileRepository;
    
    @Autowired
    private app.flo.repository.WorkflowRepository workflowRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    private final String uploadDir = "uploads/";
    
    public BusinessFile uploadFile(Long taskId, MultipartFile file) throws IOException {
        return uploadFile(taskId, file, false, false, false);
    }
    
    public BusinessFile uploadFile(Long taskId, MultipartFile file, Boolean retainFile, Boolean keepVersion, Boolean keepHistory) throws IOException {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return null;
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        BusinessFile businessFile = new BusinessFile();
        businessFile.setBusinessFileName(fileName);
        businessFile.setFilePath(filePath.toString());
        businessFile.setContentType(file.getContentType());
        businessFile.setFileSize(file.getSize());
        businessFile.setTask(task);
        businessFile.setRetainFile(retainFile != null ? retainFile : false);
        businessFile.setKeepVersion(keepVersion != null ? keepVersion : false);
        businessFile.setKeepHistory(keepHistory != null ? keepHistory : false);
        
        return businessFileRepository.save(businessFile);
    }
    
    public List<BusinessFile> getFilesByTask(Long taskId) {
        return businessFileRepository.findByTaskId(taskId);
    }
    
    public BusinessFile getFileById(Long id) {
        return businessFileRepository.findById(id).orElse(null);
    }
    
    public java.util.List<BusinessFile> getFilesByCaseId(String caseInstanceId) {
        // Get workflow by case ID, then get all files from all tasks
        app.flo.entity.Workflow workflow = workflowRepository.findByCaseInstanceId(caseInstanceId);
        if (workflow != null) {
            return workflow.getTasks().stream()
                .flatMap(task -> businessFileRepository.findByTaskId(task.getId()).stream())
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}