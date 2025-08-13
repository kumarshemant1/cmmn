package app.flo.service;

import app.flo.entity.BusinessFile;
import app.flo.entity.TaskMetadata;
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
        System.out.println("FileService.uploadFile called - TaskId: " + taskId);
        
        TaskMetadata task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            System.err.println("Task not found with ID: " + taskId);
            return null;
        }
        
        System.out.println("Task found: " + task.getName());
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath);
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("File name is null or empty");
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        System.out.println("File saved to: " + filePath);
        
        BusinessFile businessFile = new BusinessFile();
        businessFile.setBusinessFileName(fileName);
        businessFile.setFilePath(filePath.toString());
        businessFile.setContentType(file.getContentType());
        businessFile.setFileSize(file.getSize());
        businessFile.setTask(task);
        businessFile.setRetainFile(retainFile != null ? retainFile : false);
        businessFile.setKeepVersion(keepVersion != null ? keepVersion : false);
        businessFile.setKeepHistory(keepHistory != null ? keepHistory : false);
        
        BusinessFile saved = businessFileRepository.save(businessFile);
        System.out.println("BusinessFile saved with ID: " + saved.getId());
        return saved;
    }
    
    public List<BusinessFile> getFilesByTask(Long taskId) {
        return businessFileRepository.findByTaskId(taskId);
    }
    
    public BusinessFile getFileById(Long id) {
        return businessFileRepository.findById(id).orElse(null);
    }
    
    public java.util.List<BusinessFile> getFilesByCaseId(String caseInstanceId) {
        // Get files by case instance ID directly
        List<TaskMetadata> tasks = taskRepository.findByCaseInstanceId(caseInstanceId);
        return tasks.stream()
            .flatMap(task -> businessFileRepository.findByTaskId(task.getId()).stream())
            .collect(java.util.stream.Collectors.toList());
    }
}