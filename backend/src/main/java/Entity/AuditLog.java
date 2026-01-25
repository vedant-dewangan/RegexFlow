package Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name="audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(name="audit_id")
    private Long AuditId;

    @Getter
    @Setter
    @Column(nullable = false)
    private String Status;

    @Getter
    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "verified_by")
    private Users VerifiedBy;

    @Getter
    @Setter
    @Column(nullable = false,name="verified_at")
    private LocalDateTime VerifiedAt;

    @PrePersist
    public void prePersist(){
        this.VerifiedAt = LocalDateTime.now();
    }


    @Getter
    @Setter
    @OneToOne(optional = false)
    @JoinColumn(name = "Template_id",unique = true)
    private RegexTemplate Template;

}
