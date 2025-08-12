package app.flo.repository;

import app.flo.entity.BusinessFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusinessFileRepository extends JpaRepository<BusinessFile, Long> {
    List<BusinessFile> findByTaskId(Long taskId);
}