package app.flo.controller;

import app.flo.entity.BusinessFile;
import app.flo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload/{taskId}")
    public ResponseEntity<BusinessFile> uploadFile(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) {
        try {
            BusinessFile businessFile = fileService.uploadFile(taskId, file);
            return businessFile != null ? ResponseEntity.ok(businessFile) : ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<BusinessFile>> getFilesByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(fileService.getFilesByTask(taskId));
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        BusinessFile businessFile = fileService.getFileById(id);
        if (businessFile == null) return ResponseEntity.notFound().build();
        
        Resource resource = new FileSystemResource(businessFile.getFilePath());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + businessFile.getFileName() + "\"")
            .body(resource);
    }
}