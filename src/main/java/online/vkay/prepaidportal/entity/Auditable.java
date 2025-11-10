package online.vkay.prepaidportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import online.vkay.prepaidportal.utils.DateUtils;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class Auditable {

    private static final String SYSTEM_USER = "system";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = false)
    private String updatedBy;

    // ----------------------------------------------------
    // Lifecycle Hooks
    // ----------------------------------------------------

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = DateUtils.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (createdBy == null) createdBy = SYSTEM_USER;
        if (updatedBy == null) updatedBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DateUtils.now();
        if (updatedBy == null) updatedBy = SYSTEM_USER;
    }
}
