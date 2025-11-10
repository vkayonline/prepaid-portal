package online.vkay.prepaidportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_http")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpAuditLog extends Auditable{
    @Column(name = "request_id")
    private String requestId;
    private String ip;
    private String username;
    @Column(name = "channel")
    private String sourceChannel;
    private String uri;
    private String method;
    private String direction;
    private Integer status;
    private String rc;
    private String message;
    @Column(name = "duration")
    private Long durationMs;
}
