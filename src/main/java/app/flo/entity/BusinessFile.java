package app.flo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_files")
public class BusinessFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "business_file_name", nullable = false)
    private String businessFileName;
    
    @Column(nullable = false)
    private String filePath;
    
    private String contentType;
    
    private Long fileSize;
    
    @Column(name = "retain_file")
    private Boolean retainFile = false;
    
    @Column(name = "keep_version")
    private Boolean keepVersion = false;
    
    @Column(name = "keep_history")
    private Boolean keepHistory = false;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonBackReference
    private TaskMetadata task;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBusinessFileName() { return businessFileName; }
    public void setBusinessFileName(String businessFileName) { this.businessFileName = businessFileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public Boolean getRetainFile() { return retainFile; }
    public void setRetainFile(Boolean retainFile) { this.retainFile = retainFile; }
    
    public Boolean getKeepVersion() { return keepVersion; }
    public void setKeepVersion(Boolean keepVersion) { this.keepVersion = keepVersion; }
    
    public Boolean getKeepHistory() { return keepHistory; }
    public void setKeepHistory(Boolean keepHistory) { this.keepHistory = keepHistory; }
    
    public TaskMetadata getTask() { return task; }
    public void setTask(TaskMetadata task) { this.task = task; }
}