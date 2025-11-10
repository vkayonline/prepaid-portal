package online.vkay.prepaidportal.repository;

import online.vkay.prepaidportal.entity.HttpAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HttpAuditLogRepository extends JpaRepository<HttpAuditLog, Long> {
}